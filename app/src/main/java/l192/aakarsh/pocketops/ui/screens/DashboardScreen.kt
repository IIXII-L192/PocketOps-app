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
    UPI, WHATSAPP, INSTAGRAM
}

@Composable
fun DashboardScreen(
    onToolSelected: (QuickTool) -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ToolCard(
            title = "Quick UPI",
            description = "Offline payment QRs in seconds",
            iconRes = R.drawable.ic_qr_code,
            accentColor = if (isDark) Color(0xFF64B5F6) else Color(0xFF1E88E5), // UPI Blue
            onClick = { onToolSelected(QuickTool.UPI) }
        )

        ToolCard(
            title = "Quick Chat",
            description = "Direct WhatsApp chat without saving contacts",
            iconRes = R.drawable.ic_whatsapp,
            accentColor = if (isDark) Color(0xFF81C784) else Color(0xFF4CAF50), // WhatsApp Green
            onClick = { onToolSelected(QuickTool.WHATSAPP) }
        )

        ToolCard(
            title = "Quick Insta",
            description = "Instant Instagram profile searches",
            iconRes = R.drawable.ic_instagram,
            accentColor = if (isDark) Color(0xFFF48FB1) else Color(0xFFE91E63), // Insta Pink
            onClick = { onToolSelected(QuickTool.INSTAGRAM) }
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
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.20f)),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
