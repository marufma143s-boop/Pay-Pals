package com.example.screens.tasks

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.ServiceDisabledDialog
import com.example.repository.AppRepository
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen

@Composable
fun VisitEarnScreen(
    repository: AppRepository,
    onNavigateToAdstra: () -> Unit,
    onNavigateToBlogger: () -> Unit,
    onNavigateToMonetag: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val adstraCount by repository.adstraCount.collectAsState()
    val bloggerCount by repository.bloggerCount.collectAsState()
    val monetagCount by repository.monetagCount.collectAsState()
    val walletState by repository.walletState.collectAsState()
    val serviceSettings by repository.serviceControlSettings.collectAsState()

    var disabledServiceDialogInfo by remember { mutableStateOf<Pair<String, String>?>(null) }

    val isAdstraDisabled = serviceSettings.isServiceDisabled("adsterra")
    val isBloggerDisabled = serviceSettings.isServiceDisabled("blogger")
    val isMonetagDisabled = serviceSettings.isServiceDisabled("monetag")

    val totalVisitsCompleted = adstraCount + bloggerCount + monetagCount
    val totalLimit = repository.adstraLimit + repository.bloggerLimit + repository.monetagLimit

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header for Visit Earn
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Visit & Earn",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Browse partner links & collect instant Credits",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PurplePrimary.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleNeon.copy(alpha = 0.5f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🪙 ${walletState.balance.toInt()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Overview Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = PurplePrimary),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        PurplePrimary.copy(alpha = 0.15f),
                                        Color(0xFF280B4D).copy(alpha = 0.3f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Today's Visit Activity",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$totalVisitsCompleted of $totalLimit Total Daily Visits Done",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GoldAccent.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "24h Reset",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section Label
            item {
                Text(
                    text = "Select Network to Visit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Adstra Card
            item {
                NetworkEarningCard(
                    name = "Adstra Earning",
                    subtitle = if (isAdstraDisabled) "Closed for maintenance" else "High CPM Partner Visits",
                    rewardText = "🪙 25 Credits / Visit",
                    count = adstraCount,
                    limit = repository.adstraLimit,
                    icon = Icons.Filled.AdsClick,
                    gradientColors = if (isAdstraDisabled) listOf(Color.Gray, Color.DarkGray) else listOf(Color(0xFFE65100), Color(0xFFFF9800)),
                    isDisabled = isAdstraDisabled,
                    onClick = {
                        if (isAdstraDisabled) {
                            disabledServiceDialogInfo = Pair("Adstra Earning", serviceSettings.getServiceReason("adsterra"))
                        } else {
                            onNavigateToAdstra()
                        }
                    }
                )
            }

            // Blogger Card
            item {
                NetworkEarningCard(
                    name = "Blogger Earning",
                    subtitle = if (isBloggerDisabled) "Closed for maintenance" else "Article & Blog Reading Visits",
                    rewardText = "🪙 20 Credits / Visit",
                    count = bloggerCount,
                    limit = repository.bloggerLimit,
                    icon = Icons.Filled.Public,
                    gradientColors = if (isBloggerDisabled) listOf(Color.Gray, Color.DarkGray) else listOf(Color(0xFFE91E63), Color(0xFFFF4081)),
                    isDisabled = isBloggerDisabled,
                    onClick = {
                        if (isBloggerDisabled) {
                            disabledServiceDialogInfo = Pair("Blogger Earning", serviceSettings.getServiceReason("blogger"))
                        } else {
                            onNavigateToBlogger()
                        }
                    }
                )
            }

            // Monetag Card
            item {
                NetworkEarningCard(
                    name = "Monetag Earning",
                    subtitle = if (isMonetagDisabled) "Closed for maintenance" else "Global Direct Link Visits",
                    rewardText = "🪙 30 Credits / Visit",
                    count = monetagCount,
                    limit = repository.monetagLimit,
                    icon = Icons.Filled.MonetizationOn,
                    gradientColors = if (isMonetagDisabled) listOf(Color.Gray, Color.DarkGray) else listOf(Color(0xFF00897B), Color(0xFF00E676)),
                    isDisabled = isMonetagDisabled,
                    onClick = {
                        if (isMonetagDisabled) {
                            disabledServiceDialogInfo = Pair("Monetag Earning", serviceSettings.getServiceReason("monetag"))
                        } else {
                            onNavigateToMonetag()
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (disabledServiceDialogInfo != null) {
            val (title, reason) = disabledServiceDialogInfo!!
            ServiceDisabledDialog(
                serviceTitle = title,
                reason = reason,
                onDismiss = { disabledServiceDialogInfo = null }
            )
        }
    }
}

@Composable
fun NetworkEarningCard(
    name: String,
    subtitle: String,
    rewardText: String,
    count: Int,
    limit: Int,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    isDisabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (limit > 0) (count.toFloat() / limit.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(400),
        label = "progress"
    )
    val percentage = (progressFraction * 100).toInt()
    val isCompleted = count >= limit

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (isDisabled) 1.dp else 6.dp, RoundedCornerShape(20.dp), spotColor = PurplePrimary)
            .clickable { onClick() }
            .testTag("network_card_${name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDisabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Vector Gradient Icon Badge
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDisabled) Color.Red.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Reward Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDisabled) Color.Gray.copy(alpha = 0.2f) else GoldAccent.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDisabled) Color.Gray else GoldAccent.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (isDisabled) "Closed" else rewardText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDisabled) Color.Gray else GoldAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Limit and Pipe Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Limit: $count / $limit Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isCompleted) "Completed" else "$percentage%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) SuccessGreen else PurpleNeon,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Pipe
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush = if (isCompleted) {
                                    Brush.horizontalGradient(
                                        listOf(SuccessGreen, Color(0xFF00E676))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(PurplePrimary, PurpleNeon)
                                    )
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDisabled) "Service temporarily closed" else if (isCompleted) "Daily limit reached" else "Tap to start browsing & earn",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Start",
                    tint = if (isDisabled || isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else PurpleNeon,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

