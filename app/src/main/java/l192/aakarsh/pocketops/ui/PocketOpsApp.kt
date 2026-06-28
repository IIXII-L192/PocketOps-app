package l192.aakarsh.pocketops.ui

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import l192.aakarsh.pocketops.R
import l192.aakarsh.pocketops.data.UserStore
import l192.aakarsh.pocketops.ui.screens.DashboardScreen
import l192.aakarsh.pocketops.ui.screens.EnterAmountScreen
import l192.aakarsh.pocketops.ui.screens.QuickChatScreen
import l192.aakarsh.pocketops.ui.screens.QuickInstaScreen
import l192.aakarsh.pocketops.ui.screens.QuickTool
import l192.aakarsh.pocketops.ui.screens.SetupScreen
import l192.aakarsh.pocketops.ui.screens.ShowQrScreen
import l192.aakarsh.pocketops.ui.screens.SettingsScreen
import l192.aakarsh.pocketops.utils.QRCodeGenerator
import l192.aakarsh.pocketops.utils.UpdateManager
import l192.aakarsh.pocketops.utils.UpdateState

sealed interface PocketOpsUiState {
    data object Dashboard : PocketOpsUiState
    data object Setup : PocketOpsUiState
    data class EnterAmount(val upiIds: List<String>, val defaultUpiId: String) : PocketOpsUiState
    data class ShowQr(
        val amount: String, val qrBitmap: Bitmap, val upiId: String, val payeeName: String
    ) : PocketOpsUiState

    data object WhatsApp : PocketOpsUiState
    data object Instagram : PocketOpsUiState
    data object Settings : PocketOpsUiState
}

