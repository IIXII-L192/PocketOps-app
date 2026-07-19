package l192.aakarsh.pocketops.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import l192.aakarsh.pocketops.R

enum class SocialPlatform(
    val displayName: String,
    val iconRes: Int,
    val defaultUsername: String,
    val baseUrl: String,
    val packageName: String
) {
    INSTAGRAM("Instagram", R.drawable.ic_instagram, "anshu07.192", "https://instagram.com/", "com.instagram.android"),
    FACEBOOK("Facebook", R.drawable.ic_facebook, "anshu07.192", "https://facebook.com/", "com.facebook.katana"),
    THREADS("Threads", R.drawable.ic_threads, "anshu07.192", "https://threads.net/@", "com.instagram.barcelona"),
    X("X", R.drawable.ic_twitter_x, "anshu07.192", "https://x.com/", "com.twitter.android"),
    LINKEDIN("LinkedIn", R.drawable.ic_linkedin, "192aakarsh", "https://linkedin.com/in/", "com.linkedin.android")
}

@Composable
fun SocialProfilerScreen(
    onDismiss: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf(SocialPlatform.INSTAGRAM) }
    val context = LocalContext.current

    val cleanedUsername = username.trim().removePrefix("@").trim()
    val isValid = cleanedUsername.isNotEmpty() && !cleanedUsername.contains(" ")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Select a platform and enter a username to open their profile directly.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Platform selectors (Icons only)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialPlatform.values().forEach { platform ->
                val isSelected = selectedPlatform == platform
                IconButton(
                    onClick = { 
                        selectedPlatform = platform
                        username = "" // Clear username when switching platforms to show correct placeholder
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        painter = painterResource(platform.iconRes),
                        contentDescription = platform.displayName,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("${selectedPlatform.displayName} Username", maxLines = 1) },
            placeholder = { Text(selectedPlatform.defaultUsername) },
            shape = RoundedCornerShape(16.dp),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_at),
                    contentDescription = "Username Prefix",
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

        // Open Profile button with selected platform's logo
        Button(
            onClick = {
                if (isValid) {
                    val appUri = when (selectedPlatform) {
                        SocialPlatform.INSTAGRAM -> Uri.parse("http://instagram.com/_u/$cleanedUsername")
                        SocialPlatform.THREADS -> Uri.parse("barcelona://user?username=$cleanedUsername")
                        else -> Uri.parse(selectedPlatform.baseUrl + cleanedUsername)
                    }
                    val browserUri = Uri.parse(selectedPlatform.baseUrl + cleanedUsername)
                    val intent = Intent(Intent.ACTION_VIEW, appUri).apply {
                        setPackage(selectedPlatform.packageName)
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
                painter = painterResource(selectedPlatform.iconRes),
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
    }
}
