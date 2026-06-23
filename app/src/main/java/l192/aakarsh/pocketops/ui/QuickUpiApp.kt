package l192.aakarsh.pocketops.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import l192.aakarsh.pocketops.PocketOpsTheme
import l192.aakarsh.pocketops.R
import l192.aakarsh.pocketops.data.UserStore
import l192.aakarsh.pocketops.ui.screens.DashboardScreen
import l192.aakarsh.pocketops.ui.screens.EnterAmountScreen
import l192.aakarsh.pocketops.ui.screens.QuickInstaScreen
import l192.aakarsh.pocketops.ui.screens.QuickTool
import l192.aakarsh.pocketops.ui.screens.QuickWhatsAppScreen
import l192.aakarsh.pocketops.ui.screens.SettingsScreen
import l192.aakarsh.pocketops.ui.screens.SetupScreen
import l192.aakarsh.pocketops.ui.screens.ShowQrScreen
import l192.aakarsh.pocketops.utils.QRCodeGenerator
import kotlinx.coroutines.launch

sealed interface QuickUpiUiState {
    data object Dashboard : QuickUpiUiState
    data object Setup : QuickUpiUiState
    data class EnterAmount(val upiIds: List<String>) : QuickUpiUiState
    data class ShowQr(
        val amount: String, val qrBitmap: Bitmap, val upiId: String, val payeeName: String
    ) : QuickUpiUiState

    data object Settings : QuickUpiUiState
    data object WhatsApp : QuickUpiUiState
    data object Instagram : QuickUpiUiState
}

@Composable
fun QuickUpiApp(
    userStore: UserStore,
    onQrShown: () -> Unit = {},
    onRestoreBrightness: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {

    val savedUpiIds by userStore.upiIds.collectAsState(initial = emptyList())

    val savedPayeeName by userStore.payeeName.collectAsState(initial = null)
    val recentAmounts by userStore.recentAmounts.collectAsState(initial = emptyList())
    val showUpiId by userStore.showUpiId.collectAsState(initial = true)

    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf<QuickUpiUiState>(QuickUpiUiState.Dashboard) }

    val currentTool = when (uiState) {
        QuickUpiUiState.WhatsApp -> QuickTool.WHATSAPP
        QuickUpiUiState.Instagram -> QuickTool.INSTAGRAM
        is QuickUpiUiState.EnterAmount, is QuickUpiUiState.ShowQr -> QuickTool.UPI
        else -> null
    }

    PocketOpsTheme(tool = currentTool) {
        QuickUpiContent(
            uiState = uiState,
            recentAmounts = recentAmounts,
            upiIds = savedUpiIds,
            showUpiId = showUpiId,
            onSaveUpiIds = { upiIds, name ->
                scope.launch {
                    userStore.saveUpiIds(upiIds)
                    userStore.savePayeeName(name)
                    if (upiIds.isNotEmpty()) {
                        uiState = QuickUpiUiState.EnterAmount(upiIds)
                    }
                }
            }, onGenerateQr = { amount, note, selectedUpiId ->
                if (amount.isNotBlank()) {
                    scope.launch {
                        userStore.saveRecentAmount(amount)
                    }
                }

                val uriBuilder =
                    Uri.Builder().scheme("upi").authority("pay")
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

                val bitmap = QRCodeGenerator.generateQRCode(
                    payeeURL.toString(), 1024, 1024
                )

                uiState = QuickUpiUiState.ShowQr(
                    amount, bitmap, selectedUpiId, savedPayeeName ?: ""
                )
            }, onResetUpi = {
                uiState = QuickUpiUiState.Setup
            }, onToggleShowUpiId = { show ->
                scope.launch {
                    userStore.saveShowUpiId(show)
                }
            }, onSettingsClick = {
                uiState = QuickUpiUiState.Settings
            }, onBackToHome = {
                uiState = QuickUpiUiState.Dashboard
            }, onToolSelected = { tool ->
                uiState = when (tool) {
                    QuickTool.UPI -> {
                        if (savedUpiIds.isEmpty()) {
                            QuickUpiUiState.Setup
                        } else {
                            QuickUpiUiState.EnterAmount(savedUpiIds)
                        }
                    }
                    QuickTool.WHATSAPP -> QuickUpiUiState.WhatsApp
                    QuickTool.INSTAGRAM -> QuickUpiUiState.Instagram
                }
            }, onQrShown = onQrShown, onRestoreBrightness = onRestoreBrightness, onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickUpiContent(
    uiState: QuickUpiUiState,
    recentAmounts: List<String> = emptyList(),
    upiIds: List<String> = emptyList(),
    showUpiId: Boolean = true,


    onSaveUpiIds: (List<String>, String) -> Unit,
    onGenerateQr: (String, String, String) -> Unit,
    onResetUpi: () -> Unit,
    onToggleShowUpiId: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    onBackToHome: () -> Unit,
    onToolSelected: (QuickTool) -> Unit = {},
    onQrShown: () -> Unit = {},
    onRestoreBrightness: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {

    BasicAlertDialog(
        onDismissRequest = { onDismiss() }, properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        )
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            tonalElevation = AlertDialogDefaults.TonalElevation,
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {


                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState != QuickUpiUiState.Dashboard) {
                        IconButton(
                            onClick = onBackToHome,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    }

                    Text(
                        text = when (uiState) {
                            QuickUpiUiState.Settings -> "Settings"
                            QuickUpiUiState.WhatsApp -> "Quick WhatsApp"
                            QuickUpiUiState.Instagram -> "Quick Insta"
                            else -> "PocketOps"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    if (uiState is QuickUpiUiState.EnterAmount || uiState is QuickUpiUiState.Dashboard) {
                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_settings),
                                contentDescription = "Settings"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))


                when (uiState) {
                    QuickUpiUiState.Dashboard -> {
                        DashboardScreen(onToolSelected = onToolSelected)
                    }

                    QuickUpiUiState.Setup -> {
                        SetupScreen(
                            upiIds = upiIds,
                            onSaveUpiIds = onSaveUpiIds,
                        )
                    }

                    is QuickUpiUiState.EnterAmount -> {
                        EnterAmountScreen(
                            recentAmounts = recentAmounts,
                            upiIds = uiState.upiIds,
                            onGenerateQr = onGenerateQr,
                            onResetUpi = onResetUpi
                        )
                    }

                    is QuickUpiUiState.ShowQr -> {
                        ShowQrScreen(
                            amount = uiState.amount,
                            qrBitmap = uiState.qrBitmap,
                            upiId = uiState.upiId,
                            payeeName = uiState.payeeName,
                            showUpiId = showUpiId,
                            onQrShown = onQrShown,
                            onRestoreBrightness = onRestoreBrightness,
                            onDismiss = onDismiss
                        )
                    }

                    QuickUpiUiState.Settings -> {
                        SettingsScreen(
                            showUpiId = showUpiId,
                            onToggleShowUpiId = onToggleShowUpiId
                        )
                    }

                    QuickUpiUiState.WhatsApp -> {
                        QuickWhatsAppScreen(onDismiss = onDismiss)
                    }

                    QuickUpiUiState.Instagram -> {
                        QuickInstaScreen(onDismiss = onDismiss)
                    }
                }
            }
        }
    }
}


