package l192.aakarsh.pocketops.ui

import android.content.Intent
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    data object WhatsApp : QuickUpiUiState
    data object Instagram : QuickUpiUiState
}

@Composable
fun QuickUpiApp(
    userStore: UserStore,
    startWithQr: Boolean = false,
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

    LaunchedEffect(savedUpiIds) {
        if (startWithQr) {
            if (savedUpiIds.isNotEmpty()) {
                uiState = QuickUpiUiState.EnterAmount(savedUpiIds)
            } else {
                uiState = QuickUpiUiState.Setup
            }
        }
    }

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


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.size(48.dp)) {
                        if (uiState != QuickUpiUiState.Dashboard) {
                            IconButton(
                                onClick = onBackToHome,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_back),
                                    contentDescription = "Back"
                                )
                            }
                        }
                    }

                    Text(
                        text = when (uiState) {
                            QuickUpiUiState.WhatsApp -> "Quick WhatsApp"
                            QuickUpiUiState.Instagram -> "Quick Insta"
                            else -> "PocketOps"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    Box(modifier = Modifier.size(48.dp))
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

                    QuickUpiUiState.WhatsApp -> {
                        QuickWhatsAppScreen(onDismiss = onDismiss)
                    }

                    QuickUpiUiState.Instagram -> {
                        QuickInstaScreen(onDismiss = onDismiss)
                    }
                }

                val context = LocalContext.current
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/sponsors/IIXII-L192")
                            )
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sponsor", style = MaterialTheme.typography.bodyMedium)
                    }

                    OutlinedButton(
                        onClick = {
                            val upiUri = Uri.Builder().scheme("upi").authority("pay")
                                .appendQueryParameter("pa", context.getString(R.string.upi_id))
                                .appendQueryParameter("pn", context.getString(R.string.upi_name))
                                .appendQueryParameter("tn", context.getString(R.string.upi_description))
                                .appendQueryParameter("cu", "INR")
                                .build()
                            val intent = Intent(Intent.ACTION_VIEW, upiUri)
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Support", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}


