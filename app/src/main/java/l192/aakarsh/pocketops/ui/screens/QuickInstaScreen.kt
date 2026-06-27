package l192.aakarsh.pocketops.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import l192.aakarsh.pocketops.R

@Composable
fun QuickInstaScreen(
    onDismiss: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    val context = LocalContext.current

    val cleanedUsername = username.trim().removePrefix("@").trim()
    val isValid = cleanedUsername.isNotEmpty() && !cleanedUsername.contains(" ")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Search an Instagram profile by username — opens directly in the app if installed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Instagram Username", maxLines = 1) },
            placeholder = { Text("anshu07.192") },
            shape = RoundedCornerShape(16.dp),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_instagram),
                    contentDescription = "Username",
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (username.isNotEmpty()) {
                    IconButton(onClick = { username = "" }) {
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

        // Open Profile button
        Button(
            onClick = {
                if (isValid) {
                    val appUri = Uri.parse("http://instagram.com/_u/$cleanedUsername")
                    val browserUri = Uri.parse("https://instagram.com/$cleanedUsername")

                    val intent = Intent(Intent.ACTION_VIEW, appUri).apply {
                        setPackage("com.instagram.android")
                    }

                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                    }
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
                painter = painterResource(R.drawable.ic_instagram),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Open Profile",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Direct Search button
        OutlinedButton(
            onClick = {
                val appUri = Uri.parse("instagram://search")
                val browserUri = Uri.parse("https://instagram.com/explore/")
                val intent = Intent(Intent.ACTION_VIEW, appUri).apply {
                    setPackage("com.instagram.android")
                }
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                }
                onDismiss()
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_instagram),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Direct Search on Insta",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
