package com.example.screens.account

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.components.SubScreenTopBar
import com.example.repository.AppRepository
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.WalletGradientBrush
import com.example.utils.Base64OrResourceImage

@Composable
fun DeveloperProfileScreen(
    repository: AppRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val developerInfo by repository.developerInfo.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("developer_profile_screen")
    ) {
        SubScreenTopBar(
            title = "Developer Profile",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = PurplePrimary),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WalletGradientBrush)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                            .padding(22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(96.dp)
                                    .border(3.dp, PurpleNeon, CircleShape),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Base64OrResourceImage(
                                    base64Str = developerInfo.avatarBase64,
                                    placeholderRes = R.drawable.img_avatar_maruf_1787554123074,
                                    contentDescription = developerInfo.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = developerInfo.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = developerInfo.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = PurpleNeon,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Specialized in modern Jetpack Compose development, high-security fintech architectures, reactive state engines, and intuitive user experiences.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Comprehensive Description Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "About the Developer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = developerInfo.description.ifEmpty {
                                "${developerInfo.name} is a seasoned Mobile Architect with a proven track record in architecting high-performance financial, campaign management, and digital reward platforms.\n\nWith a deep focus on modern declarative UI paradigms using Jetpack Compose, reactive state architectures, and robust cloud database persistence, every component is tailored to ensure instantaneous feedback, verified task validation, and dependable digital payout lifecycles."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Contact & Connect
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Professional Inquiries",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (developerInfo.email.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:${developerInfo.email}")
                                        }
                                        runCatching { context.startActivity(intent) }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Email, contentDescription = "Email", tint = PurplePrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(developerInfo.email, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        if (developerInfo.phone.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${developerInfo.phone}")
                                        }
                                        runCatching { context.startActivity(intent) }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Phone, contentDescription = "Phone", tint = PurplePrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(developerInfo.phone, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        if (developerInfo.website.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val url = if (developerInfo.website.startsWith("http")) developerInfo.website else "https://${developerInfo.website}"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        runCatching { context.startActivity(intent) }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Language, contentDescription = "Website", tint = PurplePrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(developerInfo.website, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
