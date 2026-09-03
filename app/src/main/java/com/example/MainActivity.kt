package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.components.CloudAttendBottomBar
import com.example.ui.components.CloudAttendTopAppBar
import com.example.ui.components.GlassCanvasBackground
import com.example.ui.components.LiveWebSocketBanner
import com.example.ui.screens.*
import com.example.ui.theme.CloudAttendTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CloudAttendViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CloudAttendViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val currentRole by viewModel.currentRole.collectAsState()
            val currentEmployee by viewModel.currentEmployee.collectAsState()
            val toastMessage by viewModel.toastMessage.collectAsState()
            val latestLiveToast by viewModel.latestLiveToast.collectAsState()
            val latestPushAlert by viewModel.latestPushAlert.collectAsState()
            val pushNotifications by viewModel.pushNotifications.collectAsState()
            val unreadCount by viewModel.unreadNotificationsCount.collectAsState()

            var showNotificationsModal by remember { mutableStateOf(false) }

            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(toastMessage) {
                toastMessage?.let { msg ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = msg,
                            duration = SnackbarDuration.Short
                        )
                        viewModel.clearToast()
                    }
                }
            }

            // Auto dismiss live banner after 6 seconds
            LaunchedEffect(latestLiveToast) {
                if (latestLiveToast != null) {
                    kotlinx.coroutines.delay(6000)
                    viewModel.dismissLiveToast()
                }
            }

            // Auto dismiss push alert toast after 7 seconds
            LaunchedEffect(latestPushAlert) {
                if (latestPushAlert != null) {
                    kotlinx.coroutines.delay(7000)
                    viewModel.dismissPushAlert()
                }
            }

            // Handle back navigation
            BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
                if (currentScreen == AppScreen.PAYSLIP_DETAIL) {
                    viewModel.navigateTo(AppScreen.PAYROLL)
                } else {
                    viewModel.navigateTo(AppScreen.DASHBOARD)
                }
            }

            CloudAttendTheme(darkTheme = isDarkMode) {
                GlassCanvasBackground {
                    Scaffold(
                        containerColor = Color.Transparent,
                        topBar = {
                            CloudAttendTopAppBar(
                                currentScreen = currentScreen,
                                currentRole = currentRole,
                                currentUserName = currentEmployee?.name ?: "User",
                                isDarkMode = isDarkMode,
                                unreadNotificationsCount = unreadCount,
                                onThemeToggleClick = { viewModel.toggleTheme() },
                                onNotificationsClick = { showNotificationsModal = true },
                                onRoleSwitchClick = {
                                    viewModel.switchRole(
                                        if (currentRole == UserRole.ADMIN) UserRole.EMPLOYEE else UserRole.ADMIN
                                    )
                                },
                                onNavigateBack = if (currentScreen == AppScreen.PAYSLIP_DETAIL) {
                                    { viewModel.navigateTo(AppScreen.PAYROLL) }
                                } else null,
                                onSettingsClick = { viewModel.navigateTo(AppScreen.PROFILE) }
                            )
                        },
                        bottomBar = {
                            if (currentScreen != AppScreen.PAYSLIP_DETAIL) {
                                CloudAttendBottomBar(
                                    currentScreen = currentScreen,
                                    currentRole = currentRole,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Crossfade(
                                targetState = currentScreen,
                                label = "screen_transition"
                            ) { screen ->
                                when (screen) {
                                    AppScreen.DASHBOARD -> DashboardScreen(viewModel)
                                    AppScreen.ATTENDANCE -> AttendanceScreen(viewModel)
                                    AppScreen.LEAVES -> LeaveScreen(viewModel)
                                    AppScreen.EMPLOYEES -> EmployeeListScreen(viewModel)
                                    AppScreen.PAYROLL -> PayrollScreen(viewModel)
                                    AppScreen.PAYSLIP_DETAIL -> PayslipDetailScreen(viewModel)
                                    AppScreen.ANALYTICS -> AnalyticsScreen(viewModel)
                                    AppScreen.PROFILE, AppScreen.AUTH -> ProfileScreen(viewModel)
                                }
                            }

                            // Notification Banners Stack (WebSocket Live Event or Push Notification Toast)
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Push Notification Alert Toast (Clock-in, Leave Status, Payroll)
                                AnimatedVisibility(
                                    visible = latestPushAlert != null,
                                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                                ) {
                                    latestPushAlert?.let { alert ->
                                        com.example.ui.components.PushNotificationBanner(
                                            notification = alert,
                                            onDismiss = { viewModel.dismissPushAlert() },
                                            onOpen = {
                                                viewModel.dismissPushAlert()
                                                showNotificationsModal = true
                                            }
                                        )
                                    }
                                }

                                // Real-time floating WebSocket Push Banner
                                AnimatedVisibility(
                                    visible = latestLiveToast != null,
                                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                                ) {
                                    latestLiveToast?.let { event ->
                                        LiveWebSocketBanner(
                                            event = event,
                                            onDismiss = { viewModel.dismissLiveToast() }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Notification History Modal Sheet
                    if (showNotificationsModal) {
                        com.example.ui.components.PushNotificationsModal(
                            notifications = pushNotifications,
                            onDismiss = { showNotificationsModal = false },
                            onMarkAllRead = { viewModel.markAllNotificationsRead() },
                            onClearAll = { viewModel.clearAllNotifications() },
                            onItemClick = { item ->
                                viewModel.markNotificationAsRead(item.id)
                                when (item.type) {
                                    com.example.data.model.NotificationType.CLOCK_IN_SUCCESS,
                                    com.example.data.model.NotificationType.CLOCK_OUT_SUCCESS,
                                    com.example.data.model.NotificationType.STATUS_OVERRIDE -> {
                                        viewModel.navigateTo(AppScreen.ATTENDANCE)
                                    }
                                    com.example.data.model.NotificationType.LEAVE_STATUS_UPDATED -> {
                                        viewModel.navigateTo(AppScreen.LEAVES)
                                    }
                                    com.example.data.model.NotificationType.PAYROLL_ALERT -> {
                                        viewModel.navigateTo(AppScreen.PAYROLL)
                                    }
                                    else -> {}
                                }
                                showNotificationsModal = false
                            }
                        )
                    }
                }
            }
        }
    }
}
