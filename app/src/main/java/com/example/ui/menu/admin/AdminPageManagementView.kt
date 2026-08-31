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
import com.example.data.model.PageItem
import com.example.data.repository.GroupPageRepository
import com.example.ui.components.VerificationBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PageFilterTab(val title: String) {
    ALL("All Pages"),
    ACTIVE("Active"),
    BLOCKED("Blocked"),
    VERIFIED("Verified")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPageManagementView(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { GroupPageRepository(context) }
    val allPages by repository.pagesFlow.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(PageFilterTab.ALL) }

    // Dialog States
    var editingPage by remember { mutableStateOf<PageItem?>(null) }
    var deletingPage by remember { mutableStateOf<PageItem?>(null) }
    var badgeManagingPage by remember { mutableStateOf<PageItem?>(null) }
    var expiryManagingPage by remember { mutableStateOf<PageItem?>(null) }

    // Filter list
    val filteredPages = remember(allPages, searchQuery, selectedTab) {
        allPages.filter { page ->
            val matchesSearch = searchQuery.isBlank() ||
                    page.name.contains(searchQuery, ignoreCase = true) ||
                    page.description.contains(searchQuery, ignoreCase = true) ||
                    page.category.contains(searchQuery, ignoreCase = true) ||
                    page.creatorId.contains(searchQuery, ignoreCase = true) ||
                    page.website.contains(searchQuery, ignoreCase = true) ||
                    page.email.contains(searchQuery, ignoreCase = true) ||
                    page.id.contains(searchQuery, ignoreCase = true)

            val matchesTab = when (selectedTab) {
                PageFilterTab.ALL -> true
                PageFilterTab.ACTIVE -> !page.isBlocked
                PageFilterTab.BLOCKED -> page.isBlocked
                PageFilterTab.VERIFIED -> page.isBadgeActive()
            }

            matchesSearch && matchesTab
        }
    }

    val totalCount = allPages.size
    val activeCount = allPages.count { !it.isBlocked }
    val blockedCount = allPages.count { it.isBlocked }
    val verifiedCount = allPages.count { it.isBadgeActive() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Page Management",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF050505)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE91E63).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "$totalCount",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE91E63),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Manage and oversee all user pages",
                            fontSize = 11.sp,
                            color = Color(0xFF65676B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_page_back_btn")) {
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
        modifier = modifier.testTag("admin_page_management_screen")
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
                        AdminStatMiniItem(title = "Total Pages", value = "$totalCount", color = Color(0xFFE91E63))
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
                    placeholder = { Text("Search by page name, category, email, ID...", fontSize = 14.sp) },
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
                        focusedBorderColor = Color(0xFFE91E63),
                        unfocusedBorderColor = Color(0xFFCED0D4)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("admin_page_search_input")
                )
            }

            // Filter Tabs
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PageFilterTab.values()) { tab ->
                        val isSelected = selectedTab == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            label = {
                                val count = when (tab) {
                                    PageFilterTab.ALL -> totalCount
                                    PageFilterTab.ACTIVE -> activeCount
                                    PageFilterTab.BLOCKED -> blockedCount
                                    PageFilterTab.VERIFIED -> verifiedCount
                                }
                                Text("${tab.title} ($count)", fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE91E63),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF050505)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Empty state or Page List
            if (filteredPages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = Color(0xFFBCC0C4),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No pages found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF65676B)
                            )
                            Text(
                                text = "When users create pages, they will appear here",
                                fontSize = 12.sp,
                                color = Color(0xFF8A8D91)
                            )
                        }
                    }
                }
            } else {
                items(filteredPages, key = { it.id }) { page ->
                    AdminPageCard(
                        page = page,
                        onEdit = { editingPage = page },
                        onToggleBlock = {
                            val newStatus = !page.isBlocked
                            repository.setPageBlocked(page.id, newStatus)
                            Toast.makeText(
                                context,
                                if (newStatus) "Page '${page.name}' blocked" else "Page '${page.name}' unblocked",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onManageBadge = { badgeManagingPage = page },
                        onManageExpiry = { expiryManagingPage = page },
                        onDelete = { deletingPage = page }
                    )
                }
            }
        }
    }

    // 1. Edit Page Dialog
    editingPage?.let { page ->
        AdminEditPageDialog(
            page = page,
            onDismiss = { editingPage = null },
            onSave = { updated ->
                repository.updatePage(updated)
                editingPage = null
                Toast.makeText(context, "Page successfully updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 2. Delete Confirmation Dialog
    deletingPage?.let { page ->
        AlertDialog(
            onDismissRequest = { deletingPage = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F)) },
            title = { Text("Delete Page?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to permanently delete '${page.name}' from the database? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.deletePage(page.id)
                        deletingPage = null
                        Toast.makeText(context, "Page deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Yes, Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingPage = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. Verification Badge Dialog (On/Off & Type)
    badgeManagingPage?.let { page ->
        AdminPageBadgeDialog(
            page = page,
            onDismiss = { badgeManagingPage = null },
            onSave = { isVerified, badgeType, expiresAt ->
                repository.setPageVerification(page.id, isVerified, badgeType, expiresAt)
                badgeManagingPage = null
                Toast.makeText(context, "Verification badge updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 4. Verification Expiry / Validity Dialog (Increase / Decrease)
    expiryManagingPage?.let { page ->
        AdminBadgeExpiryDialog(
            title = "Adjust Page Badge Expiry",
            currentExpiry = page.badgeExpiresAt,
            onDismiss = { expiryManagingPage = null },
            onAdjustDays = { days ->
                repository.adjustPageBadgeExpiry(page.id, days)
                expiryManagingPage = null
                Toast.makeText(context, "Badge expiry updated", Toast.LENGTH_SHORT).show()
            },
            onSetCustomExpiry = { expiryMs ->
                repository.setPageVerification(page.id, true, page.badgeType, expiryMs)
                expiryManagingPage = null
                Toast.makeText(context, "Badge expiry updated", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun AdminPageCard(
    page: PageItem,
    onEdit: () -> Unit,
    onToggleBlock: () -> Unit,
    onManageBadge: () -> Unit,
    onManageExpiry: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val isVerifiedActive = page.isBadgeActive()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("admin_page_card_${page.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Avatar + Title + Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Avatar
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFCE4EC),
                    modifier = Modifier.size(54.dp)
                ) {
                    if (page.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = page.avatarUrl,
                            contentDescription = page.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = Color(0xFFE91E63),
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
                            text = page.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF050505),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isVerifiedActive) {
                            Spacer(modifier = Modifier.width(4.dp))
                            if (page.badgeType == "GREEN") {
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
                            color = Color(0xFFFCE4EC)
                        ) {
                            Text(
                                text = page.category,
                                fontSize = 11.sp,
                                color = Color(0xFFC2185B),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE3F2FD)
                        ) {
                            Text(
                                text = "${page.followersCount} followers",
                                fontSize = 11.sp,
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = "${page.likesCount} likes",
                                fontSize = 11.sp,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Block status badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (page.isBlocked) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = if (page.isBlocked) "Blocked" else "Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (page.isBlocked) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Description
            if (page.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = page.description,
                    fontSize = 13.sp,
                    color = Color(0xFF333333),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Contact / Website info if available
            if (page.website.isNotBlank() || page.email.isNotBlank() || page.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (page.website.isNotBlank()) {
                        Text("🌐 ${page.website}", fontSize = 11.sp, color = Color(0xFF1877F2), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (page.email.isNotBlank()) {
                        Text("✉️ ${page.email}", fontSize = 11.sp, color = Color(0xFF65676B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
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
                        text = "Created: ${dateFormat.format(Date(page.createdAt))}",
                        fontSize = 11.sp,
                        color = Color(0xFF8A8D91)
                    )
                    if (page.creatorId.isNotBlank()) {
                        Text(
                            text = "Creator ID: ${page.creatorId}",
                            fontSize = 10.sp,
                            color = Color(0xFF8A8D91),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Verification Badge details
                if (page.isVerified) {
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
                            val expiryText = if (page.badgeExpiresAt == 0L) {
                                "Lifetime"
                            } else if (isVerifiedActive) {
                                val remainingDays = ((page.badgeExpiresAt - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
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
                        containerColor = if (page.isBlocked) Color(0xFF2E7D32) else Color(0xFFE53935)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.1f).height(38.dp)
                ) {
                    Icon(
                        imageVector = if (page.isBlocked) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (page.isBlocked) "Unblock" else "Block",
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
                        tint = if (page.isVerified) Color(0xFF1877F2) else Color(0xFF65676B)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (page.isVerified) "Badge ON" else "Badge OFF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (page.isVerified) Color(0xFF1877F2) else Color(0xFF65676B)
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
 * Edit Page Dialog for Admins
 */
@Composable
fun AdminEditPageDialog(
    page: PageItem,
    onDismiss: () -> Unit,
    onSave: (PageItem) -> Unit
) {
    var name by remember { mutableStateOf(page.name) }
    var category by remember { mutableStateOf(page.category) }
    var description by remember { mutableStateOf(page.description) }
    var coverUrl by remember { mutableStateOf(page.coverUrl) }
    var avatarUrl by remember { mutableStateOf(page.avatarUrl) }
    var followersCountStr by remember { mutableStateOf(page.followersCount.toString()) }
    var likesCountStr by remember { mutableStateOf(page.likesCount.toString()) }
    var website by remember { mutableStateOf(page.website) }
    var email by remember { mutableStateOf(page.email) }
    var phone by remember { mutableStateOf(page.phone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Page Details", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Page Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Creator, Business, Media...)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = followersCountStr,
                        onValueChange = { followersCountStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Followers Count") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = likesCountStr,
                        onValueChange = { likesCountStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Likes Count") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = avatarUrl,
                    onValueChange = { avatarUrl = it },
                    label = { Text("Profile/Logo URL") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = coverUrl,
                    onValueChange = { coverUrl = it },
                    label = { Text("Cover Image URL") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Website URL (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Page Description (Bio/Description)") },
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
                        val updated = page.copy(
                            name = name.trim(),
                            category = category.trim().ifBlank { "Creator" },
                            description = description.trim(),
                            coverUrl = coverUrl.trim(),
                            avatarUrl = avatarUrl.trim(),
                            followersCount = followersCountStr.toIntOrNull() ?: page.followersCount,
                            likesCount = likesCountStr.toIntOrNull() ?: page.likesCount,
                            website = website.trim(),
                            email = email.trim(),
                            phone = phone.trim()
                        )
                        onSave(updated)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
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
 * Manage Verification Badge (Turn on/off, select Blue or Green, set initial validity) for Pages
 */
@Composable
fun AdminPageBadgeDialog(
    page: PageItem,
    onDismiss: () -> Unit,
    onSave: (isVerified: Boolean, badgeType: String, expiresAt: Long) -> Unit
) {
    var isVerified by remember { mutableStateOf(page.isVerified) }
    var badgeType by remember { mutableStateOf(page.badgeType) }
    var validityOption by remember { mutableStateOf("LIFETIME") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF1877F2))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Page Verification Badge Control", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                            text = if (isVerified) "Verified badge icon displayed next to page" else "Badge is disabled",
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
