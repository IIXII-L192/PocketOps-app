package l192.aakarsh.pocketops.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import l192.aakarsh.pocketops.R

@Composable
fun SettingsScreen(
    themeMode: String,
    dynamicColor: Boolean,
    isLoggingActive: Boolean,
    onToggleLogging: () -> Unit,
    onChangeThemeMode: (String) -> Unit,
    onToggleDynamicColor: (Boolean) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        // --- Theme Section ---
        Text(
            text = "App Theme",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Choose how PocketOps appears on your device.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ThemeOptionCard(
                title = "Follow System",
                description = "Default settings matched with your phone theme.",
                iconRes = R.drawable.ic_phone,
                isSelected = themeMode == "SYSTEM",
                onClick = { onChangeThemeMode("SYSTEM") }
            )

            ThemeOptionCard(
                title = "Light Mode",
                description = "Classic clean light interface background.",
                iconRes = R.drawable.ic_sun,
                isSelected = themeMode == "LIGHT",
                onClick = { onChangeThemeMode("LIGHT") }
            )

            ThemeOptionCard(
                title = "Dark Mode",
                description = "Sleek low-light friendly background.",
                iconRes = R.drawable.ic_moon,
                isSelected = themeMode == "DARK",
                onClick = { onChangeThemeMode("DARK") }
            )
        }

        // --- Dynamic Colors (Material You) --- Only supported on Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Dynamic Color",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Personalize colors using your active device wallpaper.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Standard M3 Switch Toggle Row for Dynamic Color
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_palette),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Use Dynamic Palette",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Apply system Material You wallpaper matching engine.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = onToggleDynamicColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Data Management Section ---
        Text(
            text = "Data Management",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Data Backup and Restore",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onExport,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_export),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onImport,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_import),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Help & Feedback Section ---
        Text(
            text = "Help & Feedback",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Get support or request new features directly on GitHub.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val context = LocalContext.current
            val openUrl = { url: String ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } catch (e: Exception) { }
            }

            SettingsLinkCard(
                title = "Report a Bug",
                iconRes = R.drawable.ic_bug,
                iconColor = Color(0xFFFF0000),
                onClick = { openUrl("https://github.com/IIXII-L192/PocketOps-app/issues") }
            )

            SettingsLinkCard(
                title = "Feedback",
                iconRes = R.drawable.ic_feedback,
                iconColor = Color(0xFFFFD500),
                onClick = { openUrl("https://github.com/IIXII-L192/PocketOps-app/discussions/categories/ideas") }
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_terminal),
                        contentDescription = null,
                        tint = Color(0xFF23AF31),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Diagnostic Log",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isLoggingActive) "Recording active process logcat..." else "Record diagnostic logs for developer crash reports.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    RecorderButton(
                        isRecording = isLoggingActive,
                        onClick = onToggleLogging
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- About Section ---
        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "PocketOps developer details and support.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingsLinkCard(
                title = "Donate",
                iconRes = R.drawable.ic_donate,
                iconColor = Color(0xFFFA0557),
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bmad192.vercel.app"))
                        context.startActivity(intent)
                    } catch (e: Exception) { }
                }
            )

            SettingsLinkCard(
                title = "Contact",
                iconRes = R.drawable.ic_envelope,
                iconColor = Color(0xFFFF9E7A),
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:192aakarsh@gmail.com"))
                        context.startActivity(intent)
                    } catch (e: Exception) { }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Footer Credits ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Made with 💖 by Aakarsh(IIXII-L192)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun RecorderButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(targetState = isRecording, label = "recorderTransition")
    
    val innerShapeProgress by transition.animateFloat(
        transitionSpec = { tween(250) },
        label = "innerShape"
    ) { recording ->
        if (recording) 0.35f else 1.0f
    }

    val innerSizeProgress by transition.animateFloat(
        transitionSpec = { tween(250) },
        label = "innerSize"
    ) { recording ->
        if (recording) 0.45f else 0.55f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "dashRotation")
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dashPhase"
    )

    val activeColor = MaterialTheme.colorScheme.onSurface
    val recordColor = Color(0xFFFF3B30)

    Box(
        modifier = modifier
            .size(36.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val outerRadius = size.width / 2 - 2.dp.toPx()
            
            if (isRecording) {
                val segmentSpacing = 360f / 5f
                val segmentSweep = 48f
                repeat(5) { segmentIndex ->
                    drawArc(
                        color = activeColor,
                        startAngle = dashPhase + segmentIndex * segmentSpacing,
                        sweepAngle = segmentSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                        size = size.copy(width = outerRadius * 2, height = outerRadius * 2),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            } else {
                drawCircle(
                    color = activeColor.copy(alpha = 0.8f),
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            val innerSize = size.width * innerSizeProgress
            val rect = Rect(
                center.x - innerSize / 2,
                center.y - innerSize / 2,
                center.x + innerSize / 2,
                center.y + innerSize / 2
            )
            val cornerRadius = (innerSize / 2) * innerShapeProgress

            val path = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = rect,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                )
            }
            drawPath(path = path, color = recordColor)
        }
    }
}


@Composable
fun SettingsLinkCard(
    title: String,
    description: String? = null,
    iconRes: Int,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOptionCard(
    title: String,
    description: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) activeColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) activeColor else inactiveColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
