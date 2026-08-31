package com.example.ui.menu.admin

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.GroupItem
import com.example.data.repository.GroupPageRepository
import com.example.ui.components.VerificationBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class GroupFilterTab(val title: String) {
    ALL("All Groups"),
    ACTIVE("Active"),
    BLOCKED("Blocked"),
    VERIFIED("Verified")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGroupManagementView(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { GroupPageRepository(context) }
    val allGroups by repository.groupsFlow.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(GroupFilterTab.ALL) }

    // Dialog States
    var editingGroup by remember { mutableStateOf<GroupItem?>(null) }
    var deletingGroup by remember { mutableStateOf<GroupItem?>(null) }
    var badgeManagingGroup by remember { mutableStateOf<GroupItem?>(null) }
    var expiryManagingGroup by remember { mutableStateOf<GroupItem?>(null) }

    // Filter list
    val filteredGroups = remember(allGroups, searchQuery, selectedTab) {
        allGroups.filter { group ->
            val matchesSearch = searchQuery.isBlank() ||
                    group.name.contains(searchQuery, ignoreCase = true) ||
                    group.description.contains(searchQuery, ignoreCase = true) ||
                    group.category.contains(searchQuery, ignoreCase = true) ||
                    group.creatorId.contains(searchQuery, ignoreCase = true) ||
                    group.id.contains(searchQuery, ignoreCase = true)

            val matchesTab = when (selectedTab) {
                GroupFilterTab.ALL -> true
                GroupFilterTab.ACTIVE -> !group.isBlocked
                GroupFilterTab.BLOCKED -> group.isBlocked
                GroupFilterTab.VERIFIED -> group.isBadgeActive()
            }

            matchesSearch && matchesTab
        }
    }

    val totalCount = allGroups.size
    val activeCount = allGroups.count { !it.isBlocked }
    val blockedCount = allGroups.count { it.isBlocked }
    val verifiedCount = allGroups.count { it.isBadgeActive() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Group Management",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF050505)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1877F2).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "$totalCount",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1877F2),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Manage and oversee all user groups",
                            fontSize = 11.sp,
                            color = Color(0xFF65676B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_group_back_btn")) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF050505)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF0F2F5),
        modifier = modifier.testTag("admin_group_management_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Stats Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AdminStatMiniItem(title = "Total Groups", value = "$totalCount", color = Color(0xFF1877F2))
                        AdminStatMiniItem(title = "Active", value = "$activeCount", color = Color(0xFF2E7D32))
                        AdminStatMiniItem(title = "Blocked", value = "$blockedCount", color = Color(0xFFD32F2F))
                        AdminStatMiniItem(title = "Verified", value = "$verifiedCount", color = Color(0xFF00897B))
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by group name, category, ID...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF65676B))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF65676B))
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF1877F2),
                        unfocusedBorderColor = Color(0xFFCED0D4)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("admin_group_search_input")
                )
            }

            // Filter Tabs
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(GroupFilterTab.values()) { tab ->
                        val isSelected = selectedTab == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            label = {
                                val count = when (tab) {
                                    GroupFilterTab.ALL -> totalCount
                                    GroupFilterTab.ACTIVE -> activeCount
                                    GroupFilterTab.BLOCKED -> blockedCount
                                    GroupFilterTab.VERIFIED -> verifiedCount
                                }
                                Text("${tab.title} ($count)", fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1877F2),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF050505)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Empty state or Group List
            if (filteredGroups.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFFBCC0C4),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No groups found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF65676B)
                            )
                            Text(
                                text = "When users create groups, they will appear here",
                                fontSize = 12.sp,
                                color = Color(0xFF8A8D91)
                            )
                        }
                    }
                }
            } else {
                items(filteredGroups, key = { it.id }) { group ->
                    AdminGroupCard(
                        group = group,
                        onEdit = { editingGroup = group },
                        onToggleBlock = {
                            val newStatus = !group.isBlocked
                            repository.setGroupBlocked(group.id, newStatus)
                            Toast.makeText(
                                context,
                                if (newStatus) "Group '${group.name}' blocked" else "Group '${group.name}' unblocked",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onManageBadge = { badgeManagingGroup = group },
                        onManageExpiry = { expiryManagingGroup = group },
                        onDelete = { deletingGroup = group }
                    )
                }
            }
        }
    }

    // 1. Edit Group Dialog
    editingGroup?.let { group ->
        AdminEditGroupDialog(
            group = group,
            onDismiss = { editingGroup = null },
            onSave = { updated ->
                repository.updateGroup(updated)
                editingGroup = null
                Toast.makeText(context, "Group successfully updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 2. Delete Confirmation Dialog
    deletingGroup?.let { group ->
        AlertDialog(
            onDismissRequest = { deletingGroup = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F)) },
            title = { Text("Delete Group?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to permanently delete '${group.name}' from the database? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.deleteGroup(group.id)
                        deletingGroup = null
                        Toast.makeText(context, "Group deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Yes, Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingGroup = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. Verification Badge Dialog (On/Off & Type)
    badgeManagingGroup?.let { group ->
        AdminGroupBadgeDialog(
            group = group,
            onDismiss = { badgeManagingGroup = null },
            onSave = { isVerified, badgeType, expiresAt ->
                repository.setGroupVerification(group.id, isVerified, badgeType, expiresAt)
                badgeManagingGroup = null
                Toast.makeText(context, "Verification badge updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 4. Verification Expiry / Validity Dialog (Increase / Decrease)
    expiryManagingGroup?.let { group ->
        AdminBadgeExpiryDialog(
            title = "Adjust Group Badge Expiry",
            currentExpiry = group.badgeExpiresAt,
            onDismiss = { expiryManagingGroup = null },
            onAdjustDays = { days ->
                repository.adjustGroupBadgeExpiry(group.id, days)
                expiryManagingGroup = null
                Toast.makeText(context, "Badge expiry updated", Toast.LENGTH_SHORT).show()
            },
            onSetCustomExpiry = { expiryMs ->
                repository.setGroupVerification(group.id, true, group.badgeType, expiryMs)
                expiryManagingGroup = null
                Toast.makeText(context, "Badge expiry updated", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun AdminGroupCard(
    group: GroupItem,
    onEdit: () -> Unit,
    onToggleBlock: () -> Unit,
    onManageBadge: () -> Unit,
    onManageExpiry: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val isVerifiedActive = group.isBadgeActive()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("admin_group_card_${group.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Cover/Icon + Group Name + Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Group Cover / Avatar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE4E6EB),
                    modifier = Modifier.size(54.dp)
                ) {
                    if (group.coverUrl.isNotBlank()) {
                        AsyncImage(
                            model = group.coverUrl,
                            contentDescription = group.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFF1877F2),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = group.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF050505),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isVerifiedActive) {
                            Spacer(modifier = Modifier.width(4.dp))
                            if (group.badgeType == "GREEN") {
                                VerificationBadge(size = 16.dp, show = true)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Blue",
                                    tint = Color(0xFF1877F2),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF0F2F5)
                        ) {
                            Text(
                                text = group.privacy,
                                fontSize = 11.sp,
                                color = Color(0xFF65676B),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "${group.membersCount} members",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEDE7F6)
                        ) {
                            Text(
                                text = group.category,
                                fontSize = 11.sp,
                                color = Color(0xFF5E35B1),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Block status badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (group.isBlocked) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = if (group.isBlocked) "Blocked" else "Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (group.isBlocked) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Description
            if (group.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = group.description,
                    fontSize = 13.sp,
                    color = Color(0xFF333333),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF0F2F5), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Metadata & Verification Expiry info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Created: ${dateFormat.format(Date(group.createdAt))}",
                        fontSize = 11.sp,
                        color = Color(0xFF8A8D91)
                    )
                    if (group.creatorId.isNotBlank()) {
                        Text(
                            text = "Creator ID: ${group.creatorId}",
                            fontSize = 10.sp,
                            color = Color(0xFF8A8D91),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Verification Badge details
                if (group.isVerified) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isVerifiedActive) Color(0xFFE3F2FD) else Color(0xFFFFEBEE),
                        modifier = Modifier.clickable { onManageExpiry() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = if (isVerifiedActive) Color(0xFF1877F2) else Color(0xFFD32F2F),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val expiryText = if (group.badgeExpiresAt == 0L) {
                                "Lifetime"
                            } else if (isVerifiedActive) {
                                val remainingDays = ((group.badgeExpiresAt - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                                "$remainingDays days left"
                            } else {
                                "Expired"
                            }
                            Text(
                                text = "Badge: $expiryText",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isVerifiedActive) Color(0xFF1877F2) else Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row (Edit, Block/Unblock, Badge, Expiry, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Edit
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                // 2. Block / Unblock
                Button(
                    onClick = onToggleBlock,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (group.isBlocked) Color(0xFF2E7D32) else Color(0xFFE53935)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.1f).height(38.dp)
                ) {
                    Icon(
                        imageVector = if (group.isBlocked) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (group.isBlocked) "Unblock" else "Block",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // 3. Verification Badge Settings
                OutlinedButton(
                    onClick = onManageBadge,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.1f).height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = if (group.isVerified) Color(0xFF1877F2) else Color(0xFF65676B)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (group.isVerified) "Badge ON" else "Badge OFF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (group.isVerified) Color(0xFF1877F2) else Color(0xFF65676B)
                    )
                }

                // 4. Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Edit Group Dialog for Admins
 */
@Composable
fun AdminEditGroupDialog(
    group: GroupItem,
    onDismiss: () -> Unit,
    onSave: (GroupItem) -> Unit
) {
    var name by remember { mutableStateOf(group.name) }
    var category by remember { mutableStateOf(group.category) }
    var privacy by remember { mutableStateOf(group.privacy) }
    var description by remember { mutableStateOf(group.description) }
    var coverUrl by remember { mutableStateOf(group.coverUrl) }
    var membersCountStr by remember { mutableStateOf(group.membersCount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Group Details", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = privacy,
                        onValueChange = { privacy = it },
                        label = { Text("Privacy (Public/Private)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = membersCountStr,
                        onValueChange = { membersCountStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Members Count") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = coverUrl,
                    onValueChange = { coverUrl = it },
                    label = { Text("Cover Image URL") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Rules") },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val updated = group.copy(
                            name = name.trim(),
                            category = category.trim().ifBlank { "General" },
                            privacy = privacy.trim().ifBlank { "Public" },
                            description = description.trim(),
                            coverUrl = coverUrl.trim(),
                            membersCount = membersCountStr.toIntOrNull() ?: group.membersCount
                        )
                        onSave(updated)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
            ) {
                Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Manage Verification Badge (Turn on/off, select Blue or Green, set initial validity)
 */
@Composable
fun AdminGroupBadgeDialog(
    group: GroupItem,
    onDismiss: () -> Unit,
    onSave: (isVerified: Boolean, badgeType: String, expiresAt: Long) -> Unit
) {
    var isVerified by remember { mutableStateOf(group.isVerified) }
    var badgeType by remember { mutableStateOf(group.badgeType) }
    var validityOption by remember { mutableStateOf("LIFETIME") } // LIFETIME, 30_DAYS, 90_DAYS, 365_DAYS

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF1877F2))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verification Badge Control", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Enable/Disable switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F2F5), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Enable Verification Badge", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = if (isVerified) "Verified badge icon displayed next to group" else "Badge is disabled",
                            fontSize = 12.sp,
                            color = Color(0xFF65676B)
                        )
                    }
                    Switch(
                        checked = isVerified,
                        onCheckedChange = { isVerified = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF1877F2)
                        )
                    )
                }

                if (isVerified) {
                    // Badge Color Type
                    Text("Badge Type & Color:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (badgeType == "BLUE") Color(0xFFE3F2FD) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (badgeType == "BLUE") Color(0xFF1877F2) else Color(0xFFCED0D4)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { badgeType = "BLUE" }
                                .padding(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Blue Badge", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1877F2))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (badgeType == "GREEN") Color(0xFFE8F5E9) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (badgeType == "GREEN") Color(0xFF2E7D32) else Color(0xFFCED0D4)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { badgeType = "GREEN" }
                                .padding(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                VerificationBadge(size = 20.dp, show = true)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Green Badge", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                            }
                        }
                    }

                    // Expiry options
                    Text("Select Validity:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "LIFETIME" to "Lifetime",
                            "30_DAYS" to "30 Days",
                            "90_DAYS" to "90 Days",
                            "365_DAYS" to "1 Year"
                        ).forEach { (key, label) ->
                            val isSelected = validityOption == key
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF1877F2) else Color(0xFFF0F2F5),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { validityOption = key }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF050505),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val expiresAt = if (!isVerified) {
                        0L
                    } else when (validityOption) {
                        "30_DAYS" -> System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L
                        "90_DAYS" -> System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000L
                        "365_DAYS" -> System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000L
                        else -> 0L
                    }
                    onSave(isVerified, badgeType, expiresAt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
            ) {
                Text("Update", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Manage / Increase / Decrease Badge Expiry Dialog
 */
@Composable
fun AdminBadgeExpiryDialog(
    title: String,
    currentExpiry: Long,
    onDismiss: () -> Unit,
    onAdjustDays: (Int) -> Unit,
    onSetCustomExpiry: (Long) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    var customDaysText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF00897B))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current status
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE0F2F1),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Current Verification Expiry:", fontSize = 12.sp, color = Color(0xFF004D40))
                        Text(
                            text = if (currentExpiry == 0L) "Lifetime (No expiration)"
                            else dateFormat.format(Date(currentExpiry)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004D40)
                        )
                    }
                }

                Text("Quick Add Expiry:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "+7 Days" to 7,
                        "+30 Days" to 30,
                        "+90 Days" to 90,
                        "+1 Year" to 365
                    ).forEach { (label, days) ->
                        Button(
                            onClick = { onAdjustDays(days) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Text("Reduce or Lifetime Expiry:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAdjustDays(-7) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Text("-7 Days", fontSize = 11.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { onAdjustDays(-30) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Text("-30 Days", fontSize = 11.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onSetCustomExpiry(0L) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1.3f).height(38.dp)
                    ) {
                        Text("Lifetime (∞)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Custom Days Input
                Text("Add Custom Days:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customDaysText,
                        onValueChange = { customDaysText = it.filter { ch -> ch.isDigit() } },
                        placeholder = { Text("Enter number of days (e.g., 45)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            val days = customDaysText.toIntOrNull()
                            if (days != null && days > 0) {
                                onAdjustDays(days)
                            }
                        },
                        enabled = customDaysText.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("Add", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AdminStatMiniItem(
    title: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = title, fontSize = 11.sp, color = Color(0xFF65676B))
    }
}
