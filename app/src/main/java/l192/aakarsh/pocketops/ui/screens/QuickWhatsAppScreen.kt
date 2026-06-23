package l192.aakarsh.pocketops.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import l192.aakarsh.pocketops.R

@Composable
fun QuickWhatsAppScreen(
    onDismiss: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    val context = LocalContext.current

    val cleanedNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
    val isValid = cleanedNumber.isNotEmpty() && (cleanedNumber.startsWith("+") || cleanedNumber.length >= 10)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Enter WhatsApp Details", modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number (with Country Code)") },
            placeholder = { Text("e.g. +919876543210") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_person),
                    contentDescription = "Phone Number"
                )
            },
            trailingIcon = {
                if (phoneNumber.isNotEmpty()) {
                    IconButton(onClick = { phoneNumber = "" }) {
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isValid) {
                    var finalNum = cleanedNumber
                    if (!finalNum.startsWith("+")) {
                        finalNum = "+$finalNum"
                    }
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://api.whatsapp.com/send?phone=$finalNum")
                    }
                    context.startActivity(intent)
                    onDismiss()
                }
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Chat")
        }
    }
}


