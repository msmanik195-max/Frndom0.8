package com.example.ui.maintenance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MaintenanceConfig
import com.example.ui.menu.admin.AdminPinEntryDialog

/**
 * Full-screen blocking Maintenance overlay when maintenance mode is active.
 */
@Composable
fun MaintenanceScreen(
    maintenanceConfig: MaintenanceConfig,
    onVerifyAdminPin: (String) -> Boolean,
    onDisableMaintenance: () -> Unit,
    onAdminDashboardBypass: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAdminAuthDialog by remember { mutableStateOf(false) }
    var showAdminActionSheet by remember { mutableStateOf(false) }

    if (showAdminAuthDialog) {
        AdminPinEntryDialog(
            onDismiss = { showAdminAuthDialog = false },
            onVerifyPin = onVerifyAdminPin,
            onSuccess = {
                showAdminAuthDialog = false
                showAdminActionSheet = true
            }
        )
    }

    if (showAdminActionSheet) {
        AlertDialog(
            onDismissRequest = { showAdminActionSheet = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color(0xFF0866FF),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Admin Controls",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "You are authenticated as Admin. What would you like to do?",
                        fontSize = 14.sp,
                        color = Color(0xFF65676B)
                    )

                    Button(
                        onClick = {
                            showAdminActionSheet = false
                            onDisableMaintenance()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF008937),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Disable Maintenance Mode", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            showAdminActionSheet = false
                            onAdminDashboardBypass()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Admin Dashboard", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAdminActionSheet = false }) {
                    Text("Close")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1877F2),
                        Color(0xFF0D47A1),
                        Color(0xFF0A192F)
                    )
                )
            )
            .testTag("maintenance_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Main Maintenance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Animated-style Icon Badge
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Construction,
                                contentDescription = "Maintenance",
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Status Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE53935),
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Text(
                                text = "System Maintenance",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE53935)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = maintenanceConfig.title.ifBlank { "Maintenance" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1C1E21),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    Text(
                        text = maintenanceConfig.description.ifBlank {
                            "We are currently performing scheduled maintenance to improve our services. Please check back later."
                        },
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF4B4F56),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = Color(0xFFE4E6EB))
                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF65676B),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "All features are temporarily paused.",
                            fontSize = 13.sp,
                            color = Color(0xFF65676B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Admin Access Button at the bottom
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Admin",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Admin Access",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedButton(
                        onClick = { showAdminAuthDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(text = "Unlock", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Dialog shown to the Admin when toggling Maintenance Mode ON.
 */
@Composable
fun MaintenanceConfigDialog(
    initialConfig: MaintenanceConfig,
    onSave: (title: String, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(if (initialConfig.title.isNotBlank()) initialConfig.title else "Maintenance") }
    var description by remember { mutableStateOf(initialConfig.description) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Construction,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Maintenance Mode",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1E21)
                        )
                        Text(
                            text = "Enable system maintenance for all users",
                            fontSize = 12.sp,
                            color = Color(0xFF65676B)
                        )
                    }
                }

                Divider(color = Color(0xFFE4E6EB))

                // Title Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Title",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1E21)
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorText = null
                        },
                        placeholder = { Text("Maintenance") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0866FF),
                            unfocusedBorderColor = Color(0xFFCED0D4)
                        )
                    )
                }

                // Description Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Maintenance Description (বিস্তারিত ডেসক্রিপশন)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1E21)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            errorText = null
                        },
                        placeholder = {
                            Text("কিসের জন্য মেইনটেনেন্স বিস্তারিত লিখুন... যেমন: সার্ভার আপগ্রেড বা নতুন ফিচার আপডেটের কাজ চলছে।")
                        },
                        minLines = 4,
                        maxLines = 6,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0866FF),
                            unfocusedBorderColor = Color(0xFFCED0D4)
                        )
                    )
                }

                if (errorText != null) {
                    Text(
                        text = errorText ?: "",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Info Note
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFF8E1),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF57C00),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Save এ ক্লিক করার সাথে সাথে পুরো অ্যাপ্লিকেশন সাধারণ ব্যবহারকারীদের জন্য বন্ধ থাকবে এবং এই মেন্টেনেন্স নোটিশ প্রদর্শিত হবে।",
                            fontSize = 12.sp,
                            color = Color(0xFF5D4037),
                            lineHeight = 16.sp
                        )
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF65676B))
                    }

                    Button(
                        onClick = {
                            if (description.isBlank()) {
                                errorText = "Please write a maintenance description."
                            } else {
                                onSave(title.trim().ifBlank { "Maintenance" }, description.trim())
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0866FF)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Save & Enable", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
