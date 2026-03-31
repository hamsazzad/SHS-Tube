package com.shsshobuj.shstube.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shsshobuj.shstube.R
import com.shsshobuj.shstube.config.SecretConfig
import com.shsshobuj.shstube.ui.theme.CopperOrange
import com.shsshobuj.shstube.ui.theme.MetallicBlue
import com.shsshobuj.shstube.ui.theme.ObsidianBlack
import com.shsshobuj.shstube.ui.theme.SurfaceDark
import com.shsshobuj.shstube.ui.theme.SurfaceDarker

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Developer Profile Header ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D1B35),
                            ObsidianBlack
                        )
                    )
                )
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Developer Profile Image (dev_profile.jpg)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(3.dp, brush = Brush.linearGradient(
                            colors = listOf(MetallicBlue, CopperOrange)
                        ), CircleShape)
                ) {
                    AsyncImage(
                        model = R.drawable.dev_profile,
                        contentDescription = "Developer Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.ic_dev_placeholder),
                        placeholder = painterResource(R.drawable.ic_dev_placeholder)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = SecretConfig.DEV_NAME,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Developer & Creator",
                    style = MaterialTheme.typography.bodySmall,
                    color = CopperOrange
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SHS Tube v${SecretConfig.APP_VERSION}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF555566)
                )
            }
        }

        // ── About App Card ─────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "About SHS Tube",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "A premium media downloader powered by yt-dlp. Browse any website, detect media automatically, and download videos, audio & streams in the best quality.",
                    color = Color(0xFF8888AA),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }
        }

        // ── SHS Tube Premium Support Section ──────────────────────────────────
        Text(
            text = "SHS Tube Premium Support",
            color = CopperOrange,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        // bKash Payment Card
        BkashCard(
            paymentNumber = SecretConfig.BKASH_PAYMENT_NUMBER,
            context = context
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Social Links
        SocialLinksCard(context = context)

        Spacer(modifier = Modifier.height(24.dp))

        // Footer
        Text(
            text = "Made with ❤ by ${SecretConfig.DEV_NAME}",
            color = Color(0xFF444455),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BkashCard(paymentNumber: String, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A0A1E)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.linearGradient(listOf(Color(0xFFE2156A), Color(0xFF9B1042)))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // bKash logo color block
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFE2156A), Color(0xFF9B1042))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "bK",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "bKash",
                        color = Color(0xFFE2156A),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "Support the Developer",
                        color = Color(0xFF888888),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFF3A1030))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Payment Number",
                        color = Color(0xFF888888),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        paymentNumber,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 1.sp
                    )
                }

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("bKash Number", paymentNumber))
                        Toast.makeText(context, "Number copied!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE2156A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Send Money / Payment • Number ki copy kore paste korun",
                color = Color(0xFF664466),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun SocialLinksCard(context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Connect with Developer",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Telegram Button
            SocialButton(
                label = "Telegram Channel",
                subtitle = SecretConfig.TELEGRAM_CHANNEL_URL.removePrefix("https://"),
                backgroundColor = Color(0xFF0088CC),
                icon = Icons.Filled.Send,
                onClick = {
                    openUrl(context, SecretConfig.TELEGRAM_CHANNEL_URL)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Facebook Button
            SocialButton(
                label = "Facebook Profile",
                subtitle = "Follow on Facebook",
                backgroundColor = Color(0xFF1877F2),
                icon = Icons.Filled.Facebook,
                onClick = {
                    openUrl(context, SecretConfig.FACEBOOK_PROFILE_URL)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email Button
            SocialButton(
                label = "Email Support",
                subtitle = SecretConfig.SUPPORT_EMAIL,
                backgroundColor = Color(0xFFEA4335),
                icon = Icons.Filled.Email,
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${SecretConfig.SUPPORT_EMAIL}")
                        putExtra(Intent.EXTRA_SUBJECT, "SHS Tube Support")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
fun SocialButton(
    label: String,
    subtitle: String,
    backgroundColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor.copy(alpha = 0.12f))
            .border(1.dp, backgroundColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = subtitle,
                color = Color(0xFF888888),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }

        Icon(
            Icons.Filled.Send,
            null,
            tint = backgroundColor,
            modifier = Modifier.size(16.dp)
        )
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
    }
}
