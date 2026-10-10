package l192.aakarsh.pocketops.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private enum class DashboardFilter(val label: String) {
    ALL("All"),
    FAST_ACTIONS("Fast Actions"),
    FAVORITES("Favorites")
}

private data class DashboardTool(
    val quickTool: QuickTool,
    val title: String,
    val description: String,
    val iconRes: Int,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    usePaypal: Boolean = false,
    onToolSelected: (QuickTool) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var selectedFilter by remember { mutableStateOf(DashboardFilter.ALL) }

    val tools = listOf(
        DashboardTool(
            quickTool = QuickTool.UPI,
            title = "Pay Collect",
            description = "Offline payment QRs in seconds",
            iconRes = if (usePaypal) R.drawable.ic_paypal else R.drawable.ic_upi_pay,
            accentColor = if (usePaypal) {
                if (isDark) Color(0xFF90CAF9) else Color(0xFF003087)
            } else {
                if (isDark) Color(0xFF64B5F6) else Color(0xFF1565C0)
            }
        ),
        DashboardTool(
            QuickTool.WHATSAPP,
            "WhatsApp Direct",
            "Whatsapp chat without contacts",
            R.drawable.ic_whatsapp,
            if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
        ),
        DashboardTool(
            QuickTool.TELEGRAM,
            "Telegram Direct",
            "Telegram chat by username",
            R.drawable.ic_telegram,
            if (isDark) Color(0xFF4FC3F7) else Color(0xFF0288D1)
        ),
        DashboardTool(
            QuickTool.SMS,
            "Send SMS",
            "",
            R.drawable.ic_sms,
            if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100)
        ),
        DashboardTool(
            QuickTool.SOCIAL_PROFILER,
            "Social Profiler",
            "Search profiles on social media",
            R.drawable.ic_person_circle,
            if (isDark) Color(0xFFF48FB1) else Color(0xFFC2185B)
        ),
        DashboardTool(
            QuickTool.CLIPBOARD,
            "Clip Vault",
            "Smart clipboard history",
            R.drawable.ic_clipboard,
            if (isDark) Color(0xFFFFE082) else Color(0xFFF57F17)
        ),
        DashboardTool(
            QuickTool.LINK,
            "Bookmarks",
            "Save links with previews",
            R.drawable.ic_bookmarks,
            if (isDark) Color(0xFFB388FF) else Color(0xFF6200EA)
        ),
        DashboardTool(
            QuickTool.WEB,
            "Web Search",
            "Search with your engine",
            R.drawable.ic_globe,
            if (isDark) Color(0xFF80CBC4) else Color(0xFF00695C)
        ),
        DashboardTool(
            QuickTool.YT_EXPLORER,
            "YT Explorer",
            "Search directly on YouTube",
            R.drawable.ic_youtube,
            if (isDark) Color(0xFFEF9A9A) else Color(0xFFD32F2F)
        ),
        DashboardTool(
            QuickTool.LOCAL_SAVE,
            "Local Save",
            "Save shared files locally",
            R.drawable.ic_sd_card,
            if (isDark) Color(0xFFB0BEC5) else Color(0xFF37474F)
        )
    )

    val visibleTools = when (selectedFilter) {
        DashboardFilter.ALL -> tools
        DashboardFilter.FAST_ACTIONS -> tools.filter {
            it.quickTool in setOf(QuickTool.UPI, QuickTool.WHATSAPP, QuickTool.TELEGRAM, QuickTool.SMS)
        }
        DashboardFilter.FAVORITES -> tools.filter {
            it.quickTool in setOf(QuickTool.UPI, QuickTool.WHATSAPP, QuickTool.CLIPBOARD, QuickTool.LINK)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Quick Tools",
                    fontWeight = FontWeight.Bold
                )
            }
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Your everyday tools, together",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Pick a shortcut and get things done in a few taps.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            DashboardFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.label) }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
        ) {
            items(visibleTools, key = { it.quickTool.name }) { tool ->
                ToolCard(
                    title = tool.title,
                    description = tool.description,
                    iconRes = tool.iconRes,
                    accentColor = tool.accentColor,
                    onClick = { onToolSelected(tool.quickTool) }
                )
            }
        }
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
