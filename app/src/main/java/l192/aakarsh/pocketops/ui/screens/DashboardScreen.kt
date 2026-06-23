package l192.aakarsh.pocketops.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import l192.aakarsh.pocketops.R

enum class QuickTool {
    UPI,
    WHATSAPP,
    INSTAGRAM
}

@Composable
fun DashboardScreen(
    onToolSelected: (QuickTool) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Select a Tool",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        ToolCard(
            title = "Quick UPI",
            description = "Generate dynamic payment QRs offline in seconds.",
            iconRes = R.drawable.ic_qr_code,
            gradientColors = listOf(Color(0xFF2979FF), Color(0xFF1565C0)),
            darkGradientColors = listOf(Color(0xFF1565C0), Color(0xFF0D47A1)),
            onClick = { onToolSelected(QuickTool.UPI) }
        )

        ToolCard(
            title = "Quick WhatsApp",
            description = "Start a chat without saving the number to contacts.",
            iconRes = R.drawable.ic_whatsapp,
            gradientColors = listOf(Color(0xFF00E676), Color(0xFF2E7D32)),
            darkGradientColors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)),
            onClick = { onToolSelected(QuickTool.WHATSAPP) }
        )

        ToolCard(
            title = "Quick Insta",
            description = "Open any Instagram profile instantly by username.",
            iconRes = R.drawable.ic_instagram,
            gradientColors = listOf(Color(0xFFFF1744), Color(0xFFAD1457)),
            darkGradientColors = listOf(Color(0xFFAD1457), Color(0xFF880E4F)),
            onClick = { onToolSelected(QuickTool.INSTAGRAM) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Made with 💖 by Aakarsh (L192)",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ToolCard(
    title: String,
    description: String,
    iconRes: Int,
    gradientColors: List<Color>,
    darkGradientColors: List<Color>,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, label = "pressScale")
    
    val isDark = MaterialTheme.colorScheme.primary.red < 0.5f // Simple heuristic for dark theme
    val activeGradients = if (isDark) darkGradientColors else gradientColors

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        try {
                            awaitRelease()
                        } finally {
                            pressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .background(Brush.horizontalGradient(activeGradients))
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

