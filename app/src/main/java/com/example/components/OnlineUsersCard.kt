package com.example.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import kotlin.math.sin
import java.util.Random

// Calculates deterministic synced online count between minUsers and maxUsers for all users at any given timestamp
fun calculateSyncedOnlineUsers(
    minUsers: Int = 50,
    maxUsers: Int = 1000,
    timeMs: Long = System.currentTimeMillis()
): Int {
    val actualMin = if (minUsers < 1) 1 else minUsers
    val actualMax = if (maxUsers < actualMin) actualMin else maxUsers
    val range = actualMax - actualMin
    if (range <= 0) return actualMin

    val intervalStep = timeMs / 3500L // synced interval step every 3.5 seconds
    val rand = Random(intervalStep xor 0x5DEECE66DL)
    
    // Normalized smooth sine cycle [0.0, 1.0]
    val cycle = (sin((intervalStep % 1000) * 0.02) + 1.0) / 2.0 // 0.0 to 1.0
    val shortVariation = (sin((intervalStep % 100) * 0.25)) * 0.08 // -0.08 to +0.08
    val jitterFraction = ((rand.nextDouble() - 0.5) * 0.06) // -0.03 to +0.03

    val combinedFraction = (0.2 + 0.6 * cycle + shortVariation + jitterFraction).coerceIn(0.0, 1.0)
    val calculated = (actualMin + (range * combinedFraction)).toInt()
    return calculated.coerceIn(actualMin, actualMax)
}

@Composable
fun OnlineUsersCard(
    minUsers: Int = 50,
    maxUsers: Int = 1000
) {
    var onlineUsers by remember(minUsers, maxUsers) { 
        mutableIntStateOf(calculateSyncedOnlineUsers(minUsers, maxUsers)) 
    }

    LaunchedEffect(minUsers, maxUsers) {
        while (true) {
            onlineUsers = calculateSyncedOnlineUsers(minUsers, maxUsers)
            delay(3500L)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(SuccessGreen.copy(alpha = alpha), CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Total Online Users",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = DecimalFormat("#,###").format(onlineUsers),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PurplePrimary
            )
        }
    }
}
