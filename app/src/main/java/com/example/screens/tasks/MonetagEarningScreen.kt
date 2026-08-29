package com.example.screens.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.SubScreenTopBar
import com.example.repository.AppRepository
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.utils.CustomTabsHelper
import kotlinx.coroutines.delay

@Composable
fun MonetagEarningScreen(
    repository: AppRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val count by repository.monetagCount.collectAsState()
    val adwardSettings by repository.adwardSettings.collectAsState()
    val config = adwardSettings.monetagConfig
    val limit = config.dailyLimit
    val preferredBrowser by repository.preferredBrowser.collectAsState()
    val monetagBreakUntil by repository.monetagBreakUntil.collectAsState()

    var isVisitingInProgress by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableIntStateOf(config.visitDurationSeconds) }
    var canClaimReward by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var rewardEarned by remember { mutableStateOf(0.0) }
    var visitStartTime by remember { mutableLongStateOf(0L) }
    var earlyExitWarning by remember { mutableStateOf<String?>(null) }
    var breakRemainingSec by remember { mutableLongStateOf(repository.getBreakRemainingSeconds("monetag")) }

    val isLimitReached = count >= limit
    val isOnBreak = breakRemainingSec > 0L

    // Live break countdown timer
    LaunchedEffect(monetagBreakUntil) {
        while (true) {
            val rem = repository.getBreakRemainingSeconds("monetag")
            breakRemainingSec = rem
            if (rem <= 0L) break
            delay(1000L)
        }
    }

    val targetInfo = remember(isVisitingInProgress, count) { repository.getTargetLinkForVisit("monetag") }

    // Countdown Timer for Monetag Offer
    LaunchedEffect(isVisitingInProgress) {
        if (isVisitingInProgress) {
            secondsRemaining = config.visitDurationSeconds
            canClaimReward = false
            while (secondsRemaining > 0) {
                delay(1000L)
                secondsRemaining -= 1
            }
            canClaimReward = true
        }
    }

    val browserName = if (preferredBrowser.equals("firefox", ignoreCase = true)) "Mozilla Firefox" else "Google Chrome"
    val browserIcon = if (preferredBrowser.equals("firefox", ignoreCase = true)) "🦊" else "🌐"

    fun startOffer() {
        if (isLimitReached || isOnBreak) return
        earlyExitWarning = null
        visitStartTime = System.currentTimeMillis()
        isVisitingInProgress = true
        CustomTabsHelper.openCustomTab(
            context = context,
            url = targetInfo.url,
            preferredBrowser = preferredBrowser
        )
    }

    fun claimReward() {
        val elapsedSec = ((System.currentTimeMillis() - visitStartTime) / 1000).toInt()
        if (elapsedSec < config.visitDurationSeconds) {
            earlyExitWarning = "⚠️ ভিজিট সম্পূর্ণ হয়নি! আপনি মাত্র ${elapsedSec} সেকেন্ড সাইটে ছিলেন। পয়েন্ট পাওয়ার জন্য পুরো ${config.visitDurationSeconds} সেকেন্ড সাইটে অবস্থান করতে হবে।"
            return
        }

        val res = repository.completeVisitEarn(
            networkType = "monetag",
            elapsedSeconds = elapsedSec,
            campaignId = targetInfo.campaignId,
            adminLinkId = targetInfo.adminLinkId
        )

        if (res.isSuccess) {
            rewardEarned = res.getOrNull() ?: config.rewardPoints
            isVisitingInProgress = false
            canClaimReward = false
            earlyExitWarning = null
            showSuccessDialog = true
        } else {
            earlyExitWarning = res.exceptionOrNull()?.message ?: "কাজ সম্পন্ন করা যায়নি।"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SubScreenTopBar(
            title = "Monetag Earning",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFF1565C0)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1565C0).copy(alpha = 0.25f),
                                    Color(0xFF0D47A1).copy(alpha = 0.3f),
                                    Color(0xFF0A192F)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(Color(0xFF1E88E5), Color(0xFF64B5F6))),
                            RoundedCornerShape(22.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF1565C0), Color(0xFF1E88E5))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MonetizationOn,
                                        contentDescription = "Monetag Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Monetag Network",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "High CPM SmartLink Tasks (${config.visitDurationSeconds}s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GoldAccent.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "🪙 ${config.rewardPoints.toInt()} Credits",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Progress Bar & Stats
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.Black.copy(alpha = 0.25f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Daily Progress ($count / $limit)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isLimitReached) "Limit Reached" else "${(count.toFloat() / limit.coerceAtLeast(1) * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isLimitReached) SuccessGreen else Color(0xFF64B5F6),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((count.toFloat() / limit.coerceAtLeast(1)).coerceIn(0f, 1f))
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF1565C0), Color(0xFF64B5F6))
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Break Timer Card if Active
            if (isOnBreak) {
                val mins = breakRemainingSec / 60
                val secs = breakRemainingSec % 60
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = "Break Active",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "🛑 বিরতি চলছে (Break Active)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "প্রতি ${config.breakFrequency} টি ভিজিট পর ${config.breakDurationMinutes} মিনিট বিরতি প্রযোজ্য।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "বাকি সময়: ${String.format("%02d:%02d", mins, secs)} মিনিট",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Early exit warning alert
            if (earlyExitWarning != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = earlyExitWarning ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Browser In-App Info Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = browserIcon, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Browser: $browserName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64B5F6),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Active Visit State / Countdown Card
            AnimatedVisibility(
                visible = isVisitingInProgress,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF1565C0)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1565C0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!canClaimReward) {
                                CircularProgressIndicator(
                                    progress = { ((config.visitDurationSeconds - secondsRemaining).toFloat() / config.visitDurationSeconds.coerceAtLeast(1)).coerceIn(0f, 1f) },
                                    modifier = Modifier.size(32.dp),
                                    color = Color(0xFF1565C0),
                                    strokeCap = StrokeCap.Round
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (!canClaimReward) "Viewing SmartLink (${secondsRemaining}s)" else "SmartLink View Completed!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!canClaimReward) MaterialTheme.colorScheme.onSurface else SuccessGreen
                                )
                                Text(
                                    text = if (!canClaimReward) "Stay on page until timer reaches zero" else "🪙 ${config.rewardPoints.toInt()} Credits ready to claim",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    CustomTabsHelper.openCustomTab(
                                        context = context,
                                        url = targetInfo.url,
                                        preferredBrowser = preferredBrowser
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reopen Tab", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = { claimReward() },
                                enabled = canClaimReward,
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SuccessGreen
                                )
                            ) {
                                Text(
                                    text = if (canClaimReward) "Claim 🪙 ${config.rewardPoints.toInt()}" else "Viewing (${secondsRemaining}s)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }

            // Interactive SmartLink Starter Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1565C0).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLimitReached) Icons.Filled.CheckCircle else if (isOnBreak) Icons.Default.HourglassTop else Icons.Filled.MonetizationOn,
                            contentDescription = null,
                            tint = if (isLimitReached) SuccessGreen else if (isOnBreak) MaterialTheme.colorScheme.error else Color(0xFF1565C0),
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Text(
                        text = if (isLimitReached) "All Today's SmartLinks Visited!" else if (isOnBreak) "Break Time Active" else "Visit Monetag SmartLink",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (isLimitReached)
                            "You have reached today's limit ($limit/$limit). Come back tomorrow for more rewards!"
                        else if (isOnBreak)
                            "Please wait for the break timer to expire before starting new links."
                        else
                            "Click below to open Monetag smart link for ${config.visitDurationSeconds} seconds. 🪙 ${config.rewardPoints.toInt()} Credits will be credited instantly upon verification.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    if (targetInfo.isSponsoredAdminLink) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PurpleNeon.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = PurpleNeon,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sponsored: ${targetInfo.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PurpleNeon,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (targetInfo.campaignId != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1565C0).copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF1565C0),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Verified Campaign: ${targetInfo.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF1E88E5),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { startOffer() },
                        enabled = !isLimitReached && !isOnBreak,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_monetag_visit_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1565C0)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isLimitReached) "Limit Completed" else if (isOnBreak) "On Break..." else "Visit & Earn 🪙 ${config.rewardPoints.toInt()} Credits",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Instructions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Monetag Rules & Guidelines",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "• View smartlink landing in $browserName for full ${config.visitDurationSeconds} seconds.\n• Do not exit before countdown completes.\n• Automatic break of ${config.breakDurationMinutes}m triggers every ${config.breakFrequency} visits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Success Dialog
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Success",
                        tint = SuccessGreen,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "Monetag Visit Completed!",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "Great job! 🪙 ${rewardEarned.toInt()} Credits have been credited to your wallet balance.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showSuccessDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        Text("Continue Earning")
                    }
                }
            )
        }
    }
}
