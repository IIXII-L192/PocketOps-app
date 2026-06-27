package l192.aakarsh.pocketops

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
            PocketOpsTheme {
                PocketOpsApp(
                    userStore = userStore,
                    shortcutAction = shortcutAction,
                    onQrShown = { maxBrightness() },
                    onRestoreBrightness = { restoreBrightness() },
                    onDismiss = { finish() }
                )
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
