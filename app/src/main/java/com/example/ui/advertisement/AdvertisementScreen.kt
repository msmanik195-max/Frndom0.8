package com.example.ui.advertisement

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import com.example.data.model.UserProfile
import com.example.data.repository.AdvertisementRepository
import com.example.data.repository.AppSettingsRepository
import com.example.data.repository.WalletRepository
import java.text.SimpleDateFormat
import java.util.*

enum class AdUserTab {
    RUNNING,
    PAUSED,
    PENDING,
    COMPLETED,
    ALL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertisementScreen(
    currentUser: UserProfile?,
    onBack: () -> Unit,
    onNavigateToDeposit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adRepo = remember { AdvertisementRepository.getInstance(context) }
    val walletRepo = remember { WalletRepository.getInstance(context) }
    val appSettingsRepo = remember { AppSettingsRepository.getInstance(context) }
    val isDarkMode by appSettingsRepo.isDarkMode.collectAsState()

    val walletBalance by walletRepo.balanceFlow.collectAsState()
    val allAds by adRepo.advertisementsFlow.collectAsState()

    // Filter ads for current user (or show all if user list is small/demo)
    val userAds = remember(allAds, currentUser?.uid) {
        val uid = currentUser?.uid.orEmpty()
        val filtered = allAds.filter { it.userId == uid }
        if (filtered.isEmpty()) allAds else filtered
    }

    var selectedTab by remember { mutableStateOf(AdUserTab.RUNNING) }
    var isCreatingNewAd by remember { mutableStateOf(false) }
    var selectedAdForDetail by remember { mutableStateOf<AdvertisementItem?>(null) }

    val bgMain = if (isDarkMode) Color(0xFF18191A) else Color(0xFFF0F2F5)
    val cardBg = if (isDarkMode) Color(0xFF242526) else Color.White
    val textPrimary = if (isDarkMode) Color(0xFFE4E6EB) else Color(0xFF050505)
    val textSecondary = if (isDarkMode) Color(0xFFB0B3B8) else Color(0xFF65676B)
    val borderCol = if (isDarkMode) Color(0xFF3A3B3C) else Color(0xFFE4E6EB)

    // Running count, Paused count, Pending count, Completed count
    val runningCount = userAds.count { it.status == "RUNNING" }
    val pausedCount = userAds.count { it.status == "PAUSED" }
    val pendingCount = userAds.count { it.status == "PENDING" }
    val completedCount = userAds.count { it.status == "COMPLETED" }

    // If in Create Ad screen
    if (isCreatingNewAd) {
        CreateAdScreen(
            currentUser = currentUser,
            onBack = { isCreatingNewAd = false },
            onAdCreated = {
                isCreatingNewAd = false
                selectedTab = AdUserTab.PENDING
            },
            onNavigateToDeposit = onNavigateToDeposit,
            modifier = modifier
        )
        return
    }

    // Detail dialog
    selectedAdForDetail?.let { ad ->
        UserAdDetailDialog(
            ad = ad,
            isDarkMode = isDarkMode,
            onDismiss = { selectedAdForDetail = null },
            onTogglePause = {
                adRepo.togglePauseResume(ad.id)
                selectedAdForDetail = null
            }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("advertisement_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ads Manager",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = textPrimary
                        )
                        Text(
                            text = "Facebook Style Ad Center",
                            fontSize = 12.sp,
                            color = Color(0xFF1877F2)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("ad_screen_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                },
                actions = {
                    // Quick Wallet Balance chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF00C853).copy(alpha = 0.15f),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable(onClick = onNavigateToDeposit)
                            .testTag("ad_screen_wallet_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFF008937),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "BDT ${String.format(Locale.US, "%.0f", walletBalance)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF008937)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Deposit",
                                tint = Color(0xFF008937),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cardBg,
                    titleContentColor = textPrimary
                )
            )
        },
        floatingActionButton = {
            // Prominent "+ New Ad" button requested by user
            ExtendedFloatingActionButton(
                onClick = { isCreatingNewAd = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "New Ad") },
                text = {
                    Text(
                        text = "New Ad",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                },
                containerColor = Color(0xFF1877F2),
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("new_ad_fab_button")
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgMain)
                .padding(innerPadding)
        ) {
            // 4 Tabs at the top: Running, Paused, Pending, Completed (+ All)
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = cardBg,
                contentColor = Color(0xFF1877F2),
                edgePadding = 12.dp,
                divider = { Divider(color = borderCol) }
            ) {
                Tab(
                    selected = selectedTab == AdUserTab.RUNNING,
                    onClick = { selectedTab = AdUserTab.RUNNING },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Running ($runningCount)", fontWeight = FontWeight.Bold)
                            if (runningCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00C853))
                                )
                            }
                        }
                    }
                )

