package l192.aakarsh.pocketops.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.launch
import l192.aakarsh.pocketops.R
import l192.aakarsh.pocketops.data.UserStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickClipScreen(
    userStore: UserStore,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    
    val clipItems by userStore.clipItems.collectAsState(initial = emptyList())
    val isPaused by userStore.clipboardPause.collectAsState(initial = false)
    
    var itemToDelete by remember { mutableStateOf<UserStore.ClipItem?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Retain and manage copied text or image notes. PocketOps automatically registers copies when the app is active.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Controls: Pause/Resume Toggle & Delete All
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pause/Resume Button
            Button(
                onClick = {
                    val target = !isPaused
                    scope.launch {
                        userStore.saveClipboardPause(target)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPaused) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFFFDC00),
                    contentColor = if (isPaused) MaterialTheme.colorScheme.onSurfaceVariant else Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = painterResource(if (isPaused) R.drawable.ic_install else R.drawable.ic_check),
                    contentDescription = if (isPaused) "Resume" else "Active",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPaused) "Paused" else "Running",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            
            // Delete All Button
            val hasItems = clipItems.isNotEmpty()
            Button(
                onClick = { if (hasItems) showDeleteAllConfirm = true },
                enabled = hasItems,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasItems) Color(0xFFFF3B30) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = if (hasItems) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = "Delete All",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Delete All",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        if (clipItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved clips yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                clipItems.forEach { item ->
                    ClipItemPane(
                        item = item,
                        context = context,
                        onCopy = {
                            if (item.type == "text") {
                                clipboardManager.setText(AnnotatedString(item.content))
                                android.widget.Toast.makeText(context, "Text copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                userStore.copyImageToClipboard(item.content)
                            }
                        },
                        onDelete = { itemToDelete = item }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    // Delete Single Item Confirmation Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Clip") },
            text = { Text("Are you sure you want to delete this clip from history?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { userStore.deleteClipItem(it) }
                        itemToDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF3B30))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete All Confirmation Dialog
    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            title = { Text("Delete All Clips") },
            text = { Text("Are you sure you want to delete all saved clips? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userStore.clearAllClipItems()
                        showDeleteAllConfirm = false
                    }
                ) {
                    Text("Delete All", color = Color(0xFFFF3B30))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ClipItemPane(
    item: UserStore.ClipItem,
    context: Context,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                if (item.type == "text") {
                    Text(
                        text = item.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    // Image preview
                    val imgBitmap = remember(item.content) {
                        try {
                            val file = java.io.File(context.filesDir.resolve("Clipboard"), item.content)
                            if (file.exists()) {
                                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                            } else null
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    if (imgBitmap != null) {
                        Image(
                            bitmap = imgBitmap,
                            contentDescription = "Image Clip",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(height = 80.dp, width = 120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        )
                    } else {
                        Text(
                            text = "[Image unavailable]",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Actions: Copy & Delete
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription = "Copy to Clipboard",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Delete Clip",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
