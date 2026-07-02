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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import l192.aakarsh.pocketops.data.UserStore
import l192.aakarsh.pocketops.ui.PocketOpsApp
import l192.aakarsh.pocketops.ui.theme.PocketOpsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val userStore = UserStore(this)
        val shortcutAction = intent?.action
        
        handleIntent(intent)

        setContent {
            val themeMode by userStore.themeMode.collectAsState(initial = "SYSTEM")
            val dynamicColor by userStore.dynamicColor.collectAsState(initial = false)
            val isDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            PocketOpsTheme(darkTheme = isDarkTheme, dynamicColor = dynamicColor) {
                PocketOpsApp(
                    userStore = userStore,
                    shortcutAction = shortcutAction,
                    themeMode = themeMode,
                    dynamicColor = dynamicColor,
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
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

    override fun onResume() {
        super.onResume()
        l192.aakarsh.pocketops.utils.UpdateManager.checkForUpdates(this)
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