                Tab(
                    selected = selectedTab == AdUserTab.PAUSED,
                    onClick = { selectedTab = AdUserTab.PAUSED },
                    text = { Text("Paused ($pausedCount)", fontWeight = FontWeight.Bold) }
                )

                Tab(
                    selected = selectedTab == AdUserTab.PENDING,
                    onClick = { selectedTab = AdUserTab.PENDING },
                    text = { Text("Pending ($pendingCount)", fontWeight = FontWeight.Bold) }
                )

                Tab(
                    selected = selectedTab == AdUserTab.COMPLETED,
                    onClick = { selectedTab = AdUserTab.COMPLETED },
                    text = { Text("Completed ($completedCount)", fontWeight = FontWeight.Bold) }
                )

                Tab(
                    selected = selectedTab == AdUserTab.ALL,
                    onClick = { selectedTab = AdUserTab.ALL },
                    text = { Text("All (${userAds.size})", fontWeight = FontWeight.Bold) }
                )
            }

            // Ads List for selected Tab
            val displayedAds = remember(selectedTab, userAds) {
                when (selectedTab) {
                    AdUserTab.RUNNING -> userAds.filter { it.status == "RUNNING" }
                    AdUserTab.PAUSED -> userAds.filter { it.status == "PAUSED" }
                    AdUserTab.PENDING -> userAds.filter { it.status == "PENDING" }
                    AdUserTab.COMPLETED -> userAds.filter { it.status == "COMPLETED" }
                    AdUserTab.ALL -> userAds
                }
            }

            if (displayedAds.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1877F2).copy(alpha = 0.1f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (selectedTab) {
                                        AdUserTab.RUNNING -> Icons.Default.Campaign
                                        AdUserTab.PAUSED -> Icons.Default.PauseCircle
                                        AdUserTab.PENDING -> Icons.Default.Schedule
                                        AdUserTab.COMPLETED -> Icons.Default.DoneAll
                                        AdUserTab.ALL -> Icons.Default.AddBusiness
                                    },
                                    contentDescription = null,
                                    tint = Color(0xFF1877F2),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = when (selectedTab) {
                                AdUserTab.RUNNING -> "No Active Running Ads"
                                AdUserTab.PAUSED -> "No Paused Ads"
                                AdUserTab.PENDING -> "No Pending Ads Under Review"
                                AdUserTab.COMPLETED -> "No Completed Ads"
                                AdUserTab.ALL -> "No Advertisements Created Yet"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = textPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Click the \"+ New Ad\" button below to start running advertisements and reaching thousands of customers!",
                            fontSize = 13.sp,
                            color = textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { isCreatingNewAd = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create New Ad", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp, start = 14.dp, end = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedAds, key = { it.id }) { ad ->
                        UserAdCard(
                            ad = ad,
                            isDarkMode = isDarkMode,
                            onCardClick = { selectedAdForDetail = ad },
                            onTogglePause = { adRepo.togglePauseResume(ad.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserAdCard(
    ad: AdvertisementItem,
    isDarkMode: Boolean,
    onCardClick: () -> Unit,
    onTogglePause: () -> Unit
) {
    val cardBg = if (isDarkMode) Color(0xFF242526) else Color.White
    val textPrimary = if (isDarkMode) Color(0xFFE4E6EB) else Color(0xFF050505)
    val textSecondary = if (isDarkMode) Color(0xFFB0B3B8) else Color(0xFF65676B)
    val borderCol = if (isDarkMode) Color(0xFF3A3B3C) else Color(0xFFE4E6EB)

    val (statusText, statusBg, statusColor) = when (ad.status) {
        "RUNNING" -> Triple("RUNNING", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "PAUSED" -> Triple("PAUSED", Color(0xFFFFF3E0), Color(0xFFE65100))
        "PENDING" -> Triple("PENDING", Color(0xFFE3F2FD), Color(0xFF1565C0))
        "COMPLETED" -> Triple("COMPLETED", Color(0xFFECEFF1), Color(0xFF455A64))
        "REJECTED" -> Triple("REJECTED", Color(0xFFFFEBEE), Color(0xFFC62828))
        else -> Triple(ad.status, Color(0xFFF5F5F5), Color(0xFF616161))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("user_ad_card_${ad.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Goal Pill & Status Badge
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
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBg
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        if (ad.status == "RUNNING") {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle: Banner image thumbnail & info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (ad.mediaUrl.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .size(74.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = ad.mediaUrl,
                            contentDescription = "Ad Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ad.campaignName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = ad.headline,
                        fontSize = 13.sp,
                        color = textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total: BDT ${String.format(Locale.US, "%.0f", ad.totalBudget)} (${ad.durationDays}d @ BDT ${String.format(Locale.US, "%.0f", ad.dailyBudget)}/d)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1877F2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = borderCol)
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Metrics Bar & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(text = "Reach", fontSize = 10.sp, color = textSecondary)
                        Text(
                            text = if (ad.impressions > 0) "${ad.impressions}" else "~${ad.estimatedReach}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                    }
                    Column {
                        Text(text = "Clicks", fontSize = 10.sp, color = textSecondary)
                        Text(
                            text = if (ad.clicks > 0) "${ad.clicks}" else "~${ad.estimatedClicks}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (ad.status == "RUNNING" || ad.status == "PAUSED") {
                        OutlinedButton(
                            onClick = onTogglePause,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = if (ad.status == "RUNNING") Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (ad.status == "RUNNING") Color(0xFFE65100) else Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (ad.status == "RUNNING") "Pause" else "Resume",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ad.status == "RUNNING") Color(0xFFE65100) else Color(0xFF2E7D32)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    TextButton(
                        onClick = onCardClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "View Details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1877F2)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserAdDetailDialog(
    ad: AdvertisementItem,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onTogglePause: () -> Unit
) {
    val textPrimary = if (isDarkMode) Color(0xFFE4E6EB) else Color(0xFF050505)
    val textSecondary = if (isDarkMode) Color(0xFFB0B3B8) else Color(0xFF65676B)
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = ad.campaignName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                if (ad.mediaUrl.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = ad.mediaUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = ad.headline,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ad.description,
                    fontSize = 13.sp,
                    color = textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Status:", fontSize = 12.sp, color = textSecondary)
                    Text(ad.status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1877F2))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Budget:", fontSize = 12.sp, color = textSecondary)
                    Text("BDT ${ad.totalBudget}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Duration:", fontSize = 12.sp, color = textSecondary)
                    Text("${ad.durationDays} Days (BDT ${ad.dailyBudget}/day)", fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Target Audience:", fontSize = 12.sp, color = textSecondary)
                    Text("${ad.targetLocation} • ${ad.targetAgeRange}", fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Created On:", fontSize = 12.sp, color = textSecondary)
                    Text(dateFormat.format(Date(ad.createdAt)), fontSize = 12.sp)
                }

                if (ad.adminNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Admin Note: ${ad.adminNote}",
                            fontSize = 12.sp,
                            color = Color(0xFFE65100),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (ad.status == "RUNNING" || ad.status == "PAUSED") {
                Button(
                    onClick = onTogglePause,
                    colors = ButtonDefaults.buttonColors(containerColor = if (ad.status == "RUNNING") Color(0xFFE65100) else Color(0xFF2E7D32))
                ) {
                    Text(if (ad.status == "RUNNING") "Pause Ad" else "Resume Ad")
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}
