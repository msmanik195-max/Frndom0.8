package com.example.ui.menu.admin

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AdvertisementItem
import com.example.data.repository.AdvertisementRepository
import com.example.data.repository.WalletRepository
import java.text.SimpleDateFormat
import java.util.*

enum class AdminAdFilterTab {
    ALL,
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    REJECTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAdvertisementManagementView(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adRepo = remember { AdvertisementRepository.getInstance(context) }
    val walletRepo = remember { WalletRepository.getInstance(context) }

    val allAds by adRepo.advertisementsFlow.collectAsState()

    var selectedFilterTab by remember { mutableStateOf(AdminAdFilterTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var viewingAdId by remember { mutableStateOf<String?>(null) }

    // Quick status action dialog
    var adToQuickAction by remember { mutableStateOf<AdvertisementItem?>(null) }

    // If viewing single ad detail page
    viewingAdId?.let { id ->
        AdminAdDetailView(
            adId = id,
            onBack = { viewingAdId = null },
            modifier = modifier
        )
        return
    }

    val pendingCount = allAds.count { it.status == "PENDING" }
    val runningCount = allAds.count { it.status == "RUNNING" }
    val pausedCount = allAds.count { it.status == "PAUSED" }
    val completedCount = allAds.count { it.status == "COMPLETED" }
    val rejectedCount = allAds.count { it.status == "REJECTED" }
    val totalRevenue = allAds.sumOf { it.totalBudget }

    val filteredAds = remember(allAds, selectedFilterTab, searchQuery) {
        allAds.filter { ad ->
            val matchesTab = when (selectedFilterTab) {
                AdminAdFilterTab.ALL -> true
                AdminAdFilterTab.PENDING -> ad.status == "PENDING"
                AdminAdFilterTab.RUNNING -> ad.status == "RUNNING"
                AdminAdFilterTab.PAUSED -> ad.status == "PAUSED"
                AdminAdFilterTab.COMPLETED -> ad.status == "COMPLETED"
                AdminAdFilterTab.REJECTED -> ad.status == "REJECTED"
            }
            val matchesSearch = searchQuery.isBlank() ||
                    ad.campaignName.contains(searchQuery, ignoreCase = true) ||
                    ad.userName.contains(searchQuery, ignoreCase = true) ||
                    ad.headline.contains(searchQuery, ignoreCase = true)
            matchesTab && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_ad_management_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Advertisement Manage",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF050505)
                        )
                        Text(
                            text = "${allAds.size} total campaigns • $pendingCount pending",
                            fontSize = 12.sp,
                            color = if (pendingCount > 0) Color(0xFFE53935) else Color(0xFF008937),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_ad_management_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF050505)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F2F5))
                .padding(innerPadding)
        ) {
            // Summary Stats Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pending Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Pending", fontSize = 11.sp, color = Color(0xFFE65100), fontWeight = FontWeight.SemiBold)
                        Text("$pendingCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                    }
                }

                // Running Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Running", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                        Text("$runningCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }

                // Total Ad Volume Card
                Card(
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Ad Revenue", fontSize = 11.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold)
                        Text("BDT ${String.format(Locale.US, "%.0f", totalRevenue)}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                    }
                }
            }

            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.White
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by campaign or advertiser...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF65676B)) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF65676B))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_ad_search_input"),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Tabs Table (Approved, Rejected, Pending)
            ScrollableTabRow(
                selectedTabIndex = selectedFilterTab.ordinal,
                containerColor = Color.White,
                contentColor = Color(0xFF1877F2),
                edgePadding = 12.dp,
                divider = { Divider(color = Color(0xFFE4E6EB)) }
            ) {
                Tab(
                    selected = selectedFilterTab == AdminAdFilterTab.ALL,
                    onClick = { selectedFilterTab = AdminAdFilterTab.ALL },
                    text = { Text("All (${allAds.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedFilterTab == AdminAdFilterTab.PENDING,
                    onClick = { selectedFilterTab = AdminAdFilterTab.PENDING },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Pending ($pendingCount)", fontWeight = FontWeight.Bold)
                            if (pendingCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE53935),
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedFilterTab == AdminAdFilterTab.RUNNING,
                    onClick = { selectedFilterTab = AdminAdFilterTab.RUNNING },
                    text = { Text("Approved / Running ($runningCount)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedFilterTab == AdminAdFilterTab.PAUSED,
                    onClick = { selectedFilterTab = AdminAdFilterTab.PAUSED },
                    text = { Text("Paused ($pausedCount)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedFilterTab == AdminAdFilterTab.COMPLETED,
                    onClick = { selectedFilterTab = AdminAdFilterTab.COMPLETED },
                    text = { Text("Completed ($completedCount)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedFilterTab == AdminAdFilterTab.REJECTED,
                    onClick = { selectedFilterTab = AdminAdFilterTab.REJECTED },
                    text = { Text("Rejected ($rejectedCount)", fontWeight = FontWeight.Bold) }
                )
            }

            // List of Campaigns
            if (filteredAds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFB0B3B8), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No advertisements found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF65676B)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAds, key = { it.id }) { ad ->
                        AdminAdCard(
                            ad = ad,
                            onViewClick = { viewingAdId = ad.id },
                            onQuickAction = { adToQuickAction = ad }
                        )
                    }
                }
            }
        }
    }

    // Quick Action Dialog for Admin
    adToQuickAction?.let { ad ->
        var targetStatus by remember { mutableStateOf(ad.status) }
        var adminNote by remember { mutableStateOf(ad.adminNote) }
        var refundUser by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { adToQuickAction = null },
            title = {
                Text("Manage Ad: ${ad.campaignName}", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Select Status:", fontSize = 12.sp, color = Color(0xFF65676B))
                    Spacer(modifier = Modifier.height(6.dp))

                    listOf("RUNNING", "PAUSED", "COMPLETED", "REJECTED", "PENDING").forEach { st ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { targetStatus = st }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = targetStatus == st,
                                onClick = { targetStatus = st },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1877F2))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (st) {
                                    "RUNNING" -> "Approve as RUNNING"
                                    "PAUSED" -> "Set to PAUSED"
                                    "COMPLETED" -> "Set to COMPLETED"
                                    "REJECTED" -> "REJECT Campaign"
                                    "PENDING" -> "Set to PENDING"
                                    else -> st
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (targetStatus == "REJECTED") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = refundUser,
                                onCheckedChange = { refundUser = it }
                            )
                            Text("Refund BDT ${ad.totalBudget} to user's wallet", fontSize = 12.sp, color = Color(0xFF008937), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = adminNote,
                        onValueChange = { adminNote = it },
                        label = { Text("Admin Note") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = adRepo.updateAdvertisementStatus(
                            adId = ad.id,
                            newStatus = targetStatus,
                            adminNote = adminNote.trim(),
                            walletRepo = walletRepo,
                            refundWallet = (targetStatus == "REJECTED" && refundUser)
                        )
                        if (success) {
                            Toast.makeText(context, "Status updated to $targetStatus", Toast.LENGTH_SHORT).show()
                        }
                        adToQuickAction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { adToQuickAction = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminAdCard(
    ad: AdvertisementItem,
    onViewClick: () -> Unit,
    onQuickAction: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    val (statusLabel, statusBg, statusColor) = when (ad.status) {
        "RUNNING" -> Triple("RUNNING", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "PAUSED" -> Triple("PAUSED", Color(0xFFFFF3E0), Color(0xFFE65100))
        "PENDING" -> Triple("PENDING", Color(0xFFE3F2FD), Color(0xFF1565C0))
        "COMPLETED" -> Triple("COMPLETED", Color(0xFFECEFF1), Color(0xFF455A64))
        "REJECTED" -> Triple("REJECTED", Color(0xFFFFEBEE), Color(0xFFC62828))
        else -> Triple(ad.status, Color(0xFFF5F5F5), Color(0xFF616161))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_ad_card_${ad.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Advertiser & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1877F2),
                        modifier = Modifier.size(34.dp)
                    ) {
                        if (ad.userAvatar.isNotBlank()) {
                            AsyncImage(
                                model = ad.userAvatar,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = ad.userName.firstOrNull()?.uppercase() ?: "A",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = ad.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF050505)
                        )
                        Text(
                            text = dateFormat.format(Date(ad.createdAt)),
                            fontSize = 11.sp,
                            color = Color(0xFF65676B)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Campaign Title & Creative Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (ad.mediaUrl.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = ad.mediaUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ad.campaignName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF050505),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = ad.headline,
                        fontSize = 12.sp,
                        color = Color(0xFF65676B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Budget: BDT ${String.format(Locale.US, "%.0f", ad.totalBudget)} (${ad.durationDays}d @ BDT ${ad.dailyBudget})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1877F2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFE4E6EB))
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Buttons: "View" button and "Action" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1877F2).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = ad.campaignGoal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1877F2),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onQuickAction,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("quick_action_${ad.id}")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Action", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onViewClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("view_ad_${ad.id}")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
