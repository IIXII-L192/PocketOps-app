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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import l192.aakarsh.pocketops.R
import l192.aakarsh.pocketops.data.UserStore
import l192.aakarsh.pocketops.ui.screens.DashboardScreen
import l192.aakarsh.pocketops.ui.screens.EnterAmountScreen
import l192.aakarsh.pocketops.ui.screens.QuickChatScreen
import l192.aakarsh.pocketops.ui.screens.QuickInstaScreen
import l192.aakarsh.pocketops.ui.screens.QuickTool
import l192.aakarsh.pocketops.ui.screens.SetupScreen
import l192.aakarsh.pocketops.ui.screens.ShowQrScreen
import l192.aakarsh.pocketops.utils.QRCodeGenerator
import l192.aakarsh.pocketops.utils.UpdateManager

sealed interface PocketOpsUiState {
    data object Dashboard : PocketOpsUiState
    data object Setup : PocketOpsUiState
    data class EnterAmount(val upiIds: List<String>, val defaultUpiId: String) : PocketOpsUiState
    data class ShowQr(
        val amount: String, val qrBitmap: Bitmap, val upiId: String, val payeeName: String
    ) : PocketOpsUiState

    data object WhatsApp : PocketOpsUiState
    data object Instagram : PocketOpsUiState
}

@Composable
fun PocketOpsApp(
    userStore: UserStore,
    shortcutAction: String? = null,
    onQrShown: () -> Unit = {},
    onRestoreBrightness: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val savedUpiIds by userStore.upiIds.collectAsState(initial = emptyList())
    val savedDefaultUpiId by userStore.defaultUpiId.collectAsState(initial = null)
    val savedPayeeName by userStore.payeeName.collectAsState(initial = null)
    val recentAmounts by userStore.recentAmounts.collectAsState(initial = emptyList())
    val showUpiId by userStore.showUpiId.collectAsState(initial = true)

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var uiState by remember { mutableStateOf<PocketOpsUiState>(PocketOpsUiState.Dashboard) }

    // Start update verification immediately when PocketOps is opened
    LaunchedEffect(Unit) {
        UpdateManager.checkForUpdates(context)
    }

    LaunchedEffect(savedUpiIds, savedDefaultUpiId, shortcutAction) {
        if (shortcutAction != null) {
            when (shortcutAction) {
                "l192.aakarsh.pocketops.ACTION_QUICK_UPI", "l192.aakarsh.pocketops.ACTION_SHOW_QR" -> {
                    uiState = if (savedUpiIds.isNotEmpty()) {
                        PocketOpsUiState.EnterAmount(savedUpiIds, savedDefaultUpiId ?: savedUpiIds.first())
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
        upiIds = savedUpiIds,
        defaultUpiId = savedDefaultUpiId,
        showUpiId = showUpiId,
        onSaveUpiIds = { upiIds, name, defaultId ->
            scope.launch {
                userStore.saveUpiIds(upiIds)
                userStore.savePayeeName(name)
                userStore.saveDefaultUpiId(defaultId)
                if (upiIds.isNotEmpty()) {
                    uiState = PocketOpsUiState.EnterAmount(upiIds, defaultId)
                }
            }
        },
        onGenerateQr = { amount, note, selectedUpiId ->
            if (amount.isNotBlank()) {
                scope.launch { userStore.saveRecentAmount(amount) }
            }

            val uriBuilder = Uri.Builder().scheme("upi").authority("pay")
                .appendQueryParameter("pa", selectedUpiId)
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

            val payeeURL = uriBuilder.build()

            scope.launch {
                val bitmap = withContext(Dispatchers.Default) {
                    QRCodeGenerator.generateQRCode(context, payeeURL.toString(), 1024, 1024)
                }
                uiState = PocketOpsUiState.ShowQr(
                    amount, bitmap, selectedUpiId, savedPayeeName ?: ""
                )
            }
        },
        onManageUpiIds = { uiState = PocketOpsUiState.Setup },
        onBackToHome = { uiState = PocketOpsUiState.Dashboard },
        onToolSelected = { tool ->
            uiState = when (tool) {
                QuickTool.UPI -> {
                    if (savedUpiIds.isEmpty()) PocketOpsUiState.Setup
                    else PocketOpsUiState.EnterAmount(savedUpiIds, savedDefaultUpiId ?: savedUpiIds.first())
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
    onSaveUpiIds: (List<String>, String, String) -> Unit,
    onGenerateQr: (String, String, String) -> Unit,
    onManageUpiIds: () -> Unit,
    onBackToHome: () -> Unit,
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
                    Box(modifier = Modifier.size(40.dp)) {
                        if (uiState != PocketOpsUiState.Dashboard) {
                            IconButton(
                                onClick = onBackToHome,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_back),
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        text = when (uiState) {
                            PocketOpsUiState.WhatsApp -> "Quick Chat"
                            PocketOpsUiState.Instagram -> "Quick Insta"
                            PocketOpsUiState.Setup,
                            is PocketOpsUiState.EnterAmount,
                            is PocketOpsUiState.ShowQr -> "Quick UPI"
                            else -> "PocketOps"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    Box(modifier = Modifier.size(40.dp))
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
                            DashboardScreen(onToolSelected = onToolSelected)
                        PocketOpsUiState.Setup ->
                            SetupScreen(
                                upiIds = upiIds,
                                defaultUpiId = defaultUpiId,
                                onSaveUpiIds = onSaveUpiIds
                            )
                        is PocketOpsUiState.EnterAmount ->
                            EnterAmountScreen(
                                recentAmounts = recentAmounts,
                                upiIds = state.upiIds,
                                defaultUpiId = state.defaultUpiId,
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
                                onQrShown = onQrShown,
                                onRestoreBrightness = onRestoreBrightness,
                                onDismiss = onDismiss
                            )
                        PocketOpsUiState.WhatsApp ->
                            QuickChatScreen(onDismiss = onDismiss)
                        PocketOpsUiState.Instagram ->
                            QuickInstaScreen(onDismiss = onDismiss)
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
                    Text("Support", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun PocketOpsUiState.order(): Int = when (this) {
    PocketOpsUiState.Dashboard -> 0
    PocketOpsUiState.Setup -> 1
    is PocketOpsUiState.EnterAmount -> 2
    is PocketOpsUiState.ShowQr -> 3
    PocketOpsUiState.WhatsApp -> 4
    PocketOpsUiState.Instagram -> 5
}
