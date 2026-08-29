package com.example.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdminDirectLink
import com.example.repository.AppRepository
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import java.util.UUID

@Composable
fun AdminDirectLinksSection(repository: AppRepository) {
    val adminLinks by repository.adminDirectLinks.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingLink by remember { mutableStateOf<AdminDirectLink?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<AdminDirectLink?>(null) }

    // Dialog Input State
    var inputTitle by remember { mutableStateOf("") }
    var inputUrl by remember { mutableStateOf("") }
    var inputNetwork by remember { mutableStateOf("all") }
    var inputFreq by remember { mutableStateOf("10") }
    var inputIsActive by remember { mutableStateOf(true) }
    var inputError by remember { mutableStateOf<String?>(null) }

    fun openAddDialog() {
        editingLink = null
        inputTitle = ""
        inputUrl = ""
        inputNetwork = "all"
        inputFreq = "10"
        inputIsActive = true
        inputError = null
        showAddEditDialog = true
    }

    fun openEditDialog(link: AdminDirectLink) {
        editingLink = link
        inputTitle = link.title
        inputUrl = link.url
        inputNetwork = link.networkType
        inputFreq = link.frequency.toString()
        inputIsActive = link.isActive
        inputError = null
        showAddEditDialog = true
    }

    fun saveLink() {
        if (inputTitle.isBlank()) {
            inputError = "অনুগ্রহ করে লিংকের একটি নাম/টাইটেল দিন।"
            return
        }
        val cleanUrl = inputUrl.trim()
        if (cleanUrl.isBlank() || (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://"))) {
            inputError = "সঠিক URL প্রবেশ করান (https:// সহ)।"
            return
        }

        val freq = inputFreq.toIntOrNull() ?: 10
        val linkToSave = if (editingLink != null) {
            editingLink!!.copy(
                title = inputTitle.trim(),
                url = cleanUrl,
                networkType = inputNetwork,
                frequency = freq.coerceAtLeast(1),
                isActive = inputIsActive
            )
        } else {
            AdminDirectLink(
                id = UUID.randomUUID().toString(),
                title = inputTitle.trim(),
                url = cleanUrl,
                networkType = inputNetwork,
                frequency = freq.coerceAtLeast(1),
                isActive = inputIsActive,
                createdDate = "Today",
                viewsServed = 0,
                timestamp = System.currentTimeMillis()
            )
        }

        repository.saveAdminDirectLink(linkToSave)
        showAddEditDialog = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddDialog() },
                containerColor = PurplePrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_admin_direct_link_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Sponsored Link")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleNeon.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PurplePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = PurpleNeon,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Admin Sponsored Direct Links",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "আনলিমিটেড স্পন্সর লিংক যুক্ত করুন এবং ফ্রিকোয়েন্সি অনুযায়ী ইউজারদের দেখান",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (adminLinks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LinkOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "কোনো স্পন্সর লিংক নেই",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "নিচের (+) বাটনে চাপ দিয়ে এডমিন স্পন্সর লিংক যুক্ত করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(adminLinks, key = { it.id }) { link ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (link.isActive) PurpleNeon.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (link.isActive) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (link.isActive) "ACTIVE" else "DISABLED",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (link.isActive) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = link.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Switch(
                                        checked = link.isActive,
                                        onCheckedChange = { checked ->
                                            repository.saveAdminDirectLink(link.copy(isActive = checked))
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = PurpleNeon
                                        )
                                    )
                                }

                                Text(
                                    text = link.url,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(
                                            text = "🎯 ${link.networkType.uppercase()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PurpleNeon,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "🔄 প্রতি ${link.frequency} ভিউ পর",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "👁️ ভিউ: ${link.viewsServed}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoldAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { openEditDialog(link) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = PurpleNeon,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { showDeleteConfirmDialog = link },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add / Edit Dialog
        if (showAddEditDialog) {
            AlertDialog(
                onDismissRequest = { showAddEditDialog = false },
                title = {
                    Text(
                        text = if (editingLink != null) "Edit Sponsored Link" else "Add New Sponsored Link",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (inputError != null) {
                            Text(
                                text = inputError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        OutlinedTextField(
                            value = inputTitle,
                            onValueChange = { inputTitle = it },
                            label = { Text("লিংক/ক্যাম্পেইনের টাইটেল") },
                            placeholder = { Text("e.g. Official Direct Sponsor") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            label = { Text("Target URL") },
                            placeholder = { Text("https://example.com/direct-link") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Network Selection Row
                        Column {
                            Text(
                                text = "কোন নেটওয়ার্কে দেখাবে:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("all" to "All", "adstra" to "Adsterra", "blogger" to "Blogger", "monetag" to "Monetag").forEach { (id, label) ->
                                    val isSel = inputNetwork == id
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) PurplePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        onClick = { inputNetwork = id },
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = inputFreq,
                            onValueChange = { inputFreq = it },
                            label = { Text("ফ্রিকোয়েন্সি (প্রতি কত ভিউ পর)") },
                            placeholder = { Text("e.g. 10 বা 20") },
                            supportingText = { Text("ইউজারদের কতটি কাজের পর এই এডমিন লিংকটি রোটেশনে আসবে") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("সক্রিয় রাখবেন (Active Link):", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = inputIsActive,
                                onCheckedChange = { inputIsActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PurpleNeon
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { saveLink() },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Text(if (editingLink != null) "Update Link" else "Add Link")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Confirm Dialog
        if (showDeleteConfirmDialog != null) {
            val linkToDelete = showDeleteConfirmDialog!!
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = null },
                title = { Text("Delete Sponsored Link?") },
                text = { Text("Are you sure you want to delete '${linkToDelete.title}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            repository.deleteAdminDirectLink(linkToDelete.id)
                            showDeleteConfirmDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
