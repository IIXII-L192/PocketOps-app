package l192.aakarsh.pocketops

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import l192.aakarsh.pocketops.data.UserStore
import l192.aakarsh.pocketops.ui.PocketOpsApp
import l192.aakarsh.pocketops.ui.theme.PocketOpsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val userStore = UserStore(this)
        val shortcutAction = intent?.action
        val sharedLink = extractSharedLink(intent)
        
        handleIntent(intent)

        val pendingCrash = l192.aakarsh.pocketops.utils.DiagnosticLogger.getPendingCrashLogFile(this)
        if (pendingCrash != null) {
            shareLogFile(pendingCrash)
        }

        setContent {
            val themeMode by userStore.themeMode.collectAsState(initial = "SYSTEM")
            val dynamicColor by userStore.dynamicColor.collectAsState(initial = false)
            val isDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }
            
            var isLoggingActive by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(l192.aakarsh.pocketops.utils.DiagnosticLogger.isActive())
            }

            PocketOpsTheme(darkTheme = isDarkTheme, dynamicColor = dynamicColor) {
                PocketOpsApp(
                    userStore = userStore,
                    shortcutAction = shortcutAction,
                    sharedLink = sharedLink,
                    themeMode = themeMode,
                    dynamicColor = dynamicColor,
                    isLoggingActive = isLoggingActive,
                    onToggleLogging = {
                        if (isLoggingActive) {
                            val logFile = l192.aakarsh.pocketops.utils.DiagnosticLogger.stopLogging(this@MainActivity)
                            if (logFile != null) {
                                shareLogFile(logFile)
                            }
                            isLoggingActive = false
                        } else {
                            l192.aakarsh.pocketops.utils.DiagnosticLogger.startLogging(this@MainActivity)
                            isLoggingActive = true
                        }
                    },
                    onToggleDynamicColor = { enabled ->
                        lifecycleScope.launch {
                            userStore.saveDynamicColor(enabled)
                        }
                    },
                    onChangeThemeMode = { nextMode ->
                        lifecycleScope.launch {
                            userStore.saveThemeMode(nextMode)
                        }
                    },
                    onQrShown = { maxBrightness() },
                    onRestoreBrightness = { restoreBrightness() },
                    onDismiss = { finish() }
                )
            }
        }
    }

    private fun shareLogFile(file: java.io.File) {
        try {
            val authority = "${packageName}.provider"
            val uri = androidx.core.content.FileProvider.getUriForFile(this, authority, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share Diagnostic Log"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Error sharing log: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }


    private fun extractSharedLink(intent: Intent?): String? {
        if (intent == null) return null
        val raw = when (intent.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            Intent.ACTION_VIEW -> intent.dataString
            else -> null
        } ?: return null
        return Regex("https?://\\S+").find(raw)?.value?.trim()
    }
    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val userStore = UserStore(this)
        val receivedJsonUri = if (intent.action == Intent.ACTION_SEND) {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        } else if (intent.action == Intent.ACTION_VIEW) {
            intent.data
        } else {
            null
        }

        if (receivedJsonUri != null) {
            lifecycleScope.launch {
                try {
                    val inputStream = contentResolver.openInputStream(receivedJsonUri)
                    val jsonString = inputStream?.bufferedReader()?.use { it.readText() }
                    if (jsonString != null) {
                        val success = userStore.importFromJson(jsonString)
                        if (success) {
                            android.widget.Toast.makeText(this@MainActivity, "Backup restored successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(this@MainActivity, "Invalid backup format!", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(this@MainActivity, "Error importing backup: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private var clipboardListener: android.content.ClipboardManager.OnPrimaryClipChangedListener? = null

    override fun onResume() {
        super.onResume()
        l192.aakarsh.pocketops.utils.UpdateManager.checkForUpdates(this)
        
        val userStore = UserStore(this)
        checkClipboard(userStore)

        val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboardListener = android.content.ClipboardManager.OnPrimaryClipChangedListener {
            checkClipboard(userStore)
        }
        clipboard.addPrimaryClipChangedListener(clipboardListener)
    }

    override fun onPause() {
        super.onPause()
        if (clipboardListener != null) {
            val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.removePrimaryClipChangedListener(clipboardListener)
            clipboardListener = null
        }
    }

    private fun checkClipboard(userStore: UserStore) {
        lifecycleScope.launch {
            try {
                val isPaused = userStore.clipboardPause.first()
                if (isPaused) return@launch

                val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                if (clipboard.hasPrimaryClip()) {
                    val clipData = clipboard.primaryClip
                    if (clipData != null && clipData.itemCount > 0) {
                        val item = clipData.getItemAt(0)
                        val text = item.text?.toString()
                        val uri = item.uri
                        
                        var imageStream: java.io.InputStream? = null
                        if (uri != null && text.isNullOrBlank()) {
                            try {
                                imageStream = contentResolver.openInputStream(uri)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        if (!text.isNullOrBlank() || imageStream != null) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                if (!text.isNullOrBlank()) {
                                    userStore.addTextToClipboardHistory(text)
                                } else if (imageStream != null) {
                                    userStore.addImageStreamToClipboardHistory(imageStream)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun maxBrightness() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 1.0f
        window.attributes = layoutParams
    }

    private fun restoreBrightness() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness =
            android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = layoutParams
    }
}

