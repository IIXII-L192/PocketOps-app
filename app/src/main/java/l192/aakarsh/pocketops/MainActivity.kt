package l192.aakarsh.pocketops

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import l192.aakarsh.pocketops.data.UserStore
import l192.aakarsh.pocketops.ui.QuickUpiApp

import l192.aakarsh.pocketops.ui.screens.QuickTool
import l192.aakarsh.pocketops.ui.theme.*
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userStore = UserStore(this)

        val startWithQr = intent?.action == "l192.aakarsh.pocketops.ACTION_SHOW_QR"

        setContent {
            QuickUpiApp(
                userStore = userStore,
                startWithQr = startWithQr,
                onQrShown = { maxBrightness() },
                onRestoreBrightness = { restoreBrightness() },
                onDismiss = { finish() }
            )
        }
    }

    private fun maxBrightness() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 1.0f // 1.0f is 100% brightness
        window.attributes = layoutParams
    }

    private fun restoreBrightness() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness =
            android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = layoutParams
    }
}


@Composable
fun PocketOpsTheme(
    tool: QuickTool? = null,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (tool) {
        QuickTool.UPI -> {
            if (isDarkTheme) {
                darkColorScheme(
                    primary = UpiBlueDark,
                    secondary = UpiBlueDark,
                    tertiary = UpiBlueDark,
                    surfaceVariant = UpiBlueDark.copy(alpha = 0.15f)
                )
            } else {
                lightColorScheme(
                    primary = UpiBlueLight,
                    secondary = UpiBlueLight,
                    tertiary = UpiBlueLight,
                    surfaceVariant = UpiBlueLight.copy(alpha = 0.15f)
                )
            }
        }
        QuickTool.WHATSAPP -> {
            if (isDarkTheme) {
                darkColorScheme(
                    primary = WhatsAppGreenDark,
                    secondary = WhatsAppGreenDark,
                    tertiary = WhatsAppGreenDark,
                    surfaceVariant = WhatsAppGreenDark.copy(alpha = 0.15f)
                )
            } else {
                lightColorScheme(
                    primary = WhatsAppGreenLight,
                    secondary = WhatsAppGreenLight,
                    tertiary = WhatsAppGreenLight,
                    surfaceVariant = WhatsAppGreenLight.copy(alpha = 0.15f)
                )
            }
        }
        QuickTool.INSTAGRAM -> {
            if (isDarkTheme) {
                darkColorScheme(
                    primary = InstaPinkDark,
                    secondary = InstaPinkDark,
                    tertiary = InstaPinkDark,
                    surfaceVariant = InstaPinkDark.copy(alpha = 0.15f)
                )
            } else {
                lightColorScheme(
                    primary = InstaPinkLight,
                    secondary = InstaPinkLight,
                    tertiary = InstaPinkLight,
                    surfaceVariant = InstaPinkLight.copy(alpha = 0.15f)
                )
            }
        }
        else -> {
            if (isDarkTheme) {
                darkColorScheme(
                    primary = Color(0xFFFE9F06), // Gold/Orange
                    onPrimary = Color(0xFF060D1D),
                    primaryContainer = Color(0xFF1F2C46),
                    onPrimaryContainer = Color(0xFFFE9F06),
                    secondary = Color(0xFFFE9F06),
                    background = Color(0xFF060D1D),
                    surface = Color(0xFF0C192E),
                    onBackground = Color.White,
                    onSurface = Color.White,
                    surfaceVariant = Color(0xFF13233F),
                    onSurfaceVariant = Color(0xFFCBD5E0)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFFE65100), // Rich orange/gold
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFFFF3E0),
                    onPrimaryContainer = Color(0xFFE65100),
                    secondary = Color(0xFFE65100),
                    background = Color(0xFFF4F6F9),
                    surface = Color.White,
                    onBackground = Color(0xFF060D1D),
                    onSurface = Color(0xFF060D1D),
                    surfaceVariant = Color(0xFFE0E0E0),
                    onSurfaceVariant = Color(0xFF424242)
                )
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}


