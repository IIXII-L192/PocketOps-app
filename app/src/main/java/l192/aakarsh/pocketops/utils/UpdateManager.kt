package l192.aakarsh.pocketops.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

sealed interface UpdateState {
    object Idle : UpdateState
    data class UpdateAvailable(val versionName: String, val apkUrl: String, val versionCode: Int) : UpdateState
    data class Downloading(val versionName: String, val progress: Int) : UpdateState
    data class ReadyToInstall(val versionName: String, val fileName: String) : UpdateState
}

object UpdateManager {
    var updateState by mutableStateOf<UpdateState>(UpdateState.Idle)
        private set

    var hasLocalApk by mutableStateOf(false)
        private set

    private var lastCheckTime: Long = 0

    /**
     * Get the app-private downloads directory. This is where APKs are saved
     * so that FileProvider can always access and serve them without any storage permissions.
     */
    private fun getDownloadDir(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "updates")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Get the APK file for a specific version.
     */
    fun getApkFile(context: Context, fileName: String): File {
        return File(getDownloadDir(context), fileName)
    }

    /**
     * Check if any PocketOps APK exists in our private download directory.
     */
    fun hasDownloadedApk(context: Context): Boolean {
        return try {
            val dir = getDownloadDir(context)
            dir.listFiles()?.any {
                it.isFile && it.name.startsWith("PocketOps-v") && it.name.endsWith(".apk")
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete ALL downloaded PocketOps APKs from private directory.
     */
    fun deleteDownloadedApks(context: Context) {
        try {
            val dir = getDownloadDir(context)
            dir.listFiles()?.forEach {
                if (it.isFile && it.name.startsWith("PocketOps-v") && it.name.endsWith(".apk")) {
                    it.delete()
                }
            }
            // Also clean public downloads (leftover from old versions)
            try {
                val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                publicDir.listFiles()?.forEach {
                    if (it.isFile && it.name.startsWith("PocketOps-v") && it.name.endsWith(".apk")) {
                        it.delete()
                    }
                }
            } catch (_: Exception) {}
            hasLocalApk = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Check for updates by fetching update.json from GitHub.
     */
    fun checkForUpdates(context: Context) {
        val now = System.currentTimeMillis()
        if (now - lastCheckTime < 5000) return
        lastCheckTime = now

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode
                }

                val url = URL("https://raw.githubusercontent.com/IIXII-L192/PocketOps-app/main/update.json?t=${System.currentTimeMillis()}")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.useCaches = false
                connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                connection.setRequestProperty("Pragma", "no-cache")
                val responseText = connection.inputStream.bufferedReader().readText()
                connection.disconnect()

                val json = JSONObject(responseText)
                val remoteVersionCode = json.optInt("versionCode", 0)
                val remoteVersionName = json.optString("versionName", "")
                val apkUrl = json.optString("apkUrl", "")

                val activeFileName = "PocketOps-v$remoteVersionName.apk"

                // Clean obsolete APKs (keep only the one matching current remote version, if any)
                cleanObsoleteApks(context, if (remoteVersionCode > currentVersionCode) activeFileName else null)

                if (remoteVersionCode > currentVersionCode) {
                    // Update is available — check if we already downloaded this version's APK
                    val localFile = getApkFile(context, activeFileName)
                    if (localFile.exists() && localFile.length() > 0) {
                        updateState = UpdateState.ReadyToInstall(remoteVersionName, activeFileName)
                    } else {
                        updateState = UpdateState.UpdateAvailable(remoteVersionName, apkUrl, remoteVersionCode)
                    }
                } else {
                    // Already up to date
                    updateState = UpdateState.Idle
                    hasLocalApk = hasDownloadedApk(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                hasLocalApk = hasDownloadedApk(context)
            }
        }
    }

    /**
     * Download the APK from the GitHub release URL directly to app-private storage.
     */
    fun startDownload(context: Context, urlStr: String, remoteVersionName: String) {
        val fileName = "PocketOps-v$remoteVersionName.apk"
        val destFile = getApkFile(context, fileName)

        // Delete any existing file first
        if (destFile.exists()) destFile.delete()

        updateState = UpdateState.Downloading(remoteVersionName, 0)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(urlStr)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.connect()

                val fileLength = connection.contentLength
                val inputStream = connection.inputStream
                val outputStream = destFile.outputStream()

                val data = ByteArray(4096)
                var total: Long = 0
                var count: Int
                var lastProgressUpdate = -1
                var lastUpdateTime = 0L

                while (inputStream.read(data).also { count = it } != -1) {
                    total += count
                    outputStream.write(data, 0, count)
                    if (fileLength > 0) {
                        val progress = (total * 100 / fileLength).toInt()
                        val now = System.currentTimeMillis()
                        // Throttle progress updates to avoid UI stutter
                        if (progress != lastProgressUpdate && now - lastUpdateTime > 100) {
                            updateState = UpdateState.Downloading(remoteVersionName, progress)
                            lastProgressUpdate = progress
                            lastUpdateTime = now
                        }
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                connection.disconnect()

                updateState = UpdateState.ReadyToInstall(remoteVersionName, fileName)
            } catch (e: Exception) {
                e.printStackTrace()
                if (destFile.exists()) destFile.delete()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
                updateState = UpdateState.Idle
                hasLocalApk = hasDownloadedApk(context)
            }
        }
    }

    /**
     * Launch the system Package Installer for the given APK file.
     * Uses FileProvider to generate a content:// URI that the installer can read.
     */
    fun installApk(context: Context, fileName: String) {
        try {
            val file = getApkFile(context, fileName)
            if (!file.exists()) {
                Toast.makeText(context, "APK file not found. Please download again.", Toast.LENGTH_SHORT).show()
                updateState = UpdateState.Idle
                return
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Install failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Delete APKs that don't match the currently-needed update file.
     */
    private fun cleanObsoleteApks(context: Context, activeUpdateFileName: String?) {
        try {
            val dir = getDownloadDir(context)
            dir.listFiles()?.forEach {
                if (it.isFile && it.name.startsWith("PocketOps-v") && it.name.endsWith(".apk")) {
                    if (activeUpdateFileName != null && it.name == activeUpdateFileName) {
                        return@forEach // Keep this one
                    }
                    it.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
