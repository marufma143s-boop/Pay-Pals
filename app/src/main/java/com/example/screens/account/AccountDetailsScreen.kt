package com.example.screens.account

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.components.SubScreenTopBar
import com.example.repository.AppRepository
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.utils.Base64OrResourceImage
import com.example.utils.ImageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AccountDetailsScreen(
    repository: AppRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by repository.userProfile.collectAsState()
    val context = LocalContext.current

    var isEditMode by remember { mutableStateOf(false) }
    var fullNameInput by remember(userProfile.fullName) { mutableStateOf(userProfile.fullName) }
    var emailInput by remember(userProfile.email) { mutableStateOf(userProfile.email) }
    var phoneInput by remember(userProfile.phone) { mutableStateOf(userProfile.phone) }

    var isSaving by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = ImageUtils.uriToBase64(context, uri, maxDimension = 360, quality = 80)
            if (base64 != null) {
                repository.updateUserAvatar(base64)
                successMessage = "Profile picture updated successfully!"
            } else {
                errorMessage = "Failed to process selected image."
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("account_details_screen")
    ) {
        SubScreenTopBar(
            title = "Account Details",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card Top View
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.size(96.dp)
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
                                base64Str = userProfile.avatarBase64,
                                placeholderRes = R.drawable.img_avatar_maruf_1787554123074,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        // Floating camera edit icon badge
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
                                    contentDescription = "Change Profile Picture",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = PurpleNeon
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Change Profile Picture",
                            style = MaterialTheme.typography.labelLarge,
                            color = PurpleNeon,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = userProfile.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = userProfile.username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PurpleNeon,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Editable / View details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Personal Information",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )

                        if (!isEditMode) {
                            OutlinedButton(
                                onClick = {
                                    isEditMode = true
                                    successMessage = null
                                    errorMessage = null
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("edit_profile_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Profile")
                            }
                        }
                    }

                    // Full Name
                    if (isEditMode) {
                        OutlinedTextField(
                            value = fullNameInput,
                            onValueChange = { fullNameInput = it },
                            label = { Text("Full Name") },
                            leadingIcon = {
                                Icon(Icons.Filled.Person, contentDescription = "Name")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        DetailItemRow("Full Name", userProfile.fullName)
                    }

                    // Email
                    if (isEditMode) {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(Icons.Filled.Email, contentDescription = "Email")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_email_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        DetailItemRow("Email", userProfile.email)
                    }

                    // Phone
                    if (isEditMode) {
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Phone") },
                            leadingIcon = {
                                Icon(Icons.Filled.Phone, contentDescription = "Phone")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_phone_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        DetailItemRow("Phone", userProfile.phone)
                    }

                    DetailItemRow("Account ID", userProfile.accountId)
                    DetailItemRow("Registration Date", userProfile.registrationDate)
                    DetailItemRow("Referral Code", userProfile.referralCode)

                    if (successMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SuccessGreen.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = successMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ErrorRed.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ErrorRed,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (isEditMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isEditMode = false
                                    fullNameInput = userProfile.fullName
                                    emailInput = userProfile.email
                                    phoneInput = userProfile.phone
                                    errorMessage = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    isSaving = true
                                    errorMessage = null
                                    coroutineScope.launch {
                                        delay(600)
                                        val result = repository.updateProfile(fullNameInput, emailInput, phoneInput)
                                        isSaving = false
                                        result.onSuccess {
                                             isEditMode = false
                                             successMessage = "Profile updated successfully."
                                        }.onFailure { ex ->
                                             errorMessage = ex.message ?: "Failed to update profile."
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("save_profile_button"),
                                enabled = !isSaving,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Filled.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save Changes")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}
