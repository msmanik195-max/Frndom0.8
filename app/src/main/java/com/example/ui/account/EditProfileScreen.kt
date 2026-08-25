package com.example.ui.account

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.data.repository.UserRepository
import com.example.data.service.MediaUploadService
import com.example.ui.components.CropShape
import com.example.ui.components.ImageCropDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userProfile: UserProfile,
    userRepository: UserRepository,
    mediaUploadService: MediaUploadService? = null,
    onBack: () -> Unit,
    onSaved: (UserProfile) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Photos State
    var profilePictureUrl by remember { mutableStateOf(userProfile.profilePictureUrl) }
    var coverPictureUrl by remember { mutableStateOf(userProfile.coverPictureUrl) }
    var isUploadingProfile by remember { mutableStateOf(false) }
    var isUploadingCover by remember { mutableStateOf(false) }
    var showProfileOptionsSheet by remember { mutableStateOf(false) }
    var showCoverOptionsSheet by remember { mutableStateOf(false) }
    var showAvatarUrlDialog by remember { mutableStateOf(false) }
    var showCoverUrlDialog by remember { mutableStateOf(false) }
    var tempAvatarUrl by remember { mutableStateOf(userProfile.profilePictureUrl) }
    var tempCoverUrl by remember { mutableStateOf(userProfile.coverPictureUrl) }

    var imageUriToCrop by remember { mutableStateOf<Uri?>(null) }
    var coverUriToCrop by remember { mutableStateOf<Uri?>(null) }

    // Pickers
    val profilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUriToCrop = uri
        }
    }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coverUriToCrop = uri
        }
    }

    // Editable Profile Fields (Primary account email & phone are excluded to prevent editing)
    var fullName by remember { mutableStateOf(userProfile.fullName) }
    var username by remember { mutableStateOf(userProfile.username) }
    var bio by remember { mutableStateOf(userProfile.bio) }

    // Contact Details
    var contactPhone by remember { mutableStateOf(userProfile.contactPhone) }
    var contactEmail by remember { mutableStateOf(userProfile.contactEmail) }
    var website by remember { mutableStateOf(userProfile.website) }

    // Work & Education
    var work by remember { mutableStateOf(userProfile.work) }
    var school by remember { mutableStateOf(userProfile.school) }
    var college by remember { mutableStateOf(userProfile.college) }

    // Places Lived
    var address by remember { mutableStateOf(userProfile.address) }
    var currentCity by remember { mutableStateOf(userProfile.currentCity) }
    var hometown by remember { mutableStateOf(userProfile.hometown) }

    // Identity & Details
    var relationshipStatus by remember { mutableStateOf(userProfile.relationshipStatus) }
    var idCardNumber by remember { mutableStateOf(userProfile.idCardNumber) }

    // Privacy Visibility Switches
    var showContactPhone by remember { mutableStateOf(userProfile.showContactPhone) }
    var showContactEmail by remember { mutableStateOf(userProfile.showContactEmail) }
    var showUsername by remember { mutableStateOf(userProfile.showUsername) }
    var showIdCard by remember { mutableStateOf(userProfile.showIdCard) }
    var showAddress by remember { mutableStateOf(userProfile.showAddress) }
    var showEducation by remember { mutableStateOf(userProfile.showEducation) }
    var showWork by remember { mutableStateOf(userProfile.showWork) }
    var showWebsite by remember { mutableStateOf(userProfile.showWebsite) }

    var isSaving by remember { mutableStateOf(false) }

    val handleSave = {
        if (!isSaving) {
            isSaving = true
            val updated = userProfile.copy(
                fullName = fullName.trim(),
                username = username.trim().removePrefix("@"),
                bio = bio.trim(),
                profilePictureUrl = profilePictureUrl.trim(),
                coverPictureUrl = coverPictureUrl.trim(),
                // Keep account primary email and phone unchanged from userProfile
                email = userProfile.email,
                phoneNumber = userProfile.phoneNumber,
                contactEmail = contactEmail.trim(),
                contactPhone = contactPhone.trim(),
                website = website.trim(),
                work = work.trim(),
                school = school.trim(),
                college = college.trim(),
                education = if (college.isNotBlank()) college.trim() else userProfile.education,
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
            userRepository.updateUserProfile(updated) { success ->
                isSaving = false
                if (success) {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    onSaved(updated)
                    onBack()
                } else {
                    Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5)),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF050505)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF050505)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { handleSave() },
                        enabled = !isSaving
                    ) {
                        Text(
                            text = if (isSaving) "Saving..." else "Save",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0866FF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF0F2F5))
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Direct Visual Header: Cover & Profile Picture
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            // Cover Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                    .background(Color(0xFFE4E6EB))
                                    .clickable { showCoverOptionsSheet = true }
                            ) {
                                if (coverPictureUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = coverPictureUrl,
                                        contentDescription = "Cover Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                                                    startY = 180f
                                                )
                                            )
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                contentDescription = null,
                                                tint = Color(0xFF65676B),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "Add cover photo",
                                                fontSize = 13.sp,
                                                color = Color(0xFF65676B),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // Cover Camera Button
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 12.dp, bottom = 10.dp)
                                        .size(36.dp)
                                        .shadow(3.dp, CircleShape)
                                ) {
                                    IconButton(onClick = { showCoverOptionsSheet = true }) {
                                        if (isUploadingCover) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xFF0866FF)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Change Cover",
                                                tint = Color(0xFF1C1E21),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Profile Avatar
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(112.dp)
                                        .border(4.dp, Color.White, CircleShape)
                                        .shadow(5.dp, CircleShape)
                                        .clickable { showProfileOptionsSheet = true },
                                    shape = CircleShape,
                                    color = Color(0xFFD8DADF)
                                ) {
                                    if (profilePictureUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = profilePictureUrl,
                                            contentDescription = fullName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = fullName.firstOrNull()?.uppercase() ?: "U",
                                                fontSize = 40.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0866FF)
                                            )
                                        }
                                    }
                                }

                                // Avatar Camera Badge
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF0866FF),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(32.dp)
                                        .border(2.dp, Color.White, CircleShape)
                                        .shadow(2.dp, CircleShape)
                                ) {
                                    IconButton(onClick = { showProfileOptionsSheet = true }) {
                                        if (isUploadingProfile) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.White
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Change Avatar",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Buttons for quick photo changes
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showProfileOptionsSheet = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEBF5FF),
                                    contentColor = Color(0xFF0866FF)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Avatar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { showCoverOptionsSheet = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF0F2F5),
                                    contentColor = Color(0xFF1C1E21)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Cover", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // 2. Basic Info
            item {
                EditProfileSectionCard(title = "Basic Info") {
                    ProfileEditTextField(
                        label = "Full Name",
                        value = fullName,
                        onValueChange = { fullName = it },
                        icon = Icons.Default.Person,
                        placeholder = "Your name"
                    )

                    ProfileEditTextField(
                        label = "Username (Optional)",
                        value = username,
                        onValueChange = { username = it },
                        icon = Icons.Default.Person,
                        placeholder = "username"
                    )

                    ProfileEditTextField(
                        label = "Bio (Optional)",
                        value = bio,
                        onValueChange = { bio = it },
                        icon = Icons.Default.Favorite,
                        placeholder = "Describe yourself...",
                        maxLines = 3,
                        singleLine = false
                    )
                }
            }

            // 3. Contact Info (Only Contact Phone & Contact Email)
            item {
                EditProfileSectionCard(title = "Contact Info") {
                    ProfileEditTextField(
                        label = "Contact Phone (Optional)",
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        icon = Icons.Default.Phone,
                        placeholder = "+1 234 567 8900"
                    )

                    ProfileEditTextField(
                        label = "Contact Email (Optional)",
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        icon = Icons.Default.Email,
                        placeholder = "contact@example.com"
                    )

                    ProfileEditTextField(
                        label = "Website (Optional)",
                        value = website,
                        onValueChange = { website = it },
                        icon = Icons.Default.Language,
                        placeholder = "https://example.com"
                    )
                }
            }

            // 4. Work & Education
            item {
                EditProfileSectionCard(title = "Work & Education") {
                    ProfileEditTextField(
                        label = "Workplace (Optional)",
                        value = work,
                        onValueChange = { work = it },
                        icon = Icons.Default.Work,
                        placeholder = "Company or job title"
                    )

                    ProfileEditTextField(
                        label = "School (Optional)",
                        value = school,
                        onValueChange = { school = it },
                        icon = Icons.Default.School,
                        placeholder = "School name"
                    )

                    ProfileEditTextField(
                        label = "College (Optional)",
                        value = college,
                        onValueChange = { college = it },
                        icon = Icons.Default.School,
                        placeholder = "College or university"
                    )
                }
            }

            // 5. Places Lived
            item {
                EditProfileSectionCard(title = "Places Lived") {
                    ProfileEditTextField(
                        label = "Address (Optional)",
                        value = address,
                        onValueChange = { address = it },
                        icon = Icons.Default.LocationOn,
                        placeholder = "Street address"
                    )

                    ProfileEditTextField(
                        label = "City (Optional)",
                        value = currentCity,
                        onValueChange = { currentCity = it },
                        icon = Icons.Default.LocationOn,
                        placeholder = "Current city"
                    )

                    ProfileEditTextField(
                        label = "Hometown (Optional)",
                        value = hometown,
                        onValueChange = { hometown = it },
                        icon = Icons.Default.Home,
                        placeholder = "Hometown"
                    )
                }
            }

            // 6. Identity & Status
            item {
                EditProfileSectionCard(title = "Identity") {
                    ProfileEditTextField(
                        label = "Relationship (Optional)",
                        value = relationshipStatus,
                        onValueChange = { relationshipStatus = it },
                        icon = Icons.Default.Favorite,
                        placeholder = "Single, In a relationship, Married..."
                    )

                    ProfileEditTextField(
                        label = "ID Card Number (Optional)",
                        value = idCardNumber,
                        onValueChange = { idCardNumber = it },
                        icon = Icons.Default.Badge,
                        placeholder = "ID Card Number"
                    )
                }
            }

            // 7. Privacy Settings (Short, simple switch labels)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color(0xFF0866FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Privacy Settings",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF050505)
                            )
                        }

                        Divider(color = Color(0xFFE4E6EB))

                        VisibilitySwitchRow(
                            label = "Contact Phone",
                            checked = showContactPhone,
                            onCheckedChange = { showContactPhone = it }
                        )

                        VisibilitySwitchRow(
                            label = "Contact Email",
                            checked = showContactEmail,
                            onCheckedChange = { showContactEmail = it }
                        )

                        VisibilitySwitchRow(
                            label = "Username",
                            checked = showUsername,
                            onCheckedChange = { showUsername = it }
                        )

                        VisibilitySwitchRow(
                            label = "Website",
                            checked = showWebsite,
                            onCheckedChange = { showWebsite = it }
                        )

                        VisibilitySwitchRow(
                            label = "Address & City",
                            checked = showAddress,
                            onCheckedChange = { showAddress = it }
                        )

                        VisibilitySwitchRow(
                            label = "Work & Education",
                            checked = showEducation,
                            onCheckedChange = {
                                showEducation = it
                                showWork = it
                            }
                        )

                        VisibilitySwitchRow(
                            label = "ID Card Number",
                            checked = showIdCard,
                            onCheckedChange = { showIdCard = it }
                        )
                    }
                }
            }

            // Save Button
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { handleSave() },
                    enabled = !isSaving,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0866FF),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSaving) "Saving..." else "Save Changes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Profile Picture Crop Dialog
    imageUriToCrop?.let { uriToCrop ->
        ImageCropDialog(
            imageUri = uriToCrop,
            cropShape = CropShape.CIRCLE,
            title = "Crop Avatar",
            onCropSuccess = { croppedUri ->
                imageUriToCrop = null
                isUploadingProfile = true
                scope.launch {
                    val remoteUrl = if (mediaUploadService != null) {
                        val res = mediaUploadService.uploadImageUri(croppedUri, folder = "profiles")
                        res.getOrDefault(croppedUri.toString())
                    } else {
                        croppedUri.toString()
                    }
                    isUploadingProfile = false
                    profilePictureUrl = remoteUrl
                    Toast.makeText(context, "Avatar updated", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { imageUriToCrop = null }
        )
    }

    // Cover Photo Crop Dialog
    coverUriToCrop?.let { uriToCrop ->
        ImageCropDialog(
            imageUri = uriToCrop,
            cropShape = CropShape.COVER,
            title = "Crop Cover",
            onCropSuccess = { croppedUri ->
                coverUriToCrop = null
                isUploadingCover = true
                scope.launch {
                    val remoteUrl = if (mediaUploadService != null) {
                        val res = mediaUploadService.uploadImageUri(croppedUri, folder = "covers")
                        res.getOrDefault(croppedUri.toString())
                    } else {
                        croppedUri.toString()
                    }
                    isUploadingCover = false
                    coverPictureUrl = remoteUrl
                    Toast.makeText(context, "Cover updated", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { coverUriToCrop = null }
        )
    }

    // Profile Picture Options Bottom Sheet
    if (showProfileOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProfileOptionsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Profile Picture",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF050505),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Pick from Gallery
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            showProfileOptionsSheet = false
                            profilePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Choose from Gallery",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF050505)
                    )
                }

                // Enter URL
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            showProfileOptionsSheet = false
                            tempAvatarUrl = profilePictureUrl
                            showAvatarUrlDialog = true
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = Color(0xFF0866FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Enter Image URL",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF050505)
                    )
                }

                // Remove avatar
                if (profilePictureUrl.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showProfileOptionsSheet = false
                                profilePictureUrl = ""
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Remove Picture",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Cover Photo Options Bottom Sheet
    if (showCoverOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCoverOptionsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Cover Photo",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF050505),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Pick from Gallery
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            showCoverOptionsSheet = false
                            coverPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Choose from Gallery",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF050505)
                    )
                }

                // Enter URL
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            showCoverOptionsSheet = false
                            tempCoverUrl = coverPictureUrl
                            showCoverUrlDialog = true
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = Color(0xFF0866FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Enter Image URL",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF050505)
                    )
                }

                // Remove cover
                if (coverPictureUrl.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showCoverOptionsSheet = false
                                coverPictureUrl = ""
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Remove Cover",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Avatar URL Dialog
    if (showAvatarUrlDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarUrlDialog = false },
            title = { Text("Profile Picture URL", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempAvatarUrl,
                    onValueChange = { tempAvatarUrl = it },
                    label = { Text("URL") },
                    placeholder = { Text("https://example.com/photo.jpg") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        profilePictureUrl = tempAvatarUrl.trim()
                        showAvatarUrlDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0866FF))
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAvatarUrlDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Cover URL Dialog
    if (showCoverUrlDialog) {
        AlertDialog(
            onDismissRequest = { showCoverUrlDialog = false },
            title = { Text("Cover Photo URL", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempCoverUrl,
                    onValueChange = { tempCoverUrl = it },
                    label = { Text("URL") },
                    placeholder = { Text("https://example.com/banner.jpg") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coverPictureUrl = tempCoverUrl.trim()
                        showCoverUrlDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0866FF))
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCoverUrlDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EditProfileSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF050505)
            )
            content()
        }
    }
}

@Composable
private fun ProfileEditTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color(0xFF8A8D91)) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF65676B),
                modifier = Modifier.size(20.dp)
            )
        },
        singleLine = singleLine,
        maxLines = maxLines,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF0866FF),
            unfocusedBorderColor = Color(0xFFCED0D4),
            focusedLabelColor = Color(0xFF0866FF),
            unfocusedLabelColor = Color(0xFF65676B)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun VisibilitySwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF050505),
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
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
