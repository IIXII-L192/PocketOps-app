package l192.aakarsh.pocketops.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import l192.aakarsh.pocketops.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EnterAmountScreen(
    recentAmounts: List<String>,
    upiIds: List<String>,
    defaultUpiId: String,
    usePaypal: Boolean = false,
    onGenerateQr: (String, String, String) -> Unit,
    onManageUpiIds: () -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var selectedUpiId by remember(upiIds, defaultUpiId) {
        mutableStateOf(if (upiIds.contains(defaultUpiId)) defaultUpiId else upiIds.firstOrNull() ?: "")
    }
    var expanded by remember { mutableStateOf(false) }

    val idTypeLabel = if (usePaypal) "PayPal ID" else "UPI ID"
    val idIcon = if (usePaypal) R.drawable.ic_paypal else R.drawable.ic_upi_pay
    val currencySymbol = if (usePaypal) "$" else "₹"
    val displayAmounts = remember(recentAmounts, usePaypal) {
        if (recentAmounts.isEmpty() || recentAmounts == listOf("100", "200", "500")) {
            if (usePaypal) listOf("10", "20", "50") else listOf("100", "200", "500")
        } else {
            recentAmounts
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ID Selector dropdown if there is more than 1 ID
        if (upiIds.size > 1) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                TextField(
                    value = selectedUpiId,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            painter = if (expanded) painterResource(R.drawable.ic_keyboard_arrow_up)
                            else painterResource(R.drawable.ic_keyboard_arrow_down),
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(idIcon),
                            contentDescription = idTypeLabel
                        )
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    upiIds.forEach { id ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = if (id == defaultUpiId) "$id (Default)" else id,
                                    fontWeight = if (id == defaultUpiId) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            onClick = {
                                selectedUpiId = id
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Amount validation
        val amountDouble = amountInput.toDoubleOrNull()
        val isAmountValid = amountInput.isEmpty() || (amountDouble != null && amountDouble > 0)
        val isAmountError = !isAmountValid && amountInput.isNotEmpty()

        // Amount input
        OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = { Text("Amount (Optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = {
                if (usePaypal) {
                    // Cleaner generic dollar text symbol for global PayPal payments
                    Text(
                        text = "$",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_currency_rupee),
                        contentDescription = "Amount"
                    )
                }
            },
            trailingIcon = {
                if (amountInput.isNotEmpty()) {
                    IconButton(onClick = { amountInput = "" }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            isError = isAmountError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Recent amount chips
        if (displayAmounts.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                displayAmounts.forEach { amount ->
                    SuggestionChip(
                        onClick = { amountInput = amount },
                        label = {
                            Text(
                                "$currencySymbol$amount",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Note input
        OutlinedTextField(
            value = noteInput,
            onValueChange = { noteInput = it },
            label = { Text("Note (Optional)") },
            shape = RoundedCornerShape(16.dp),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_note),
                    contentDescription = "Note"
                )
            },
            trailingIcon = {
                if (noteInput.isNotEmpty()) {
                    IconButton(onClick = { noteInput = "" }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Generate button
        Button(
            onClick = {
                if (isAmountValid) {
                    onGenerateQr(amountInput, noteInput, selectedUpiId)
                }
            },
            enabled = isAmountValid && selectedUpiId.isNotEmpty(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_qr_code),
                contentDescription = null,
                modifier = Modifier.height(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Generate QR Code",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Outlined Manage button with settings icon
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onManageUpiIds() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Manage $idTypeLabel",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
