package l192.aakarsh.pocketops.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import l192.aakarsh.pocketops.R

@Composable
fun LocalSaveScreen(
    sharedFileUri: Uri?,
    sharedText: String?,
    onClearShared: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Extract metadata if file is shared
    var fileName by remember { mutableStateOf("shared_file") }
    var fileSize by remember { mutableStateOf(0L) }
    var mimeType by remember { mutableStateOf("*/*") }

    remember(sharedFileUri) {
        sharedFileUri?.let { uri ->
            mimeType = context.contentResolver.getType(uri) ?: "*/*"
            try {
                val cursor = context.contentResolver.query(
                    uri,
                    arrayOf(android.provider.OpenableColumns.DISPLAY_NAME, android.provider.OpenableColumns.SIZE),
                    null,
                    null,
                    null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIdx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (nameIdx != -1) fileName = it.getString(nameIdx) ?: "shared_file"
                        if (sizeIdx != -1) fileSize = it.getLong(sizeIdx)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(mimeType)
    ) { targetUri ->
        targetUri?.let { dest ->
            try {
                context.contentResolver.openOutputStream(dest)?.use { outStream ->
                    if (sharedFileUri != null) {
                        context.contentResolver.openInputStream(sharedFileUri)?.use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    } else if (sharedText != null) {
                        sharedText.byteInputStream().copyTo(outStream)
                    }
                }
                android.widget.Toast.makeText(context, "Saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                onClearShared()
                onDismiss()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Failed to save: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (sharedFileUri != null || sharedText != null) {
            // Show details of the shared content to save
            Text(
                text = "Save Shared Content",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (sharedFileUri != null) {
                        Text(text = "File Name: $fileName", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Size: ${formatFileSize(fileSize)}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Type: $mimeType", style = MaterialTheme.typography.bodyMedium)
                    } else if (sharedText != null) {
                        Text(text = "Shared Text Snippet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (sharedText.length > 150) sharedText.take(150) + "..." else sharedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (sharedFileUri != null) {
                        saveLauncher.launch(fileName)
                    } else if (sharedText != null) {
                        saveLauncher.launch("shared_text.txt")
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_sd_card),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Save Location", fontWeight = FontWeight.Bold)
            }
        } else {
            // Direct launch guide screen
            Text(
                text = "How to save files locally:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val steps = listOf(
                "Share any file, photo, or text from another app on your device.",
                "Choose PocketOps from the Android system share sheet.",
                "The app will automatically open to the Save page.",
                "Tap Select Save Location, pick a folder, and save!"
            )

            steps.forEachIndexed { index, step ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatFileSize(size: Long): String {
    if (size <= 0) return "Unknown size"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
