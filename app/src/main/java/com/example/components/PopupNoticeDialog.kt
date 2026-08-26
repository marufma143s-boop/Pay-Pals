package com.example.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.PopupNoticeSettings
import com.example.ui.theme.PurplePrimary

@Composable
fun PopupNoticeDialog(
    settings: PopupNoticeSettings,
    socialLinks: Map<String, String>? = null,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Image
                if (settings.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = settings.imageUrl,
                        contentDescription = "Popup Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (settings.title.isNotBlank()) {
                        Text(
                            text = settings.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (settings.description.isNotBlank()) {
                        Text(
                            text = settings.description,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (settings.showSocialMedia && socialLinks != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            val fb = socialLinks["facebook"] as? String
                            val tg = socialLinks["telegram"] as? String
                            val yt = socialLinks["youtube"] as? String

                            if (!fb.isNullOrBlank()) {
                                SocialIconButton(label = "Facebook", url = fb, uriHandler = uriHandler)
                            }
                            if (!tg.isNullOrBlank()) {
                                SocialIconButton(label = "Telegram", url = tg, uriHandler = uriHandler)
                            }
                            if (!yt.isNullOrBlank()) {
                                SocialIconButton(label = "YouTube", url = yt, uriHandler = uriHandler)
                            }
                        }
                    }

                    if (settings.showActionButton && settings.buttonText.isNotBlank()) {
                        Button(
                            onClick = {
                                onDismiss()
                                if (settings.actionType == "INTERNAL") {
                                    if (settings.internalDestination.isNotBlank()) {
                                        onNavigate(settings.internalDestination)
                                    }
                                } else {
                                    if (settings.externalUrl.isNotBlank()) {
                                        uriHandler.openUri(settings.externalUrl)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Text(settings.buttonText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Close button overlay
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SocialIconButton(label: String, url: String, uriHandler: androidx.compose.ui.platform.UriHandler) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .size(48.dp)
            .clickable { uriHandler.openUri(url) }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Language, // Using generic icon if specific logos aren't available
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
