package l192.aakarsh.pocketops

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

        setContent {
            val themeMode by userStore.themeMode.collectAsState(initial = "SYSTEM")
            val isDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            PocketOpsTheme(darkTheme = isDarkTheme) {
                PocketOpsApp(
                    userStore = userStore,
                    shortcutAction = shortcutAction,
                    themeMode = themeMode,
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
