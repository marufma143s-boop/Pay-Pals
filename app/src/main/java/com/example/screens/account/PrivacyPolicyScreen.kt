package com.example.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.SubScreenTopBar
import com.example.ui.theme.PurplePrimary

@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val policySections = listOf(
        Pair("1. Introduction", "Welcome to PayPulse. We are committed to protecting your personal information and your right to privacy. This policy outlines how we collect, use, and safeguard your details when using our campaign promotion, task reward, and digital wallet services."),
        Pair("2. Information We Collect", "We collect personal identity data such as your name, username, email address, phone number, and financial payment identifiers provided during deposit and withdrawal processing."),
        Pair("3. How We Use Your Information", "We use collected information to manage user accounts, deliver campaign traffic, credit verified task rewards, process deposits and withdrawals in Credits, and prevent fraudulent behaviors."),
        Pair("4. Wallet & Transaction Security", "All wallet operations and payouts are encrypted and audited through secure server protocols. We enforce strict multi-factor checks on payout requests."),
        Pair("5. Daily Task Verification & Anti-Fraud", "To preserve campaign traffic integrity, PayPulse enforces an automated daily task quota of 50 tasks per user account. Automated bots, script execution, or emulator abuse will lead to permanent wallet forfeiture."),
        Pair("6. Referral Program Terms", "Referral rewards are granted when a verified new user registers or applies a valid referral code. Self-referrals and circular referral loops are automatically detected and disqualified."),
        Pair("7. Third-Party Links & Advertising", "PayPulse contains external promotion links submitted by campaign advertisers. We do not endorse or take responsibility for content hosted on third-party domains."),
        Pair("8. Data Retention & Erasure", "Your personal profile and transaction history are retained as long as your account remains active. You may request account deletion or data anonymization at any time via Support."),
        Pair("9. Children's Privacy", "PayPulse services are intended exclusively for individuals aged 18 and older. We do not knowingly solicit or collect data from minors."),
        Pair("10. Updates to This Policy", "We may update this Privacy Policy periodically. Continued use of PayPulse after updates constitutes acceptance of the modified terms.")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("privacy_policy_screen")
    ) {
        SubScreenTopBar(
            title = "Privacy Policy",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Last updated: October 2024",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(policySections.size) { index ->
                val (title, content) = policySections[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
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
