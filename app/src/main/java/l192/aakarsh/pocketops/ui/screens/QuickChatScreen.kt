package l192.aakarsh.pocketops.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import l192.aakarsh.pocketops.R

@Composable
fun QuickChatScreen(
    onDismiss: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Parse country code map
    val countryCodeToIso = remember {
        mapOf(
            "1" to "US", "7" to "RU", "20" to "EG", "27" to "ZA", "30" to "GR",
            "31" to "NL", "32" to "BE", "33" to "FR", "34" to "ES", "36" to "HU",
            "39" to "IT", "40" to "RO", "41" to "CH", "43" to "AT", "44" to "GB",
            "45" to "DK", "46" to "SE", "47" to "NO", "48" to "PL", "49" to "DE",
            "51" to "PE", "52" to "MX", "53" to "CU", "54" to "AR", "55" to "BR",
            "56" to "CL", "57" to "CO", "58" to "VE", "60" to "MY", "61" to "AU",
            "62" to "ID", "63" to "PH", "64" to "NZ", "65" to "SG", "66" to "TH",
            "81" to "JP", "82" to "KR", "84" to "VN", "86" to "CN", "90" to "TR",
            "91" to "IN", "92" to "PK", "93" to "AF", "94" to "LK", "95" to "MM",
            "98" to "IR", "212" to "MA", "213" to "DZ", "216" to "TN", "218" to "LY",
            "220" to "GM", "233" to "GH", "234" to "NG", "244" to "AO", "251" to "ET",
            "254" to "KE", "255" to "TZ", "256" to "UG", "260" to "ZM", "263" to "ZW",
            "351" to "PT", "352" to "LU", "353" to "IE", "354" to "IS", "355" to "AL",
            "358" to "FI", "359" to "BG", "370" to "LT", "371" to "LV", "372" to "EE",
            "380" to "UA", "381" to "RS", "382" to "ME", "385" to "HR", "386" to "SI",
            "389" to "MK", "420" to "CZ", "421" to "SK", "501" to "BZ", "502" to "GT",
            "503" to "SV", "504" to "HN", "505" to "NI", "506" to "CR", "507" to "PA",
            "509" to "HT", "591" to "BO", "593" to "EC", "595" to "PY", "598" to "UY",
            "850" to "KP", "852" to "HK", "855" to "KH", "856" to "LA", "880" to "BD",
            "886" to "TW", "960" to "MV", "961" to "LB", "962" to "JO", "963" to "SY",
            "964" to "IQ", "965" to "KW", "966" to "SA", "967" to "YE", "968" to "OM",
            "971" to "AE", "972" to "IL", "973" to "BH", "974" to "QA", "975" to "BT",
            "976" to "MN", "977" to "NP", "992" to "TJ", "993" to "TM", "994" to "AZ",
            "995" to "GE", "996" to "KG", "998" to "UZ"
        )
    }

    // Helper to generate flag emoji from ISO
    fun getFlagEmoji(countryCode: String): String {
        if (countryCode.length != 2) return "🌐"
        try {
            val firstChar = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
            val secondChar = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
            return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
        } catch (e: Exception) {
            return "🌐"
        }
    }

    // Detect country ISO based on number prefix
    fun detectCountryIso(input: String): String {
        val cleanInput = input.trim()
        val digits = cleanInput.replace(Regex("[^0-9]"), "")
        if (cleanInput.startsWith("+")) {
            if (digits.length >= 3 && countryCodeToIso.containsKey(digits.substring(0, 3))) {
                return countryCodeToIso[digits.substring(0, 3)]!!
            }
            if (digits.length >= 2 && countryCodeToIso.containsKey(digits.substring(0, 2))) {
                return countryCodeToIso[digits.substring(0, 2)]!!
            }
            if (digits.length >= 1 && countryCodeToIso.containsKey(digits.substring(0, 1))) {
                return countryCodeToIso[digits.substring(0, 1)]!!
            }
        } else {
            // Check if digits start with a known country code and number is longer than 10 digits
            if (digits.length > 10) {
                val potentialCodeLength = digits.length - 10
                val possibleCode = digits.substring(0, potentialCodeLength)
                if (countryCodeToIso.containsKey(possibleCode)) {
                    return countryCodeToIso[possibleCode]!!
                }
            }
        }
        return "IN" // Default fallback country
    }

    // Normalize number, cleaning out spaces, brackets, hyphens, etc.
    val digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")
    val isValid = digitsOnly.length >= 10

    // Construct final parsed number to send
    val finalNumber = when {
        phoneNumber.trim().startsWith("+") -> {
            "+$digitsOnly"
        }
        digitsOnly.length == 10 -> {
            // Default to India (+91) for raw 10-digit entries
            "+91$digitsOnly"
        }
        digitsOnly.length > 10 -> {
            "+$digitsOnly"
        }
        else -> {
            "+$digitsOnly"
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Enter a phone number to start a WhatsApp chat — no need to save the contact first.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            placeholder = { Text("+91 98765-43210") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = {
                // Displays country flag emoji depending on input
                val flag = getFlagEmoji(detectCountryIso(phoneNumber))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                ) {
                    Text(text = flag, fontSize = 20.sp)
                }
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

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (isValid) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://api.whatsapp.com/send?phone=$finalNumber")
                    }
                    context.startActivity(intent)
                    onDismiss()
                }
            },
            enabled = isValid,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_whatsapp),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Start Chat",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
