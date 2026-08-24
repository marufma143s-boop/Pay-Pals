package com.example.components
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.Language

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navigation.Screen
import com.example.ui.theme.CardGlowGradientBrush
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isCenterPlus: Boolean = false
)

@Composable
fun AppBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Screen.VisitEarn.route, "Visit Earn", Icons.Filled.Language, Icons.Outlined.Language),
        BottomNavItem(Screen.CreateCampaign.route, "Plus", Icons.Filled.Add, Icons.Filled.Add, isCenterPlus = true),
        BottomNavItem(Screen.Tasks.route, "Tasks", Icons.Outlined.TaskAlt, Icons.Outlined.TaskAlt),
        BottomNavItem(Screen.MyAccount.route, "My Account", Icons.Filled.Person, Icons.Outlined.Person)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Base bar surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    spotColor = PurplePrimary
                ),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    if (item.isCenterPlus) {
                        // Empty spacer for elevated center button
                        Spacer(modifier = Modifier.size(56.dp))
                    } else {
                        val isSelected = currentRoute == item.route
                        val interactionSource = remember { MutableInteractionSource() }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    if (currentRoute != item.route) {
                                        onNavigate(item.route)
                                    }
                                }
                                .testTag("nav_tab_${item.title.lowercase().replace(" ", "_")}")
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) PurplePrimary.copy(alpha = 0.15f) else Color.Transparent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) PurpleNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                        .size(22.dp)
                                )
                            }
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) PurpleNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Elevated Center Plus Button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(58.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        spotColor = PurplePrimary
                    )
                    .clip(CircleShape)
                    .clickable {
                        onNavigate(Screen.CreateCampaign.route)
                    }
                    .testTag("nav_center_plus_button"),
                shape = CircleShape,
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardGlowGradientBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create Campaign",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
