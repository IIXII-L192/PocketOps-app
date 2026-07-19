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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        ToolCard(
            title = "Pay Collect",
            description = "Offline payment QRs in seconds",
            iconRes = if (usePaypal) R.drawable.ic_paypal else R.drawable.ic_upi_pay,
            accentColor = if (usePaypal) {
                if (isDark) Color(0xFF90CAF9) else Color(0xFF003087)
            } else {
                if (isDark) Color(0xFF64B5F6) else Color(0xFF1565C0)
            },
            onClick = { onToolSelected(QuickTool.UPI) }
        )

        ToolCard(
            title = "WhatsApp Direct",
            description = "Whatsapp chat without contacts",
            iconRes = R.drawable.ic_whatsapp,
            accentColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
            onClick = { onToolSelected(QuickTool.WHATSAPP) }
        )

        ToolCard(
            title = "Telegram Direct",
            description = "Telegram chat by username",
            iconRes = R.drawable.ic_telegram,
            accentColor = if (isDark) Color(0xFF4FC3F7) else Color(0xFF0288D1),
            onClick = { onToolSelected(QuickTool.TELEGRAM) }
        )

        ToolCard(
            title = "Send SMS",
            description = "",
            iconRes = R.drawable.ic_sms,
            accentColor = if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100),
            onClick = { onToolSelected(QuickTool.SMS) }
        )

        ToolCard(
            title = "Social Profiler",
            description = "Search profiles on social media",
            iconRes = R.drawable.ic_person_circle,
            accentColor = if (isDark) Color(0xFFF48FB1) else Color(0xFFC2185B),
            onClick = { onToolSelected(QuickTool.SOCIAL_PROFILER) }
        )

        ToolCard(
            title = "Clip Vault",
            description = "Smart clipboard history",
            iconRes = R.drawable.ic_clipboard,
            accentColor = if (isDark) Color(0xFFFFE082) else Color(0xFFF57F17),
            onClick = { onToolSelected(QuickTool.CLIPBOARD) }
        )

        ToolCard(
            title = "Bookmarks",
            description = "Save links with previews",
            iconRes = R.drawable.ic_bookmarks,
            accentColor = if (isDark) Color(0xFFB388FF) else Color(0xFF6200EA),
            onClick = { onToolSelected(QuickTool.LINK) }
        )

        ToolCard(
            title = "Web Search",
            description = "Search with your engine",
            iconRes = R.drawable.ic_globe,
            accentColor = if (isDark) Color(0xFF80CBC4) else Color(0xFF00695C),
            onClick = { onToolSelected(QuickTool.WEB) }
        )

        ToolCard(
            title = "YT Explorer",
            description = "Search directly on YouTube",
            iconRes = R.drawable.ic_youtube,
            accentColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFD32F2F),
            onClick = { onToolSelected(QuickTool.YT_EXPLORER) }
        )

        ToolCard(
            title = "Local Save",
            description = "Save shared files locally",
            iconRes = R.drawable.ic_sd_card,
            accentColor = if (isDark) Color(0xFFB0BEC5) else Color(0xFF37474F),
            onClick = { onToolSelected(QuickTool.LOCAL_SAVE) }
        )
    }
}

@Composable
fun ToolCard(
    title: String,
    description: String = "",
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
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}


