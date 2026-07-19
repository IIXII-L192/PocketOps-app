package l192.aakarsh.pocketops.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import l192.aakarsh.pocketops.R

enum class QuickTool {
    UPI, WHATSAPP, TELEGRAM, SMS, SOCIAL_PROFILER, CLIPBOARD, LINK, WEB, YT_EXPLORER, LOCAL_SAVE
}

@Composable
fun DashboardScreen(
    usePaypal: Boolean = false,
    onToolSelected: (QuickTool) -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ToolCard(
            title = "Pay Collect",
            description = "Offline payment QRs in seconds",
            iconRes = if (usePaypal) R.drawable.ic_paypal else R.drawable.ic_upi_pay,
            accentColor = if (usePaypal) {
                if (isDark) Color(0xFF29B6F6) else Color(0xFF003087) // PayPal Blue
            } else {
                if (isDark) Color(0xFF64B5F6) else Color(0xFF1E88E5) // UPI Blue
            },
            onClick = { onToolSelected(QuickTool.UPI) }
        )

        ToolCard(
            title = "WhatsApp Direct",
            description = "Whatsapp chat without contacts",
            iconRes = R.drawable.ic_whatsapp,
            accentColor = if (isDark) Color(0xFF81C784) else Color(0xFF4CAF50), // WhatsApp Green
            onClick = { onToolSelected(QuickTool.WHATSAPP) }
        )

        ToolCard(
            title = "Telegram Direct",
            description = "Telegram chat by number/username",
            iconRes = R.drawable.ic_share,
            accentColor = if (isDark) Color(0xFF4FC3F7) else Color(0xFF03A9F4), // Telegram Light Blue
            onClick = { onToolSelected(QuickTool.TELEGRAM) }
        )

        ToolCard(
            title = "Send SMS",
            description = "Send SMS without saving contact",
            iconRes = R.drawable.ic_phone,
            accentColor = if (isDark) Color(0xFFFFB74D) else Color(0xFFFF9800), // SMS Orange
            onClick = { onToolSelected(QuickTool.SMS) }
        )

        ToolCard(
            title = "Social Profiler",
            description = "Instagram, Facebook, X, Threads, LinkedIn",
            iconRes = R.drawable.ic_social,
            accentColor = if (isDark) Color(0xFFF48FB1) else Color(0xFFE91E63), // Insta Pink
            onClick = { onToolSelected(QuickTool.SOCIAL_PROFILER) }
        )

        ToolCard(
            title = "Clip Vault",
            description = "Smart clipboard history",
            iconRes = R.drawable.ic_clipboard,
            accentColor = if (isDark) Color(0xFFFFD54F) else Color(0xFFF9A825), // Clipboard Yellow
            onClick = { onToolSelected(QuickTool.CLIPBOARD) }
        )

        ToolCard(
            title = "Bookmarks",
            description = "Save links with previews",
            iconRes = R.drawable.ic_bookmarks,
            accentColor = if (isDark) Color(0xFFB388FF) else Color(0xFF7C4DFF),
            onClick = { onToolSelected(QuickTool.LINK) }
        )

        ToolCard(
            title = "Web Search",
            description = "Search with your engine",
            iconRes = R.drawable.ic_globe,
            accentColor = if (isDark) Color(0xFF4DB6AC) else Color(0xFF009688), // Web Search Emerald Teal
            onClick = { onToolSelected(QuickTool.WEB) }
        )

        ToolCard(
            title = "YT Explorer",
            description = "Search directly on YouTube",
            iconRes = R.drawable.ic_youtube,
            accentColor = if (isDark) Color(0xFFEF5350) else Color(0xFFFF0000), // YouTube Red
            onClick = { onToolSelected(QuickTool.YT_EXPLORER) }
        )

        ToolCard(
            title = "Local Save",
            description = "Save shared files locally",
            iconRes = R.drawable.ic_sd_card,
            accentColor = if (isDark) Color(0xFF90A4AE) else Color(0xFF607D8B), // Slate/Grey-Blue
            onClick = { onToolSelected(QuickTool.LOCAL_SAVE) }
        )
    }
}

@Composable
fun ToolCard(
    title: String,
    description: String,
    iconRes: Int,
    accentColor: Color,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) {
        accentColor.copy(alpha = 0.14f)
    } else {
        accentColor.copy(alpha = 0.08f)
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }
        }
    }
}


