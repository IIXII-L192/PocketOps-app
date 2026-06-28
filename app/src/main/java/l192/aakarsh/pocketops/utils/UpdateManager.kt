package l192.aakarsh.pocketops.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
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
    data class Downloading(val progress: Int) : UpdateState
    data class ReadyToInstall(val releaseUrl: String, val fileName: String) : UpdateState
}

object UpdateManager {
    var updateState by mutableStateOf<UpdateState>(UpdateState.Idle)
        private set

    private var downloadId: Long = -1

    fun checkForUpdates(context: Context) {
        // Clean up any old downloaded APK files automatically on startup
        cleanObsoleteApks(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val currentVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toInt()
                } else {
                    packageInfo.versionCode
                }

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

                if (remoteVersionCode > currentVersionCode) {
                    // Check if the update is already fully downloaded locally
                    val fileName = "PocketOps-v$remoteVersionName.apk"
                    val localFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                    if (localFile.exists()) {
                        updateState = UpdateState.ReadyToInstall(releaseUrl, fileName)
                    } else {
                        updateState = UpdateState.UpdateAvailable(remoteVersionName, releaseUrl, remoteVersionCode)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startDownload(context: Context, urlStr: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(urlStr)).apply {
                setTitle("PocketOps Update")
                setDescription("Downloading latest release...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                // Download into app-private external downloads folder: Android/data/l192.aakarsh.pocketops/files/Download
                // Requires ZERO permissions (no READ/WRITE_EXTERNAL_STORAGE needed)
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadId = downloadManager.enqueue(request)
            updateState = UpdateState.Downloading(0)

            trackProgress(context, downloadManager, fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            updateState = UpdateState.Idle
        }
    }

    private fun trackProgress(context: Context, downloadManager: DownloadManager, fileName: String) {
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
                        updateState = UpdateState.ReadyToInstall("", fileName)
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        downloading = false
                        updateState = UpdateState.Idle
                    } else if (bytesTotal > 0) {
                        val progress = (bytesDownloaded * 100L / bytesTotal).toInt()
                        updateState = UpdateState.Downloading(progress)
                    }
                } else {
                    downloading = false
                    updateState = UpdateState.Idle
                }
                cursor?.close()
                delay(500)
            }
        }
    }

    fun installApk(context: Context, fileName: String) {
        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
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
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cleanObsoleteApks(context: Context) {
        try {
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir != null && downloadDir.exists()) {
                val files = downloadDir.listFiles()
                if (files != null) {
                    for (file in files) {
                        if (file.isFile && file.name.endsWith(".apk")) {
                            // Automatically clean up previously downloaded update packages
                            file.delete()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
