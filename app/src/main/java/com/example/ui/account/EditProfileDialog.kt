package com.example.ui.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserProfile

@Composable
fun EditProfileDialog(
    userProfile: UserProfile?,
    onSave: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    val initial = userProfile ?: UserProfile()

    var fullName by remember { mutableStateOf(initial.fullName) }
    var username by remember { mutableStateOf(initial.username) }
    var bio by remember { mutableStateOf(initial.bio) }
    var contactEmail by remember { mutableStateOf(initial.contactEmail) }
    var contactPhone by remember { mutableStateOf(initial.contactPhone) }
    var website by remember { mutableStateOf(initial.website) }

    var work by remember { mutableStateOf(initial.work) }
    var school by remember { mutableStateOf(initial.school) }
    var college by remember { mutableStateOf(initial.college) }

    var address by remember { mutableStateOf(initial.address) }
    var currentCity by remember { mutableStateOf(initial.currentCity) }
    var hometown by remember { mutableStateOf(initial.hometown) }

    var idCardNumber by remember { mutableStateOf(initial.idCardNumber) }
    var relationshipStatus by remember { mutableStateOf(initial.relationshipStatus) }

    // Privacy toggles
    var showContactPhone by remember { mutableStateOf(initial.showContactPhone) }
    var showContactEmail by remember { mutableStateOf(initial.showContactEmail) }
    var showUsername by remember { mutableStateOf(initial.showUsername) }
    var showIdCard by remember { mutableStateOf(initial.showIdCard) }
    var showAddress by remember { mutableStateOf(initial.showAddress) }
    var showEducation by remember { mutableStateOf(initial.showEducation) }
    var showWork by remember { mutableStateOf(initial.showWork) }
    var showWebsite by remember { mutableStateOf(initial.showWebsite) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF050505)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF050505))
                    }
                }

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Bio
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )

                // Contact Phone
                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Contact Phone (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Contact Email
                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = { contactEmail = it },
                    label = { Text("Contact Email (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Website
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Website (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Workplace
                OutlinedTextField(
                    value = work,
                    onValueChange = { work = it },
                    label = { Text("Workplace (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // School
                OutlinedTextField(
                    value = school,
                    onValueChange = { school = it },
                    label = { Text("School (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // College
                OutlinedTextField(
                    value = college,
                    onValueChange = { college = it },
                    label = { Text("College (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // City
                OutlinedTextField(
                    value = currentCity,
                    onValueChange = { currentCity = it },
                    label = { Text("City (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Hometown
                OutlinedTextField(
                    value = hometown,
                    onValueChange = { hometown = it },
                    label = { Text("Hometown (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Relationship Status
                OutlinedTextField(
                    value = relationshipStatus,
                    onValueChange = { relationshipStatus = it },
                    label = { Text("Relationship (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // ID Card Number
                OutlinedTextField(
                    value = idCardNumber,
                    onValueChange = { idCardNumber = it },
                    label = { Text("ID Card Number (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Divider(color = Color(0xFFE4E6EB))

                // Privacy Section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF0866FF), modifier = Modifier.size(18.dp))
                    Text(
                        text = "Privacy Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF050505),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                DialogSwitchRow("Contact Phone", showContactPhone) { showContactPhone = it }
                DialogSwitchRow("Contact Email", showContactEmail) { showContactEmail = it }
                DialogSwitchRow("Username", showUsername) { showUsername = it }
                DialogSwitchRow("Website", showWebsite) { showWebsite = it }
                DialogSwitchRow("Address & City", showAddress) { showAddress = it }
                DialogSwitchRow("Work & Education", showEducation) {
                    showEducation = it
                    showWork = it
                }
                DialogSwitchRow("ID Card Number", showIdCard) { showIdCard = it }

                Spacer(modifier = Modifier.height(4.dp))

                // Save Button
                Button(
                    onClick = {
                        val updated = initial.copy(
                            fullName = fullName.trim(),
                            username = username.trim().removePrefix("@"),
                            bio = bio.trim(),
                            contactEmail = contactEmail.trim(),
                            contactPhone = contactPhone.trim(),
                            website = website.trim(),
                            work = work.trim(),
                            school = school.trim(),
                            college = college.trim(),
                            education = if (college.isNotBlank()) college.trim() else initial.education,
                            address = address.trim(),
                            currentCity = currentCity.trim(),
                            hometown = hometown.trim(),
                            idCardNumber = idCardNumber.trim(),
                            relationshipStatus = relationshipStatus.trim(),
                            showContactPhone = showContactPhone,
                            showContactEmail = showContactEmail,
                            showUsername = showUsername,
                            showIdCard = showIdCard,
                            showAddress = showAddress,
                            showEducation = showEducation,
                            showWork = showWork,
                            showWebsite = showWebsite
                        )
                        onSave(updated)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0866FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(text = "Save Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun DialogSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF050505))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0866FF),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFBCC0C4)
            )
        )
    }
}
