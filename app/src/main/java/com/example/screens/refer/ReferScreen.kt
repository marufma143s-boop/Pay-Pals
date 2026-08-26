package com.example.screens.refer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.AppStatCard
import com.example.components.ReferralCard
import com.example.components.SectionHeader
import com.example.components.SubScreenTopBar
import com.example.repository.AppRepository
import com.example.ui.theme.DarkPurpleCard
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WalletGradientBrush
import com.example.utils.FormatUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReferScreen(
    repository: AppRepository,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by repository.userProfile.collectAsState()
    val walletState by repository.walletState.collectAsState()
    val referrals by repository.referrals.collectAsState()
    val totalReferralCount by repository.totalReferralCount.collectAsState()

    var referralInput by remember { mutableStateOf("") }
    var isApplying by remember { mutableStateOf(false) }
    var applyMessage by remember { mutableStateOf<String?>(null) }
    var isApplySuccess by remember { mutableStateOf(false) }
    var isCopied by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("refer_screen")
        ) {
            if (onBackClick != null) {
                SubScreenTopBar(
                    title = "Refer & Earn",
                    onBackClick = onBackClick
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Refer & Earn",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Referral Code Top Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = PurplePrimary
                            )
                            .testTag("referral_code_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(WalletGradientBrush)
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(22.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.CardGiftcard,
                                            contentDescription = "Referral Gift",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Your Referral Code",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Referral Code box
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = userProfile.referralCode,
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 4.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.testTag("my_referral_code_text")
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Referral Code", userProfile.referralCode)
                                        clipboard.setPrimaryClip(clip)

                                        isCopied = true
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Copied successfully")
                                            delay(2000)
                                            isCopied = false
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("copy_referral_code_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = PurplePrimary
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                        contentDescription = "Copy Code",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isCopied) "Copied successfully" else "Copy Code",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Summary Statistics (Total Referrals & Referral Earnings)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppStatCard(
                            title = "Total Referrals",
                            value = "$totalReferralCount",
                            supportingText = "Invited Friends",
                            icon = Icons.Outlined.People,
                            accentColor = InfoBlue,
                            modifier = Modifier.weight(1f)
                        )

                        AppStatCard(
                            title = "Referral Earnings",
                            value = FormatUtils.formatCredits(walletState.totalReferralEarnings),
                            supportingText = "100 Credits Per Invite",
                            icon = Icons.Filled.MonetizationOn,
                            accentColor = GoldAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. Apply Referral Code Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apply_referral_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "Apply Referral Code",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Enter an inviter's referral code to receive an instant 50 Credits bonus. Both you and your friend earn rewards!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            if (userProfile.appliedReferralCode != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SuccessGreen.copy(alpha = 0.15f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Applied",
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Referral Offer Claimed",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = SuccessGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Applied code: ${userProfile.appliedReferralCode}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SuccessGreen.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = referralInput,
                                    onValueChange = {
                                        referralInput = it.uppercase()
                                        applyMessage = null
                                    },
                                    placeholder = { Text("Enter referral code") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("apply_referral_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PurplePrimary
                                    ),
                                    singleLine = true
                                )

                                if (applyMessage != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = applyMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isApplySuccess) SuccessGreen else ErrorRed
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (referralInput.isBlank()) {
                                            applyMessage = "Please enter a referral code."
                                            isApplySuccess = false
                                            return@Button
                                        }

                                        isApplying = true
                                        applyMessage = null

                                        coroutineScope.launch {
                                            val result = repository.applyReferralCode(referralInput)
                                            isApplying = false
                                            result.onSuccess {
                                                isApplySuccess = true
                                                applyMessage = "Referral code applied successfully! 50 Credits added to your wallet."
                                                referralInput = ""
                                            }.onFailure { ex ->
                                                isApplySuccess = false
                                                applyMessage = ex.message ?: "Offer already claimed or invalid code."
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("apply_referral_button"),
                                    enabled = !isApplying,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PurplePrimary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    if (isApplying) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Apply Code",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Referral History
                item {
                    SectionHeader(
                        title = "Referral History",
                        actionText = null
                    )
                }

                if (referrals.isEmpty()) {
                    item {
                        com.example.components.EmptyStateView(
                            icon = Icons.Outlined.People,
                            title = "No Referrals Yet",
                            message = "Share your referral code with friends and family to start earning bonuses!"
                        )
                    }
                } else {
                    items(referrals, key = { it.id }) { ref ->
                        ReferralCard(referral = ref)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}