@Composable
fun PocketOpsApp(
    userStore: UserStore,
    shortcutAction: String? = null,
    themeMode: String = "SYSTEM",
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
    var uiState by remember { mutableStateOf<PocketOpsUiState>(PocketOpsUiState.Dashboard) }

    LaunchedEffect(Unit) {
        UpdateManager.checkForUpdates(context)
    }

    LaunchedEffect(savedUpiIds, savedPaypalIds, savedDefaultUpiId, savedDefaultPaypalId, shortcutAction, usePaypal) {
        if (shortcutAction != null) {
            when (shortcutAction) {
                "l192.aakarsh.pocketops.ACTION_QUICK_UPI", "l192.aakarsh.pocketops.ACTION_SHOW_QR" -> {
                    uiState = if (activeIds.isNotEmpty()) {
                        PocketOpsUiState.EnterAmount(activeIds, activeDefaultId ?: activeIds.first())
                    } else {
                        PocketOpsUiState.Setup
                    }
                }
                "l192.aakarsh.pocketops.ACTION_QUICK_CHAT" -> {
                    uiState = PocketOpsUiState.WhatsApp
                }
                "l192.aakarsh.pocketops.ACTION_QUICK_INSTA" -> {
                    uiState = PocketOpsUiState.Instagram
                }
            }
        }
    }

    PocketOpsContent(
        uiState = uiState,
        recentAmounts = recentAmounts,
        upiIds = activeIds,
        defaultUpiId = activeDefaultId,
        showUpiId = showUpiId,
        themeMode = themeMode,
        usePaypal = usePaypal,
        onChangeThemeMode = onChangeThemeMode,
        onTogglePaypal = { use ->
            scope.launch {
                userStore.saveUsePaypal(use)
                // Dynamically route setups depending on target config availability
                val targetIds = if (use) savedPaypalIds else savedUpiIds
                val targetDefault = if (use) savedDefaultPaypalId else savedDefaultUpiId
                if (uiState is PocketOpsUiState.Setup || uiState is PocketOpsUiState.EnterAmount || uiState is PocketOpsUiState.ShowQr) {
                    uiState = if (targetIds.isEmpty()) {
                        PocketOpsUiState.Setup
                    } else {
                        PocketOpsUiState.EnterAmount(targetIds, targetDefault ?: targetIds.first())
                    }
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
                if (ids.isNotEmpty()) {
                    uiState = PocketOpsUiState.EnterAmount(ids, defaultId)
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
                uiState = PocketOpsUiState.ShowQr(
                    amount, bitmap, selectedId, savedPayeeName ?: ""
                )
            }
        },
        onManageUpiIds = { uiState = PocketOpsUiState.Setup },
        onBackToHome = { uiState = PocketOpsUiState.Dashboard },
        onOpenSettings = { uiState = PocketOpsUiState.Settings },
        onToolSelected = { tool ->
            uiState = when (tool) {
                QuickTool.UPI -> {
                    if (activeIds.isEmpty()) PocketOpsUiState.Setup
                    else PocketOpsUiState.EnterAmount(activeIds, activeDefaultId ?: activeIds.first())
                }
                QuickTool.WHATSAPP -> PocketOpsUiState.WhatsApp
                QuickTool.INSTAGRAM -> PocketOpsUiState.Instagram
            }
        },
        onQrShown = onQrShown,
        onRestoreBrightness = onRestoreBrightness,
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketOpsContent(
    uiState: PocketOpsUiState,
    recentAmounts: List<String> = emptyList(),
    upiIds: List<String> = emptyList(),
    defaultUpiId: String? = null,
    showUpiId: Boolean = true,
    themeMode: String = "SYSTEM",
    usePaypal: Boolean = false,
    onChangeThemeMode: (String) -> Unit = {},
    onTogglePaypal: (Boolean) -> Unit = {},
    onSaveUpiIds: (List<String>, String, String) -> Unit,
    onGenerateQr: (String, String, String) -> Unit,
    onManageUpiIds: () -> Unit,
    onBackToHome: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onToolSelected: (QuickTool) -> Unit = {},
    onQrShown: () -> Unit = {},
    onRestoreBrightness: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    BasicAlertDialog(
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
                    .padding(24.dp)
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
                            PocketOpsUiState.WhatsApp -> "Quick Chat"
                            PocketOpsUiState.Instagram -> "Quick Insta"
                            PocketOpsUiState.Settings -> "Settings"
                            PocketOpsUiState.Setup,
                            is PocketOpsUiState.EnterAmount,
                            is PocketOpsUiState.ShowQr -> "Quick Pay"
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
                    transitionSpec = {
                        (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                scaleIn(initialScale = 0.96f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                            .togetherWith(
                                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                        scaleOut(targetScale = 0.96f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                            )
                    },
                    label = "screenTransition",
                    modifier = Modifier.fillMaxWidth()
                ) { state ->
                    when (state) {
                        PocketOpsUiState.Dashboard ->
                            DashboardScreen(usePaypal = usePaypal, onToolSelected = onToolSelected)
                        PocketOpsUiState.Setup ->
                            SetupScreen(
                                upiIds = upiIds,
                                defaultUpiId = defaultUpiId,
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
                            QuickChatScreen(onDismiss = onDismiss)
                        PocketOpsUiState.Instagram ->
                            QuickInstaScreen(onDismiss = onDismiss)
                        PocketOpsUiState.Settings ->
                            SettingsScreen(
                                themeMode = themeMode,
                                onChangeThemeMode = onChangeThemeMode
                            )
                    }
                }

                // ── Footer Button ────────────────────────────────
                val context = LocalContext.current
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bmad192.vercel.app/"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Support Me", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PaymentModeSwitcherButton(
    usePaypal: Boolean,
    onTogglePaypal: (Boolean) -> Unit
) {
    IconButton(
        onClick = { onTogglePaypal(!usePaypal) },
        modifier = Modifier.size(36.dp)
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
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

    when (state) {
        UpdateState.Idle -> {
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
                            state.releaseUrl,
                            "PocketOps-v${state.versionName}.apk"
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
                        painter = painterResource(R.drawable.ic_arrow_up_update),
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
                        text = "Downloading...",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(14.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { state.progress / 100f },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            strokeWidth = 1.5.dp,
                            modifier = Modifier.size(14.dp)
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_up_update),
                            contentDescription = "Downloading",
                            tint = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(8.dp)
                        )
                    }
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
                        text = "Install Update",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_install),
                        contentDescription = "Install Update",
                        tint = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
