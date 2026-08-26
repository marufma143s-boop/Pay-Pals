package com.example.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DeveloperInfo
import com.example.repository.AppRepository
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.utils.Base64OrResourceImage
import com.example.utils.ImageUtils

@Composable
fun AdminDeveloperSettingsScreen(
    repository: AppRepository,
    modifier: Modifier = Modifier
) {
    val developerInfo by repository.developerInfo.collectAsState()
    val context = LocalContext.current

    var name by remember(developerInfo.name) { mutableStateOf(developerInfo.name) }
    var title by remember(developerInfo.title) { mutableStateOf(developerInfo.title) }
    var email by remember(developerInfo.email) { mutableStateOf(developerInfo.email) }
    var phone by remember(developerInfo.phone) { mutableStateOf(developerInfo.phone) }
    var website by remember(developerInfo.website) { mutableStateOf(developerInfo.website) }
    var description by remember(developerInfo.description) { mutableStateOf(developerInfo.description) }
    var avatarBase64 by remember(developerInfo.avatarBase64) { mutableStateOf(developerInfo.avatarBase64) }

    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = ImageUtils.uriToBase64(context, uri, maxDimension = 360, quality = 80)
            if (base64 != null) {
                avatarBase64 = base64
                saveSuccessMessage = "New photo selected! Click Save Developer Profile to publish."
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Developer Profile Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Customize developer details shown across the app",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PurplePrimary.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Sync",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                }
            }
        }

        // Profile Picture Avatar Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.size(100.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxSize()
                            .border(3.dp, PurplePrimary, CircleShape)
                            .clickable { photoPickerLauncher.launch("image/*") },
                        color = PurplePrimary.copy(alpha = 0.1f)
                    ) {
                        Base64OrResourceImage(
                            base64Str = avatarBase64,
                            placeholderRes = R.drawable.img_avatar_maruf_1787554123074,
                            contentDescription = "Developer Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = PurplePrimary,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { photoPickerLauncher.launch("image/*") }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Photo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = PurpleNeon, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Change Developer Photo", color = PurpleNeon, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Inputs Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; saveSuccessMessage = null },
                    label = { Text("Developer Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dev_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; saveSuccessMessage = null },
                    label = { Text("Job Title / Role") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dev_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; saveSuccessMessage = null },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dev_email_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; saveSuccessMessage = null },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dev_phone_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it; saveSuccessMessage = null },
                    label = { Text("Website / GitHub URL") },
                    leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dev_website_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; saveSuccessMessage = null },
                    label = { Text("Developer Bio / Description") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("dev_description_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 6
                )

                if (saveSuccessMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SuccessGreen.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = saveSuccessMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        val newInfo = DeveloperInfo(
                            name = name.trim().ifEmpty { "Maruf Hossain" },
                            title = title.trim().ifEmpty { "Lead Android & Fintech Architect" },
                            email = email.trim().ifEmpty { "maruf.hossain.dev@gmail.com" },
                            phone = phone.trim().ifEmpty { "+880 1712-345678" },
                            website = website.trim().ifEmpty { "https://github.com/maruf-dev" },
                            description = description.trim(),
                            avatarBase64 = avatarBase64
                        )
                        repository.updateDeveloperInfo(newInfo)
                        saveSuccessMessage = "Developer profile saved and published in realtime!"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_dev_profile_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Developer Profile", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
