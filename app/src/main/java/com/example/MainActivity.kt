package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.AdminRequestRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.login.LoginScreen
import com.example.ui.main.MainFeedContainer
import com.example.ui.maintenance.MaintenanceScreen
import com.example.ui.menu.admin.AdminDashboardView
import com.example.ui.register.RegisterScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppPermissionHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            )
        )
        setContent {
            MyApplicationTheme {
                FrndomApp()
            }
        }
    }
}

@Composable
fun FrndomApp(
    viewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adminRepo = remember { AdminRequestRepository.getInstance(context) }
    val maintenanceConfig by adminRepo.maintenanceConfigFlow.collectAsStateWithLifecycle()

    var adminBypassedMaintenance by remember { mutableStateOf(false) }
    var showDirectAdminDashboard by remember { mutableStateOf(false) }

    // Native System Permissions Launcher (Direct Android OS Prompts)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        AppPermissionHelper.setInitialPermissionsRequested(context, true)
    }

    // Automatically trigger system permission dialogs directly upon opening app / login
    LaunchedEffect(Unit) {
        val permissions = AppPermissionHelper.getAllEssentialPermissions()
        permissionLauncher.launch(permissions)
    }

    LaunchedEffect(uiState.currentScreen) {
        if (uiState.currentScreen == AuthScreen.WELCOME) {
            val permissions = AppPermissionHelper.getAllEssentialPermissions()
            permissionLauncher.launch(permissions)
        }
    }

    // If maintenance mode is turned off remotely, reset bypass
    LaunchedEffect(maintenanceConfig.isEnabled) {
        if (!maintenanceConfig.isEnabled) {
            adminBypassedMaintenance = false
        }
    }

    // Check if Maintenance Mode is active (blocks the whole app for regular users)
    if (maintenanceConfig.isEnabled && !adminBypassedMaintenance && uiState.currentScreen != AuthScreen.SPLASH) {
        MaintenanceScreen(
            maintenanceConfig = maintenanceConfig,
            onVerifyAdminPin = { pin -> adminRepo.verifyAdminPin(pin) },
            onDisableMaintenance = {
                adminRepo.setMaintenanceMode(false, maintenanceConfig.title, maintenanceConfig.description)
                adminBypassedMaintenance = true
            },
            onAdminDashboardBypass = {
                adminBypassedMaintenance = true
                showDirectAdminDashboard = true
            },
            modifier = modifier
        )
        return
    }

    if (showDirectAdminDashboard) {
        AdminDashboardView(
            onBack = { showDirectAdminDashboard = false },
            onServerSettingsClick = {},
            modifier = modifier
        )
        return
    }

    // Handle Back Press on Register screen to return to Login
    BackHandler(enabled = uiState.currentScreen == AuthScreen.REGISTER) {
        viewModel.navigateTo(AuthScreen.LOGIN)
    }

    AnimatedContent(
        targetState = uiState.currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition",
        modifier = modifier.fillMaxSize()
    ) { screen ->
        when (screen) {
            AuthScreen.SPLASH -> {
                SplashScreen()
            }

            AuthScreen.LOGIN -> {
                LoginScreen(
                    state = uiState,
                    onIdentifierChange = viewModel::onLoginIdentifierChange,
                    onPasswordChange = viewModel::onLoginPasswordChange,
                    onTogglePasswordVisibility = viewModel::toggleLoginPasswordVisibility,
                    onLoginClick = viewModel::login,
                    onCreateAccountClick = { viewModel.navigateTo(AuthScreen.REGISTER) }
                )
            }

            AuthScreen.REGISTER -> {
                RegisterScreen(
                    state = uiState,
                    onFirstNameChange = viewModel::onRegFirstNameChange,
                    onLastNameChange = viewModel::onRegLastNameChange,
                    onIdentifierTypeChange = viewModel::onRegIdentifierTypeChange,
                    onEmailChange = viewModel::onRegEmailChange,
                    onPhoneChange = viewModel::onRegPhoneChange,
                    onGenderChange = viewModel::onRegGenderChange,
                    onBirthDateChange = viewModel::onRegBirthDateChange,
                    onPasswordChange = viewModel::onRegPasswordChange,
                    onConfirmPasswordChange = viewModel::onRegConfirmPasswordChange,
                    onTogglePasswordVisibility = viewModel::toggleRegPasswordVisibility,
                    onToggleConfirmPasswordVisibility = viewModel::toggleRegConfirmPasswordVisibility,
                    onRegisterClick = viewModel::register,
                    onLoginClick = { viewModel.navigateTo(AuthScreen.LOGIN) }
                )
            }

            AuthScreen.WELCOME -> {
                MainFeedContainer(
                    userProfile = uiState.currentUserProfile,
                    onLogoutClick = viewModel::logout
                )
            }
        }
    }
}
