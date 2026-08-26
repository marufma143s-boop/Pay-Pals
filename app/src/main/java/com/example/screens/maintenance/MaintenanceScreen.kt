package com.example.screens.maintenance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MaintenanceSettings
import com.example.model.SocialMediaLink
import com.example.screens.admin.getSocialIconColor
import com.example.screens.admin.getSocialVectorIcon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkPurpleCard
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.utils.CustomTabsHelper

@Composable
fun MaintenanceScreen(
    maintenanceSettings: MaintenanceSettings,
    isAdminBlocked: Boolean = false
) {
    val context = LocalContext.current
    val note = if (isAdminBlocked) {
        maintenanceSettings.adminNote.ifBlank {
            "Admin backend is currently undergoing maintenance. Please contact the Owner."
        }
    } else {
        maintenanceSettings.userNote.ifBlank {
            "আমাদের অ্যাপে সাময়িক রক্ষণাবেক্ষণ কাজ চলছে। খুব দ্রুত আমরা স্বাভাবিক সেবায় ফিরে আসব। সাময়িক অসুবিধার জন্য আমরা আন্তরিকভাবে দুঃখিত।"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        Color(0xFF1E143B),
                        DarkBackground
                    )
                )
            )
            .testTag("maintenance_overlay_screen"),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))

                // Glowing Maintenance Icon Box
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    PurpleNeon.copy(alpha = 0.35f),
                                    PurplePrimary.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(76.dp),
                        shape = CircleShape,
                        color = Color(0xFF2A1B4E),
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = "Maintenance",
                                tint = PurpleNeon,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = if (isAdminBlocked) "Admin Maintenance" else "Under Maintenance",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isAdminBlocked) "এডমিন প্যানেল রক্ষণাবেক্ষণ চলছে" else "অ্যাপ সাময়িক রক্ষণাবেক্ষণে আছে",
                    style = MaterialTheme.typography.titleSmall,
                    color = PurpleNeon,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Note Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkPurpleCard)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PurpleNeon,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFE2E8F0),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Social / Support Links
                if (maintenanceSettings.socialLinks.isNotEmpty()) {
                    Text(
                        text = "Official Support & Updates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "যেকোনো সাহায্য বা আপডেটের জন্য আমাদের চ্যানেলে যুক্ত থাকুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA0AEC0),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Social Media Buttons
            items(maintenanceSettings.socialLinks, key = { it.id }) { link ->
                SocialLinkUserButton(
                    link = link,
                    onClick = {
                        CustomTabsHelper.openCustomTab(context, link.url)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

@Composable
private fun SocialLinkUserButton(
    link: SocialMediaLink,
    onClick: () -> Unit
) {
    val brandColor = getSocialIconColor(link.iconKey)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = DarkPurpleCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, brandColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(brandColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getSocialVectorIcon(link.iconKey),
                    contentDescription = null,
                    tint = brandColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = link.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Tap to open official channel",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFA0AEC0)
                )
            }

            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = brandColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
