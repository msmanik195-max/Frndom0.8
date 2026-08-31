package com.example.ui.menu.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Dialog to verify Admin Security PIN before allowing access into the Admin Dashboard.
 * Default PIN is "1234".
 */
@Composable
fun AdminPinEntryDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onVerifyPin: (String) -> Boolean
) {
    var pinText by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("admin_pin_entry_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1877F2).copy(alpha = 0.12f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Security Lock",
                            tint = Color(0xFF1877F2),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Admin Security PIN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF050505),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Enter your security PIN to access the Admin Panel",
                    fontSize = 13.sp,
                    color = Color(0xFF65676B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Hint badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Default PIN: 1234",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                // PIN Input Field
                OutlinedTextField(
                    value = pinText,
                    onValueChange = { input ->
                        if (input.length <= 8) {
                            pinText = input
                            isError = false
                        }
                    },
                    label = { Text("Security PIN") },
                    placeholder = { Text("1234") },
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (onVerifyPin(pinText)) {
                                onSuccess()
                            } else {
                                isError = true
                                errorMessage = "Incorrect PIN! Please try again."
                            }
                        }
                    ),
                    visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPinVisible = !isPinVisible }) {
                            Icon(
                                imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPinVisible) "Hide PIN" else "Show PIN",
                                tint = Color(0xFF65676B)
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1877F2),
                        unfocusedBorderColor = Color(0xFFCED0D4),
                        errorBorderColor = Color(0xFFD32F2F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input")
                )

                AnimatedVisibility(visible = isError) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

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
                            .height(46.dp)
                            .testTag("admin_pin_cancel_btn")
                    ) {
                        Text("Cancel", color = Color(0xFF65676B), fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (onVerifyPin(pinText)) {
                                onSuccess()
                            } else {
                                isError = true
                                errorMessage = "Incorrect PIN! Please try again."
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("admin_pin_submit_btn")
                    ) {
                        Text("Enter", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Dialog to change the Admin Security PIN.
 * Syncs the updated PIN to Firebase Realtime Database at admin_pin and admin_pen.
 */
@Composable
fun AdminChangePinDialog(
    onDismiss: () -> Unit,
    onVerifyCurrentPin: (String) -> Boolean,
    onSaveNewPin: (String) -> Unit
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    var isCurrentVisible by remember { mutableStateOf(false) }
    var isNewVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("admin_change_pin_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF4CAF50).copy(alpha = 0.12f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "Change PIN",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Change Admin Security PIN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF050505),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "New PIN will sync with Firebase Realtime Database (admin_pin)",
                    fontSize = 12.sp,
                    color = Color(0xFF65676B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                // Current PIN
                OutlinedTextField(
                    value = currentPin,
                    onValueChange = {
                        currentPin = it
                        errorMessage = ""
                    },
                    label = { Text("Current PIN") },
                    placeholder = { Text("1234") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = if (isCurrentVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isCurrentVisible = !isCurrentVisible }) {
                            Icon(
                                imageVector = if (isCurrentVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color(0xFF65676B)
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_change_current_pin_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // New PIN
                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        newPin = it
                        errorMessage = ""
                    },
                    label = { Text("New PIN") },
                    placeholder = { Text("At least 4 digits") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = if (isNewVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isNewVisible = !isNewVisible }) {
                            Icon(
                                imageVector = if (isNewVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color(0xFF65676B)
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_change_new_pin_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Confirm PIN
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        confirmPin = it
                        errorMessage = ""
                    },
                    label = { Text("Confirm New PIN") },
                    placeholder = { Text("Re-enter new PIN") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_change_confirm_pin_input")
                )

                AnimatedVisibility(visible = errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }

                AnimatedVisibility(visible = isSuccess) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "✓ PIN successfully updated and synced to Firebase!",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF65676B), fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (!onVerifyCurrentPin(currentPin)) {
                                errorMessage = "Current PIN is incorrect!"
                                return@Button
                            }
                            if (newPin.trim().length < 4) {
                                errorMessage = "New PIN must be at least 4 digits."
                                return@Button
                            }
                            if (newPin.trim() != confirmPin.trim()) {
                                errorMessage = "New PINs do not match!"
                                return@Button
                            }

                            onSaveNewPin(newPin.trim())
                            isSuccess = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("admin_change_pin_save_btn")
                    ) {
                        Text("Save PIN", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
