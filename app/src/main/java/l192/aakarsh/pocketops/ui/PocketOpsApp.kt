package l192.aakarsh.pocketops.ui

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import l192.aakarsh.pocketops.R
import l192.aakarsh.pocketops.data.UserStore
import l192.aakarsh.pocketops.ui.screens.DashboardScreen
import l192.aakarsh.pocketops.ui.screens.EnterAmountScreen
import l192.aakarsh.pocketops.ui.screens.QuickChatScreen
import l192.aakarsh.pocketops.ui.screens.SocialProfilerScreen
import l192.aakarsh.pocketops.ui.screens.QuickTool
import l192.aakarsh.pocketops.ui.screens.SetupScreen
import l192.aakarsh.pocketops.ui.screens.ShowQrScreen
import l192.aakarsh.pocketops.ui.screens.SettingsScreen
import l192.aakarsh.pocketops.ui.screens.QuickClipScreen
import l192.aakarsh.pocketops.ui.screens.QuickLinkScreen
import l192.aakarsh.pocketops.ui.screens.QuickWebScreen
import l192.aakarsh.pocketops.ui.screens.YtExplorerScreen
import l192.aakarsh.pocketops.ui.screens.LocalSaveScreen
import l192.aakarsh.pocketops.utils.QRCodeGenerator
import l192.aakarsh.pocketops.utils.UpdateManager
import l192.aakarsh.pocketops.utils.UpdateState
import java.net.HttpURLConnection
import java.net.URL

sealed interface PocketOpsUiState {
    data object Dashboard : PocketOpsUiState
    data class Setup(val isManaging: Boolean) : PocketOpsUiState
    data class EnterAmount(val upiIds: List<String>, val defaultUpiId: String) : PocketOpsUiState
    data class ShowQr(
        val amount: String, val qrBitmap: Bitmap, val upiId: String, val payeeName: String
    ) : PocketOpsUiState

