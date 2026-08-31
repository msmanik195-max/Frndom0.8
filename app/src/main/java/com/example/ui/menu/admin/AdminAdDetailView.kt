package com.example.ui.menu.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AdvertisementItem
import com.example.data.repository.AdvertisementRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.WalletRepository
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAdDetailView(
    adId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adRepo = remember { AdvertisementRepository.getInstance(context) }
    val walletRepo = remember { WalletRepository.getInstance(context) }
    val userRepo = remember { UserRepository(context) }

    val allAds by adRepo.advertisementsFlow.collectAsState()
    val ad = remember(allAds, adId) { allAds.firstOrNull { it.id == adId } }

    val allUsers by userRepo.getAllUsersFlow().collectAsState(initial = emptyList())
    val advertiserProfile = remember(allUsers, ad?.userId) {
        allUsers.firstOrNull { it.uid == ad?.userId }
    }

    if (ad == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ad Details") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Ad not found or has been deleted.")
            }
        }
        return
    }

    var selectedStatus by remember(ad.status) { mutableStateOf(ad.status) }
    var adminNoteInput by remember(ad.adminNote) { mutableStateOf(ad.adminNote) }
    var refundToWalletOnReject by remember { mutableStateOf(true) }
    var showStatusUpdateDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    val (statusLabel, statusBg, statusColor) = when (ad.status) {
        "RUNNING" -> Triple("RUNNING", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "PAUSED" -> Triple("PAUSED", Color(0xFFFFF3E0), Color(0xFFE65100))
        "PENDING" -> Triple("PENDING", Color(0xFFE3F2FD), Color(0xFF1565C0))
        "COMPLETED" -> Triple("COMPLETED", Color(0xFFECEFF1), Color(0xFF455A64))
        "REJECTED" -> Triple("REJECTED", Color(0xFFFFEBEE), Color(0xFFC62828))
        else -> Triple(ad.status, Color(0xFFF5F5F5), Color(0xFF616161))
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_ad_detail_view"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Advertisement Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF050505)
                        )
                        Text(
                            text = "Campaign ID: #${ad.id.take(8)}",
                            fontSize = 12.sp,
                            color = Color(0xFF1877F2)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_ad_detail_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF050505)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showStatusUpdateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 12.dp).testTag("admin_change_status_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Action", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Action button bar at bottom
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (ad.status == "PENDING" || ad.status == "PAUSED") {
                        Button(
                            onClick = {
                                adRepo.updateAdvertisementStatus(
                                    adId = ad.id,
                                    newStatus = "RUNNING",
                                    adminNote = "Approved by Admin"
                                )
                                Toast.makeText(context, "Campaign Approved as RUNNING!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008937)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Approve", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (ad.status != "REJECTED") {
                        Button(
                            onClick = {
                                showStatusUpdateDialog = true
                                selectedStatus = "REJECTED"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reject / Options", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { showStatusUpdateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Change Status", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F2F5))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Status Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Current Status", fontSize = 12.sp, color = Color(0xFF65676B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = statusBg
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                if (ad.status == "RUNNING") {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = statusLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = statusColor
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Total Deducted", fontSize = 12.sp, color = Color(0xFF65676B))
                        Text(
                            text = "BDT ${String.format(Locale.US, "%.2f", ad.totalBudget)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1877F2)
                        )
                    }
                }
            }

            // 2. Advertiser Information Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Advertiser Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF050505)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1877F2),
                            modifier = Modifier.size(46.dp)
                        ) {
                            val avatar = advertiserProfile?.profilePictureUrl ?: ad.userAvatar
                            if (avatar.isNotBlank()) {
                                AsyncImage(
                                    model = avatar,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = ad.userName.firstOrNull()?.uppercase() ?: "U",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = advertiserProfile?.fullName.orEmpty().ifBlank { ad.userName },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF050505)
                            )
                            Text(
                                text = "User ID: ${ad.userId.ifBlank { "N/A" }}",
                                fontSize = 12.sp,
                                color = Color(0xFF65676B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color(0xFFE4E6EB))
                    Spacer(modifier = Modifier.height(10.dp))

                    val email = advertiserProfile?.email.orEmpty().ifBlank { ad.userEmail }.ifBlank { "Not provided" }
                    val phone = advertiserProfile?.phoneNumber.orEmpty().ifBlank { advertiserProfile?.contactPhone.orEmpty() }.ifBlank { ad.userPhone }.ifBlank { "Not provided" }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Email:", fontSize = 13.sp, color = Color(0xFF65676B))
                        Text(email, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF050505))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Phone:", fontSize = 13.sp, color = Color(0xFF65676B))
                        Text(phone, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF050505))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Wallet Status:", fontSize = 13.sp, color = Color(0xFF65676B))
                        Text("Active & Deducted", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF008937))
                    }
                }
            }

            // 3. Campaign & Creative Details Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ad Creative & Content",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF050505)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Campaign Name:", fontSize = 12.sp, color = Color(0xFF65676B))
                    Text(text = ad.campaignName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF050505))

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Goal / Objective:", fontSize = 12.sp, color = Color(0xFF65676B))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1877F2).copy(alpha = 0.1f),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = ad.campaignGoal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1877F2),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Headline:", fontSize = 12.sp, color = Color(0xFF65676B))
                    Text(text = ad.headline, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF050505))

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Primary Text / Description:", fontSize = 12.sp, color = Color(0xFF65676B))
                    Text(text = ad.description, fontSize = 14.sp, color = Color(0xFF333333))

                    Spacer(modifier = Modifier.height(12.dp))

                    if (ad.mediaUrl.isNotBlank()) {
                        Text(text = "Ad Banner Image:", fontSize = 12.sp, color = Color(0xFF65676B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = ad.mediaUrl,
                                contentDescription = "Ad Creative",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Destination Link:", fontSize = 13.sp, color = Color(0xFF65676B))
                        Text(
                            text = ad.destinationUrl,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1877F2)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("CTA Button:", fontSize = 13.sp, color = Color(0xFF65676B))
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF1877F2)) {
                            Text(
                                text = ad.callToAction,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // 4. Audience & Budget Specs
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Analytics, contentDescription = null, tint = Color(0xFF673AB7), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Targeting & Performance Budget",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF050505)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Location:", fontSize = 13.sp, color = Color(0xFF65676B))
                        Text(ad.targetLocation, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Age Range:", fontSize = 13.sp, color = Color(0xFF65676B))
                        Text(ad.targetAgeRange, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gender:", fontSize = 13.sp, color = Color(0xFF65676B))
                        Text(ad.targetGender, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Daily Budget:", fontSize = 13.sp, color = Color(0xFF65676B))
                        Text("BDT ${ad.dailyBudget} / day", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Duration:", fontSize = 13.sp, color = Color(0xFF65676B))
                        Text("${ad.durationDays} Days", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Estimated Reach:", fontSize = 13.sp, color = Color(0xFF65676B))
                        Text("~${ad.estimatedReach} People", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF008937))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Created Date:", fontSize = 13.sp, color = Color(0xFF65676B))
                        Text(dateFormat.format(Date(ad.createdAt)), fontSize = 13.sp)
                    }
                }
            }

            if (ad.adminNote.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Admin Remarks:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE65100))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(ad.adminNote, fontSize = 13.sp, color = Color(0xFFBF360C))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Admin Decision / Change Status Dialog
    if (showStatusUpdateDialog) {
        val statuses = listOf("RUNNING", "PAUSED", "COMPLETED", "REJECTED", "PENDING")

        AlertDialog(
            onDismissRequest = { showStatusUpdateDialog = false },
            title = {
                Text(
                    text = "Update Advertisement Status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Select what status to apply to this campaign:",
                        fontSize = 13.sp,
                        color = Color(0xFF65676B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    statuses.forEach { statusOption ->
                        val isSelected = selectedStatus == statusOption
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF1877F2).copy(alpha = 0.1f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedStatus = statusOption }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedStatus = statusOption },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1877F2))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    val (name, desc) = when (statusOption) {
                                        "RUNNING" -> "RUNNING (Live Ad)" to "Ad will be actively delivered to users"
                                        "PAUSED" -> "PAUSED (Temporarily Halted)" to "Campaign temporarily halted"
                                        "COMPLETED" -> "COMPLETED (Finished)" to "Campaign finished duration"
                                        "REJECTED" -> "REJECTED (Cancelled)" to "Ad rejected with optional refund"
                                        "PENDING" -> "PENDING (Under Review)" to "Under review"
                                        else -> statusOption to ""
                                    }
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(desc, fontSize = 11.sp, color = Color(0xFF65676B))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // If REJECTED, option to refund wallet
                    if (selectedStatus == "REJECTED") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = refundToWalletOnReject,
                                onCheckedChange = { refundToWalletOnReject = it }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Refund BDT ${ad.totalBudget} back to user's wallet",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF008937)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    OutlinedTextField(
                        value = adminNoteInput,
                        onValueChange = { adminNoteInput = it },
                        label = { Text("Admin Note / Remarks") },
                        placeholder = { Text("Optional message to the advertiser...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStatusUpdateDialog = false
                        val success = adRepo.updateAdvertisementStatus(
                            adId = ad.id,
                            newStatus = selectedStatus,
                            adminNote = adminNoteInput.trim(),
                            walletRepo = walletRepo,
                            refundWallet = (selectedStatus == "REJECTED" && refundToWalletOnReject)
                        )
                        if (success) {
                            Toast.makeText(context, "Campaign updated to $selectedStatus!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                ) {
                    Text("Apply Status")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusUpdateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
