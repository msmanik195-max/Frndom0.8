package com.example.ui.menu.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AdminRequestRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsView(
    adminRepo: AdminRequestRepository,
    onBack: () -> Unit
) {
    val currentSettings by adminRepo.monetizationSettingsFlow.collectAsState()
    
    var reelRate by remember { mutableStateOf(currentSettings.reelRatePer1000.toString()) }
    var imageRate by remember { mutableStateOf(currentSettings.imageRatePer1000.toString()) }
    var textRate by remember { mutableStateOf(currentSettings.textRatePer1000.toString()) }
    var minTransfer by remember { mutableStateOf(currentSettings.minTransferAmount.toString()) }
    
    var reqViews by remember { mutableStateOf(currentSettings.reqTotalViews.toString()) }
    var reqFollowers by remember { mutableStateOf(currentSettings.reqTotalFollowers.toString()) }
    var reqPosts by remember { mutableStateOf(currentSettings.reqTotalPosts.toString()) }
    var reqReels by remember { mutableStateOf(currentSettings.reqTotalReels.toString()) }
    var reqAge by remember { mutableStateOf(currentSettings.reqAccountAgeDays.toString()) }
    
    val scrollState = rememberScrollState()
    
    LaunchedEffect(currentSettings) {
        reelRate = currentSettings.reelRatePer1000.toString()
        imageRate = currentSettings.imageRatePer1000.toString()
        textRate = currentSettings.textRatePer1000.toString()
        minTransfer = currentSettings.minTransferAmount.toString()
        reqViews = currentSettings.reqTotalViews.toString()
        reqFollowers = currentSettings.reqTotalFollowers.toString()
        reqPosts = currentSettings.reqTotalPosts.toString()
        reqReels = currentSettings.reqTotalReels.toString()
        reqAge = currentSettings.reqAccountAgeDays.toString()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monetization Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    adminRepo.setMonetizationSettings(
                        reelRate = reelRate.toDoubleOrNull() ?: 0.5,
                        imageRate = imageRate.toDoubleOrNull() ?: 0.2,
                        textRate = textRate.toDoubleOrNull() ?: 0.1,
                        minTransfer = minTransfer.toDoubleOrNull() ?: 5.0,
                        reqViews = reqViews.toIntOrNull() ?: 500,
                        reqFollowers = reqFollowers.toIntOrNull() ?: 100,
                        reqPosts = reqPosts.toIntOrNull() ?: 10,
                        reqReels = reqReels.toIntOrNull() ?: 5,
                        reqAge = reqAge.toIntOrNull() ?: 7
                    )
                    onBack()
                },
                icon = { Icon(Icons.Default.Save, contentDescription = "Save") },
                text = { Text("Save Settings") },
                containerColor = Color(0xFF1877F2),
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F2F5))
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Revenue Rates (per 1000 views)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1877F2))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = reelRate,
                        onValueChange = { reelRate = it },
                        label = { Text("Reels Video Rate") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = imageRate,
                        onValueChange = { imageRate = it },
                        label = { Text("Image Post Rate") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = textRate,
                        onValueChange = { textRate = it },
                        label = { Text("Text Post Rate") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Creator Fund Eligibility", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1877F2))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = reqViews,
                        onValueChange = { reqViews = it },
                        label = { Text("Total Reach/Views") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = reqFollowers,
                        onValueChange = { reqFollowers = it },
                        label = { Text("Net Followers") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = reqPosts,
                        onValueChange = { reqPosts = it },
                        label = { Text("Total Posts") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = reqReels,
                        onValueChange = { reqReels = it },
                        label = { Text("Total Reels") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = reqAge,
                        onValueChange = { reqAge = it },
                        label = { Text("Account Age (Days)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Wallet Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1877F2))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = minTransfer,
                        onValueChange = { minTransfer = it },
                        label = { Text("Minimum Transfer Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Minimum amount required to transfer from monetization wallet to main wallet.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
