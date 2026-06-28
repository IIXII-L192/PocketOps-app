package l192.aakarsh.pocketops.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

sealed interface UpdateState {
    object Idle : UpdateState
    data class UpdateAvailable(val versionName: String, val releaseUrl: String, val versionCode: Int) : UpdateState
    data class Downloading(val versionName: String, val progress: Int) : UpdateState
    data class ReadyToInstall(val versionName: String, val releaseUrl: String, val fileName: String) : UpdateState
}

object UpdateManager {
    var updateState by mutableStateOf<UpdateState>(UpdateState.Idle)
        private set

    var hasLocalApk by mutableStateOf(false)
        private set

    private var downloadId: Long = -1

    fun getApkFile(context: Context, fileName: String): File {
        val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val publicFile = File(publicDir, fileName)
        if (publicFile.exists()) {
            return publicFile
        }
        val privateDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        return File(privateDir, fileName)
    }

    fun hasDownloadedApk(context: Context): Boolean {
        return try {
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val publicApks = publicDir.listFiles()?.any { it.isFile && it.name.startsWith("PocketOps-v") && it.name.endsWith(".apk") } ?: false
            if (publicApks) return true
            
            val privateDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            privateDir?.listFiles()?.any { it.isFile && it.name.startsWith("PocketOps-v") && it.name.endsWith(".apk") } ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun deleteDownloadedApks(context: Context) {
        try {
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            publicDir.listFiles()?.forEach {
                if (it.isFile && it.name.startsWith("PocketOps-v") && it.name.endsWith(".apk")) {
                    it.delete()
                }
            }
            val privateDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            privateDir?.listFiles()?.forEach {
                if (it.isFile && it.name.startsWith("PocketOps-v") && it.name.endsWith(".apk")) {
                    it.delete()
                }
            }
            hasLocalApk = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkForUpdates(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val currentVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toInt()
                } else {
                    packageInfo.versionCode
                }
                val currentVersionName = packageInfo.versionName ?: ""

                val url = URL("https://raw.githubusercontent.com/IIXII-L192/PocketOps-app/main/update.json")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.useCaches = false
                val responseText = connection.inputStream.bufferedReader().readText()

                val json = JSONObject(responseText)
                val remoteVersionCode = json.optInt("versionCode", 0)
                val remoteVersionName = json.optString("versionName", "")
                val releaseUrl = json.optString("releaseUrl", "")

                val activeUpdateFileName = "PocketOps-v$remoteVersionName.apk"

                val isAlreadyUpdated = currentVersionName == remoteVersionName || currentVersionCode >= remoteVersionCode
                cleanObsoleteApks(context, if (isAlreadyUpdated) null else activeUpdateFileName)

                if (remoteVersionCode > currentVersionCode) {
                    val localFile = getApkFile(context, activeUpdateFileName)
                    if (localFile.exists()) {
                        updateState = UpdateState.ReadyToInstall(remoteVersionName, releaseUrl, activeUpdateFileName)
                    } else {
                        updateState = UpdateState.UpdateAvailable(remoteVersionName, releaseUrl, remoteVersionCode)
                    }
                } else {
                    updateState = UpdateState.Idle
                    hasLocalApk = hasDownloadedApk(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to checking local files even if network is offline
                hasLocalApk = hasDownloadedApk(context)
            }
        }
    }

    fun startDownload(context: Context, urlStr: String, remoteVersionName: String) {
        val fileName = "PocketOps-v$remoteVersionName.apk"
        try {
            val request = DownloadManager.Request(Uri.parse(urlStr)).apply {
                setTitle("PocketOps Update")
                setDescription("Downloading latest release...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadId = downloadManager.enqueue(request)
            updateState = UpdateState.Downloading(remoteVersionName, 0)

            trackProgress(context, downloadManager, remoteVersionName, fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            updateState = UpdateState.Idle
        }
    }

    private fun trackProgress(context: Context, downloadManager: DownloadManager, remoteVersionName: String, fileName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var downloading = true
            while (downloading) {
                val q = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(q)
                if (cursor != null && cursor.moveToFirst()) {
                    val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val bytesIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                    val status = if (statusIdx >= 0) cursor.getInt(statusIdx) else DownloadManager.STATUS_FAILED
                    val bytesDownloaded = if (bytesIdx >= 0) cursor.getInt(bytesIdx) else 0
                    val bytesTotal = if (totalIdx >= 0) cursor.getInt(totalIdx) else 0

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false
                        updateState = UpdateState.ReadyToInstall(remoteVersionName, "", fileName)
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        downloading = false
                        updateState = UpdateState.Idle
                        hasLocalApk = hasDownloadedApk(context)
                    } else if (bytesTotal > 0) {
                        val progress = (bytesDownloaded * 100L / bytesTotal).toInt()
                        updateState = UpdateState.Downloading(remoteVersionName, progress)
                    }
                } else {
                    downloading = false
                    updateState = UpdateState.Idle
                    hasLocalApk = hasDownloadedApk(context)
                }
                cursor?.close()
                delay(500)
            }
        }
    }

    fun installApk(context: Context, fileName: String) {
        try {
            val file = getApkFile(context, fileName)
            if (file.exists()) {
                val apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "APK file not found.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Installation failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun cleanObsoleteApks(context: Context, activeUpdateFileName: String?) {
        try {
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            publicDir.listFiles()?.forEach {
                if (it.isFile && it.name.startsWith("PocketOps-v") && it.name.endsWith(".apk")) {
                    if (activeUpdateFileName != null && it.name == activeUpdateFileName) {
                        return@forEach
                    }
                    it.delete()
                }
            }
            val privateDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            privateDir?.listFiles()?.forEach {
                if (it.isFile && it.name.startsWith("PocketOps-v") && it.name.endsWith(".apk")) {
                    if (activeUpdateFileName != null && it.name == activeUpdateFileName) {
                        return@forEach
                    }
                    it.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