    data object WhatsApp : PocketOpsUiState
    data object Telegram : PocketOpsUiState
    data object SMS : PocketOpsUiState
    data object SocialProfiler : PocketOpsUiState
    data object Clipboard : PocketOpsUiState
    data object Link : PocketOpsUiState
    data object Web : PocketOpsUiState
    data object YtExplorer : PocketOpsUiState
    data object LocalSave : PocketOpsUiState
    data object Settings : PocketOpsUiState

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketOpsApp(
    userStore: UserStore,
    shortcutAction: String?,
    sharedLink: String?,
    sharedFileUri: Uri? = null,
    sharedText: String? = null,
    onClearShared: () -> Unit = {},
    themeMode: String,
    dynamicColor: Boolean = false,
    isLoggingActive: Boolean = false,
    onToggleLogging: () -> Unit = {},
    onToggleDynamicColor: (Boolean) -> Unit = {},
    onChangeThemeMode: (String) -> Unit = {},
    onQrShown: () -> Unit = {},
    onRestoreBrightness: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val usePaypal by userStore.usePaypal.collectAsState(initial = false)
    val savedUpiIds by userStore.upiIds.collectAsState(initial = emptyList())
    val savedPaypalIds by userStore.paypalIds.collectAsState(initial = emptyList())
    
    // Choose active IDs based on mode
    val activeIds = if (usePaypal) savedPaypalIds else savedUpiIds
    
    val savedDefaultUpiId by userStore.defaultUpiId.collectAsState(initial = null)
    val savedDefaultPaypalId by userStore.defaultPaypalId.collectAsState(initial = null)
    val activeDefaultId = if (usePaypal) savedDefaultPaypalId else savedDefaultUpiId

    val savedPayeeName by userStore.payeeName.collectAsState(initial = null)
    val recentAmounts by userStore.recentAmounts.collectAsState(initial = emptyList())
    val showUpiId by userStore.showUpiId.collectAsState(initial = true)

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val jsonString = inputStream?.bufferedReader()?.use { it.readText() }
                    if (jsonString != null) {
                        val success = userStore.importFromJson(jsonString)
                        if (success) {
                            android.widget.Toast.makeText(context, "Backup restored successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Invalid backup format!", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } else {
                        android.widget.Toast.makeText(context, "Failed to read file", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Error importing: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val exportBackup: () -> Unit = {
        scope.launch {
            try {
                val jsonString = userStore.exportToJson()
                val sdf = java.text.SimpleDateFormat("ddMMyyyy", java.util.Locale.getDefault())
                val dateStr = sdf.format(java.util.Date())
                val fileName = "PocketOps_Backup_$dateStr.pocketops"
                
                val file = java.io.File(context.cacheDir, fileName)
                file.writeText(jsonString)
                
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Export PocketOps Backup"))
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Error exporting: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
    
    val navigationStack = remember { mutableStateListOf<PocketOpsUiState>(PocketOpsUiState.Dashboard) }
    val uiState = navigationStack.lastOrNull() ?: PocketOpsUiState.Dashboard
    
    var showChatSettings by remember { mutableStateOf(false) }
    var selectingCountry by remember { mutableStateOf(false) }
    var processedShortcut by remember(shortcutAction) { mutableStateOf(shortcutAction) }

    val navigateTo: (PocketOpsUiState) -> Unit = { state ->
        if (navigationStack.lastOrNull() != state) {
            navigationStack.add(state)
        }
    }

    var pendingSharedLink by remember(sharedLink) { mutableStateOf(sharedLink?.takeIf { it.startsWith("http://") || it.startsWith("https://") }) }
    var linkPreviewTitle by remember(pendingSharedLink) { mutableStateOf(pendingSharedLink ?: "") }
    var linkPreviewIcon by remember(pendingSharedLink) { mutableStateOf<String?>(null) }

    // Handle shared link on startup
    LaunchedEffect(sharedLink) {
        if (sharedLink != null) {
            userStore.saveQuickLink("", sharedLink, null)
            navigateTo(PocketOpsUiState.Link)
        }
    }

    // Handle shared file or text on startup
    LaunchedEffect(sharedFileUri, sharedText) {
        if (sharedFileUri != null || sharedText != null) {
            navigateTo(PocketOpsUiState.LocalSave)
        }
    }

    LaunchedEffect(pendingSharedLink) {
        val url = pendingSharedLink ?: return@LaunchedEffect
        navigationStack.clear()
        navigationStack.add(PocketOpsUiState.Dashboard)
        navigationStack.add(PocketOpsUiState.Link)
        withContext(Dispatchers.IO) { fetchLinkPreview(url) }.let { preview ->
            linkPreviewTitle = preview.first.ifBlank { url }
            linkPreviewIcon = preview.second
        }
    }
    LaunchedEffect(Unit) {
        UpdateManager.checkForUpdates(context)
    }

    LaunchedEffect(processedShortcut) {
        val action = processedShortcut ?: return@LaunchedEffect
        navigationStack.clear()
        navigationStack.add(PocketOpsUiState.Dashboard)
        val targetState = when (action) {
            "l192.aakarsh.pocketops.ACTION_QUICK_UPI", "l192.aakarsh.pocketops.ACTION_SHOW_QR" -> {
                if (activeIds.isNotEmpty()) {
                    PocketOpsUiState.EnterAmount(activeIds, activeDefaultId ?: activeIds.first())
                } else {
                    PocketOpsUiState.Setup(isManaging = false)
                }
            }
            "l192.aakarsh.pocketops.ACTION_QUICK_CHAT" -> PocketOpsUiState.WhatsApp
            "l192.aakarsh.pocketops.ACTION_QUICK_INSTA" -> PocketOpsUiState.SocialProfiler
            else -> PocketOpsUiState.Dashboard
        }
        if (targetState != PocketOpsUiState.Dashboard) {
            navigationStack.add(targetState)
        }
        processedShortcut = null
    }


    PocketOpsContent(
        userStore = userStore,
        uiState = uiState,
        sharedFileUri = sharedFileUri,
        sharedText = sharedText,
        onClearShared = onClearShared,
        recentAmounts = recentAmounts,
        upiIds = activeIds,
        defaultUpiId = activeDefaultId,
        showUpiId = showUpiId,
        themeMode = themeMode,
        usePaypal = usePaypal,
        dynamicColor = dynamicColor,
        onToggleDynamicColor = onToggleDynamicColor,
        onChangeThemeMode = onChangeThemeMode,
        payeeName = savedPayeeName,
        showChatSettings = showChatSettings,
        onToggleChatSettings = { showChatSettings = it },
        selectingCountry = selectingCountry,
        onToggleSelectingCountry = { selectingCountry = it },
        onTogglePaypal = { use ->
            scope.launch {
                userStore.saveUsePaypal(use)
                val targetIds = if (use) {
                    userStore.paypalIds.first()
                } else {
                    userStore.upiIds.first()
                }
                val targetDefault = if (use) {
                    userStore.defaultPaypalId.first()
                } else {
                    userStore.defaultUpiId.first()
                }
                if (uiState is PocketOpsUiState.Setup || uiState is PocketOpsUiState.EnterAmount || uiState is PocketOpsUiState.ShowQr) {
                    val targetState = if (uiState is PocketOpsUiState.Setup && uiState.isManaging) {
                        PocketOpsUiState.Setup(isManaging = true)
                    } else {
                        if (targetIds.isEmpty()) {
                            PocketOpsUiState.Setup(isManaging = false)
                        } else {
                            PocketOpsUiState.EnterAmount(targetIds, targetDefault ?: targetIds.first())
                        }
                    }
                    navigationStack.clear()
                    navigationStack.add(PocketOpsUiState.Dashboard)
                    navigationStack.add(targetState)
                }
            }
        },
        onSaveUpiIds = { ids, name, defaultId ->
            scope.launch {
                if (usePaypal) {
                    userStore.savePaypalIds(ids)
                    userStore.saveDefaultPaypalId(defaultId)
                } else {
                    userStore.saveUpiIds(ids)
                    userStore.saveDefaultUpiId(defaultId)
                }
                userStore.savePayeeName(name)
                if (navigationStack.isNotEmpty() && navigationStack.lastOrNull() is PocketOpsUiState.Setup) {
                    navigationStack.removeAt(navigationStack.lastIndex)
                }
                if (ids.isNotEmpty()) {
                    navigateTo(PocketOpsUiState.EnterAmount(ids, defaultId))
                }
            }
        },
        onGenerateQr = { amount, note, selectedId ->
            if (amount.isNotBlank()) {
                scope.launch { userStore.saveRecentAmount(amount) }
            }

            val payURL = if (usePaypal) {
                val cleanAmount = amount.trim()
                if (cleanAmount.isNotBlank()) "https://paypal.me/$selectedId/$cleanAmount"
                else "https://paypal.me/$selectedId"
            } else {
                val uriBuilder = Uri.Builder().scheme("upi").authority("pay")
                    .appendQueryParameter("pa", selectedId)
                    .apply {
                        if (amount.isNotBlank()) {
                            appendQueryParameter("am", amount)
                        }
                    }.appendQueryParameter("cu", "INR")

                if (!savedPayeeName.isNullOrBlank()) {
                    uriBuilder.appendQueryParameter("pn", savedPayeeName)
                }
                if (note.isNotBlank()) {
                    uriBuilder.appendQueryParameter("tn", note)
                }
                uriBuilder.build().toString()
            }

            scope.launch {
                val bitmap = withContext(Dispatchers.Default) {
                    QRCodeGenerator.generateQRCode(context, payURL, 1024, 1024)
                }
                navigateTo(
                    PocketOpsUiState.ShowQr(
                        amount, bitmap, selectedId, savedPayeeName ?: ""
                    )
                )
            }
        },
        onManageUpiIds = { navigateTo(PocketOpsUiState.Setup(isManaging = true)) },
        onBackToHome = {
            if (selectingCountry) {
                selectingCountry = false
            } else if (showChatSettings) {
                showChatSettings = false
            } else if (navigationStack.size > 1) {
                navigationStack.removeAt(navigationStack.lastIndex)
            }
        },
        onOpenSettings = { navigateTo(PocketOpsUiState.Settings) },
        onToolSelected = { tool ->
            val targetState = when (tool) {
                QuickTool.UPI -> {
                    if (activeIds.isEmpty()) PocketOpsUiState.Setup(isManaging = false)
                    else PocketOpsUiState.EnterAmount(activeIds, activeDefaultId ?: activeIds.first())
                }
                QuickTool.WHATSAPP -> PocketOpsUiState.WhatsApp
                QuickTool.TELEGRAM -> PocketOpsUiState.Telegram
                QuickTool.SMS -> PocketOpsUiState.SMS
                QuickTool.SOCIAL_PROFILER -> PocketOpsUiState.SocialProfiler
                QuickTool.CLIPBOARD -> PocketOpsUiState.Clipboard
                QuickTool.LINK -> PocketOpsUiState.Link
                QuickTool.WEB -> PocketOpsUiState.Web
                QuickTool.YT_EXPLORER -> PocketOpsUiState.YtExplorer
                QuickTool.LOCAL_SAVE -> PocketOpsUiState.LocalSave
            }
            navigateTo(targetState)
        },
        onQrShown = onQrShown,
        onRestoreBrightness = onRestoreBrightness,
        onDismiss = onDismiss,
        onExport = exportBackup,
        onImport = { importLauncher.launch("application/json") },
        isLoggingActive = isLoggingActive,
        onToggleLogging = onToggleLogging
    )

    if (pendingSharedLink != null) {
        BasicAlertDialog(onDismissRequest = { pendingSharedLink = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Save Link", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(linkPreviewTitle, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                    Text(pendingSharedLink ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { pendingSharedLink = null }) { Text("Cancel") }
                        Button(onClick = {
                            val url = pendingSharedLink
                            if (url != null) {
                                userStore.saveQuickLink(linkPreviewTitle, url, linkPreviewIcon)
                                pendingSharedLink = null
                                navigateTo(PocketOpsUiState.Link)
                            }
                        }) { Text("Save") }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketOpsContent(
    userStore: UserStore,
    uiState: PocketOpsUiState,
    sharedFileUri: Uri? = null,
    sharedText: String? = null,
    onClearShared: () -> Unit = {},
    recentAmounts: List<String> = emptyList(),
    upiIds: List<String> = emptyList(),
    defaultUpiId: String? = null,
    payeeName: String? = null,
    showUpiId: Boolean = true,
    themeMode: String = "SYSTEM",
    usePaypal: Boolean = false,
    dynamicColor: Boolean = false,
    onChangeThemeMode: (String) -> Unit = {},
    onTogglePaypal: (Boolean) -> Unit = {},
    onToggleDynamicColor: (Boolean) -> Unit = {},
    showChatSettings: Boolean = false,
    onToggleChatSettings: (Boolean) -> Unit = {},
    selectingCountry: Boolean = false,
    onToggleSelectingCountry: (Boolean) -> Unit = {},
    onSaveUpiIds: (List<String>, String, String) -> Unit,
    onGenerateQr: (String, String, String) -> Unit,
    onManageUpiIds: () -> Unit,
    onBackToHome: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onToolSelected: (QuickTool) -> Unit = {},
    onQrShown: () -> Unit = {},
    onRestoreBrightness: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onExport: () -> Unit = {},
    onImport: () -> Unit = {},
    isLoggingActive: Boolean = false,
    onToggleLogging: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 40.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 10.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ── Top Bar ──────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier.size(width = 80.dp, height = 40.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (uiState != PocketOpsUiState.Dashboard) {
                            IconButton(onClick = onBackToHome) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_back),
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            UpdateTag()
                        }
                    }

                    Text(
                        text = when (uiState) {
                            PocketOpsUiState.WhatsApp -> if (selectingCountry) "Select Country" else if (showChatSettings) "Chat Settings" else "WhatsApp Direct"
                            PocketOpsUiState.Telegram -> if (selectingCountry) "Select Country" else if (showChatSettings) "Chat Settings" else "Telegram Direct"
                            PocketOpsUiState.SMS -> if (selectingCountry) "Select Country" else if (showChatSettings) "Chat Settings" else "Send SMS"
                            PocketOpsUiState.SocialProfiler -> "Social Profiler"
                            PocketOpsUiState.Clipboard -> "Clip Vault"
                            PocketOpsUiState.Link -> "Bookmarks"
                            PocketOpsUiState.Web -> "Web Search"
                            PocketOpsUiState.YtExplorer -> "YT Explorer"
                            PocketOpsUiState.LocalSave -> "Local Save"
                            PocketOpsUiState.Settings -> "Settings"
                            is PocketOpsUiState.Setup,
                            is PocketOpsUiState.EnterAmount,
                            is PocketOpsUiState.ShowQr -> "Quick Collect"
                            else -> "PocketOps"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier.size(width = 80.dp, height = 40.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (uiState == PocketOpsUiState.Dashboard) {
                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_settings),
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else if (uiState == PocketOpsUiState.WhatsApp && !showChatSettings) {
                            IconButton(
                                onClick = { onToggleChatSettings(true) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_tools),
                                    contentDescription = "Chat Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else if (uiState is PocketOpsUiState.Setup || uiState is PocketOpsUiState.EnterAmount || uiState is PocketOpsUiState.ShowQr) {
                            // Payment Mode Switcher in the top-right box of Quick Pay screens
                            PaymentModeSwitcherButton(
                                usePaypal = usePaypal,
                                onTogglePaypal = onTogglePaypal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Animated Screen Content ──────────────────────
                AnimatedContent(
                    targetState = uiState,
                    label = "screenTransition",
                    transitionSpec = {
                        fadeIn(animationSpec = tween(120)) togetherWith
                                fadeOut(animationSpec = tween(120)) using
                                SizeTransform(clip = false) { _, _ -> snap() }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { state ->
                    when (state) {
                        PocketOpsUiState.Dashboard ->
                            DashboardScreen(usePaypal = usePaypal, onToolSelected = onToolSelected)
                        is PocketOpsUiState.Setup ->
                            SetupScreen(
                                upiIds = upiIds,
                                defaultUpiId = defaultUpiId,
                                payeeName = payeeName,
                                usePaypal = usePaypal,
                                onSaveUpiIds = onSaveUpiIds
                            )
                        is PocketOpsUiState.EnterAmount ->
                            EnterAmountScreen(
                                recentAmounts = recentAmounts,
                                upiIds = state.upiIds,
                                defaultUpiId = state.defaultUpiId,
                                usePaypal = usePaypal,
                                onGenerateQr = onGenerateQr,
                                onManageUpiIds = onManageUpiIds
                            )
                        is PocketOpsUiState.ShowQr ->
                            ShowQrScreen(
                                amount = state.amount,
                                qrBitmap = state.qrBitmap,
                                upiId = state.upiId,
                                payeeName = state.payeeName,
                                showUpiId = showUpiId,
                                usePaypal = usePaypal,
                                onQrShown = onQrShown,
                                onRestoreBrightness = onRestoreBrightness,
                                onDismiss = onDismiss
                            )
                        PocketOpsUiState.WhatsApp ->
                            QuickChatScreen(
                                userStore = userStore,
                                mode = "WhatsApp",
                                showSettings = showChatSettings,
                                onToggleSettings = onToggleChatSettings,
                                selectingCountry = selectingCountry,
                                onToggleSelectingCountry = onToggleSelectingCountry,
                                onDismiss = onDismiss
                            )
                        PocketOpsUiState.Telegram ->
                            QuickChatScreen(
                                userStore = userStore,
                                mode = "Telegram",
                                showSettings = showChatSettings,
                                onToggleSettings = onToggleChatSettings,
                                selectingCountry = selectingCountry,
                                onToggleSelectingCountry = onToggleSelectingCountry,
                                onDismiss = onDismiss
                            )
                        PocketOpsUiState.SMS ->
                            QuickChatScreen(
                                userStore = userStore,
                                mode = "SMS",
                                showSettings = showChatSettings,
                                onToggleSettings = onToggleChatSettings,
                                selectingCountry = selectingCountry,
                                onToggleSelectingCountry = onToggleSelectingCountry,
                                onDismiss = onDismiss
                            )
                        PocketOpsUiState.SocialProfiler ->
                            SocialProfilerScreen(
                                onDismiss = { onDismiss() }
                            )
                        PocketOpsUiState.Clipboard ->
                            QuickClipScreen(
                                userStore = userStore,
                                onDismiss = onDismiss
                            )
                        PocketOpsUiState.Link -> QuickLinkScreen(userStore = userStore)
                        PocketOpsUiState.Web -> QuickWebScreen()
                        PocketOpsUiState.YtExplorer -> YtExplorerScreen()
                        PocketOpsUiState.LocalSave ->
                            LocalSaveScreen(
                                sharedFileUri = sharedFileUri,
                                sharedText = sharedText,
                                onClearShared = onClearShared,
                                onDismiss = onDismiss
                            )
                        PocketOpsUiState.Settings ->
                            SettingsScreen(
                                themeMode = themeMode,
                                dynamicColor = dynamicColor,
                                isLoggingActive = isLoggingActive,
                                onToggleLogging = onToggleLogging,
                                onChangeThemeMode = onChangeThemeMode,
                                onToggleDynamicColor = onToggleDynamicColor,
                                onExport = onExport,
                                onImport = onImport
                            )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_iixii),
                    contentDescription = "IIXII Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(width = 80.dp, height = 16.dp)
                )
            }
        }
    }
}

@Composable
fun PaymentModeSwitcherButton(
    usePaypal: Boolean,
    onTogglePaypal: (Boolean) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .size(width = 44.dp, height = 32.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onTogglePaypal(!usePaypal) }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            AnimatedContent(
                targetState = usePaypal,
                transitionSpec = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                            fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                },
                label = "paymentModeTransition"
            ) { activePaypal ->
                Icon(
                    painter = painterResource(if (activePaypal) R.drawable.ic_paypal else R.drawable.ic_upi_pay),
                    contentDescription = "Switch Payment Mode",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun UpdateTag() {
    val context = LocalContext.current
    val currentVersionName = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "v${packageInfo.versionName}"
        } catch (e: Exception) {
            "v2.1.8"
        }
    }

    val state = UpdateManager.updateState
    val hasLocalApk = UpdateManager.hasLocalApk

    when (state) {
        UpdateState.Idle -> {
            if (hasLocalApk) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .wrapContentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            UpdateManager.deleteDownloadedApks(context)
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "APK",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = "Delete APKs",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = currentVersionName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        is UpdateState.UpdateAvailable -> {
            Surface(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        UpdateManager.startDownload(
                            context,
                            state.apkUrl,
                            state.versionName
                        )
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "v${state.versionName}",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_cloud_download),
                        contentDescription = "Update Available",
                        tint = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        is UpdateState.Downloading -> {
            Surface(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "v${state.versionName}",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    CircularProgressIndicator(
                        progress = { state.progress / 100f },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        is UpdateState.ReadyToInstall -> {
            Surface(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        UpdateManager.installApk(context, state.fileName)
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "v${state.versionName}",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_check_circle_fill),
                        contentDescription = "Install Update",
                        tint = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}



private fun fetchLinkPreview(rawUrl: String): Pair<String, String?> {
    return try {
        val connection = (URL(rawUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 3500
            readTimeout = 3500
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "PocketOps/3.0")
        }
        val html = connection.inputStream.bufferedReader().use { it.readText().take(200_000) }
        val title = Regex("<meta[^>]+property=[\"']og:title[\"'][^>]+content=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.get(1)
            ?: Regex("<title[^>]*>(.*?)</title>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                .find(html)?.groupValues?.get(1)?.replace(Regex("\\s+"), " ")?.trim()
            ?: rawUrl
        val icon = Regex("<link[^>]+(?:rel=[\"'][^\"']*icon[^\"']*[\"'][^>]*href=[\"']([^\"']+)[\"']|href=[\"']([^\"']+)[\"'][^>]*rel=[\"'][^\"']*icon[^\"']*[\"'])", RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.drop(1)?.firstOrNull { it.isNotBlank() }
            ?.let { href -> URL(URL(rawUrl), href).toString() }
        title to icon
    } catch (e: Exception) {
        rawUrl to null
    }
}


