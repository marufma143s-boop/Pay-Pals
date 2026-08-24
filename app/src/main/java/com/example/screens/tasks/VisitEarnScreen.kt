package com.example.screens.tasks

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.SubScreenTopBar
import com.example.repository.AppRepository
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.PurpleNeon

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBackClick != null) {
            SubScreenTopBar(title = "Visit Earn", onBackClick = onBackClick)
        } else {
            SubScreenTopBar(title = "Visit Earn", onBackClick = {})
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Adstra Card
            NetworkEarningCard(
                name = "Adstra Earning",
                count = adstraCount,
                limit = repository.adstraLimit,
                iconType = "adstra",
                onClick = onNavigateToAdstra
            )

            // Blogger Card
            NetworkEarningCard(
                name = "Blogger Earning",
                count = bloggerCount,
                limit = repository.bloggerLimit,
                iconType = "blogger",
                onClick = onNavigateToBlogger
            )

            // Monetag Card
            NetworkEarningCard(
                name = "Monetag Earning",
                count = monetagCount,
                limit = repository.monetagLimit,
                iconType = "monetag",
                onClick = onNavigateToMonetag
            )
        }
    }
}

@Composable
fun NetworkEarningCard(
    name: String,
    count: Int,
    limit: Int,
    iconType: String,
    onClick: () -> Unit,
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
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon Generation
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(
                            id = when (iconType) {
                                "blogger" -> com.example.R.drawable.blogger_logo_1787562849837
                                "monetag" -> com.example.R.drawable.ic_monetag_logo
                                else -> com.example.R.drawable.adsterra_logo_1787562831187
                            }
                        ),
                        contentDescription = "$name logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Daily Visit",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Limit and Pipe Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Limit: $count/$limit Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) SuccessGreen else PurplePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Pipe
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush = if (isCompleted) {
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        listOf(SuccessGreen, Color(0xFF00C853))
                                    )
                                } else {
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        listOf(PurplePrimary, PurpleNeon)
                                    )
                                }
                            )
                    )
                }
            }
        }
    }
}
