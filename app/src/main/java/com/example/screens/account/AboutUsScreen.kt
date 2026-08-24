package com.example.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.SubScreenTopBar
import com.example.ui.theme.DarkPurpleCard
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WalletGradientBrush

@Composable
fun AboutUsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val features = listOf(
        Triple("High-Impact Campaigns", "Promote links with guaranteed visitor traffic across multiple tier packages starting from 3,000 Credits.", Icons.Filled.Campaign),
        Triple("Seamless Wallet in Credits", "Real-time deposits and fast 24h withdrawals in Bangladeshi Taka with zero hidden platform fees.", Icons.Filled.MonetizationOn),
        Triple("Daily Verified Tasks", "Earn transparent cash rewards for every completed task with a fair 50 daily limit per user.", Icons.Filled.Speed),
        Triple("Enterprise Grade Security", "Protected user credentials, encrypted payout pipes, and automated anti-fraud telemetry.", Icons.Filled.Security)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("about_us_screen")
    ) {
        SubScreenTopBar(
            title = "About Us",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Branding Card
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
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(2.dp, PurpleNeon, CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "PP",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "PayPulse",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Version 1.0.0 (Build 101)",
                                style = MaterialTheme.typography.bodySmall,
                                color = PurpleNeon
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "The premier digital platform empowering users and advertisers across Bangladesh with transparent monetization and guaranteed engagement.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Key Platform Features
            items(features.size) { index ->
                val (title, desc, icon) = features[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PurplePrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = title, tint = PurplePrimary, modifier = Modifier.size(22.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Contact & Legal Footer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "© 2024 PayPulse Technologies Inc.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "contact@paypulse.com • All Rights Reserved",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
