package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.LeaveStatus
import com.example.data.model.LiveEventType
import com.example.data.model.UserRole
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CloudAttendViewModel

@Composable
fun DashboardScreen(
    viewModel: CloudAttendViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentEmployee by viewModel.currentEmployee.collectAsState()
    val uiStats by viewModel.uiStats.collectAsState()
    val attendanceRecords by viewModel.attendanceRecords.collectAsState()
    val leaveRequests by viewModel.leaveRequests.collectAsState()
    val payrollRecords by viewModel.payrollRecords.collectAsState()
    val liveEvents by viewModel.liveEventsFeed.collectAsState()
    val webSocketLatency by viewModel.webSocketLatency.collectAsState()
    val isSimActive by viewModel.isRealTimeSimActive.collectAsState()
    val filteredHREmployees by viewModel.filteredHREmployees.collectAsState()
    val hrSearchQuery by viewModel.hrSearchQuery.collectAsState()
    val hrDepartmentFilter by viewModel.hrDepartmentFilter.collectAsState()

    var statusOverrideRecord by remember { mutableStateOf<AttendanceRecord?>(null) }
    var employeeToDelete by remember { mutableStateOf<com.example.data.model.Employee?>(null) }
    var employeeToEdit by remember { mutableStateOf<com.example.data.model.Employee?>(null) }

    val todayRecords = remember(attendanceRecords, viewModel.todayDateString) {
        attendanceRecords.filter { it.date == viewModel.todayDateString }
    }

    val pendingLeaves = remember(leaveRequests) {
        leaveRequests.filter { it.status == LeaveStatus.PENDING }
    }

    // Check if current employee is checked in today
    val myTodayRecord = remember(attendanceRecords, currentEmployee, viewModel.todayDateString) {
        currentEmployee?.let { emp ->
            attendanceRecords.firstOrNull { it.employeeId == emp.id && it.date == viewModel.todayDateString }
        }
    }

    val isCheckedIn = myTodayRecord?.checkInTime != null
    val isCheckedOut = myTodayRecord?.checkOutTime != null

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Real-Time Live WebSocket Gateway Header Bar
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0x2E1E1B4B),
                borderColor = Color(0x66818CF8)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x3334D399))
                                .border(1.dp, Color(0x6634D399), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WifiTethering,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Real-Time WebSocket Sync",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color(0x3310B981)
                                ) {
                                    Text(
                                        text = "${webSocketLatency}ms",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6EE7B7),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isSimActive) "Live peer events streaming automatically" else "Stream paused",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Quick push test trigger button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x336366F1),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x80818CF8)),
                        modifier = Modifier
                            .clickable { viewModel.triggerSimulatedPeerEvent() }
                            .testTag("simulate_peer_event_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFC084FC),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Test Push",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        if (currentRole == UserRole.EMPLOYEE) {
            // ==========================================
            // EMPLOYEE DASHBOARD (GLASSMORPHIC SAAS)
            // ==========================================
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0x28FFFFFF),
                    borderColor = Color(0x40FFFFFF)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Greeting and Live Attendance Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "Welcome back,",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentEmployee?.name ?: "Alex Rivera",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = viewModel.todayDisplayDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFCBD5E1)
                                )
                            }

                            if (isCheckedIn) {
                                AttendanceStatusBadge(status = myTodayRecord?.status ?: AttendanceStatus.PRESENT)
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color(0x26FFFFFF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF94A3B8))
                                        )
                                        Text(
                                            text = "NOT PUNCHED",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFFE2E8F0)
                                        )
                                    }
                                }
                            }
                        }

                        // Glass Times Boxes (Check In & Check Out)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Check In Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0x1FFFFFFF))
                                    .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(18.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "CHECK IN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.8.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = myTodayRecord?.checkInTime ?: "--:--:--",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 17.sp,
                                        color = if (isCheckedIn) Color(0xFF34D399) else Color(0xFF64748B)
                                    )
                                }
                            }

                            // Check Out Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0x1FFFFFFF))
                                    .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(18.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "CHECK OUT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.8.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = myTodayRecord?.checkOutTime ?: "--:--:--",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 17.sp,
                                        color = if (isCheckedOut) Color(0xFF38BDF8) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        // Punch Action Button with Glossy Indigo-Purple Gradient
                        if (!isCheckedIn) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                        )
                                    )
                                    .border(1.dp, Color(0x80FFFFFF), RoundedCornerShape(18.dp))
                                    .clickable { viewModel.checkIn("HQ Tech Park, Floor 3", "Mobile biometric attendance") }
                                    .testTag("employee_checkin_btn")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Fingerprint,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CHECK IN NOW (LIVE SYNC)",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        } else if (!isCheckedOut) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF0284C7), Color(0xFF4F46E5))
                                        )
                                    )
                                    .border(1.dp, Color(0x80FFFFFF), RoundedCornerShape(18.dp))
                                    .clickable { viewModel.checkOut() }
                                    .testTag("employee_checkout_btn")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Logout,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CHECK OUT NOW",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0x2E10B981),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6634D399)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "TODAY'S SHIFT COMPLETED",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6EE7B7)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2 Glass Metric Cards for Employee
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SaaSStatCard(
                        title = "Days Attended",
                        value = "18/22",
                        subtitle = "This billing cycle",
                        icon = Icons.Filled.EventAvailable,
                        iconColor = NeonAmber,
                        iconBgColor = Color(0x33F59E0B),
                        modifier = Modifier.weight(1f)
                    )

                    SaaSStatCard(
                        title = "Est. Net Pay",
                        value = formatCurrency(currentEmployee?.grossSalaryMonthly ?: 4250.0),
                        subtitle = "August 2026",
                        icon = Icons.Filled.Payments,
                        iconColor = NeonIndigo,
                        iconBgColor = Color(0x334F46E5),
                        badgeText = "On Track",
                        badgeColor = NeonEmerald,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Interactive Attendance & Leave Calendar View
            item {
                EmployeeAttendanceCalendarCard(
                    employee = currentEmployee,
                    attendanceRecords = attendanceRecords,
                    leaveRequests = leaveRequests,
                    todayDateString = viewModel.todayDateString
                )
            }

            // Quick Actions Glass Card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "QUICK ACTIONS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Action 1: Request Leave
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x1FFFFFFF))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                                    .clickable { viewModel.navigateTo(AppScreen.LEAVES) }
                                    .padding(vertical = 14.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DirectionsCar,
                                        contentDescription = null,
                                        tint = Color(0xFF818CF8),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "REQUEST LEAVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            // Action 2: Get Payslip
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x1FFFFFFF))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                                    .clickable { viewModel.navigateTo(AppScreen.PAYROLL) }
                                    .padding(vertical = 14.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Description,
                                        contentDescription = null,
                                        tint = Color(0xFF818CF8),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "GET PAYSLIP",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            // Action 3: My Log
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x1FFFFFFF))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                                    .clickable { viewModel.navigateTo(AppScreen.ATTENDANCE) }
                                    .padding(vertical = 14.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.History,
                                        contentDescription = null,
                                        tint = Color(0xFF818CF8),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "MY LOG",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Attendance Overview Chart
            item {
                WeeklyAttendanceChart()
            }

        } else {
            // ==========================================
            // ADMIN DASHBOARD (GLASSMORPHIC SAAS)
            // ==========================================
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0x2E1E1B4B),
                    borderColor = Color(0x55818CF8)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Executive Operations Hub",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = viewModel.todayDisplayDate,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color(0x2E10B981),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6634D399))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF34D399))
                                    )
                                    Text(
                                        text = "LIVE DISPATCH",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF6EE7B7)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Real-time attendance stream, instant status changes, automated leave approvals & payroll.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            // Admin Metric Grid (4 Glass Cards)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SaaSStatCard(
                        title = "Total Headcount",
                        value = "${uiStats.totalEmployees}",
                        subtitle = "Active employees",
                        icon = Icons.Filled.People,
                        iconColor = NeonIndigo,
                        iconBgColor = Color(0x334F46E5),
                        modifier = Modifier.weight(1f)
                    )
                    SaaSStatCard(
                        title = "Present Today",
                        value = "${uiStats.presentToday + uiStats.lateToday}",
                        subtitle = "${uiStats.todayAttendanceRate}% compliance",
                        icon = Icons.Filled.CheckCircle,
                        iconColor = NeonEmerald,
                        iconBgColor = Color(0x3310B981),
                        badgeText = "${uiStats.todayAttendanceRate}%",
                        badgeColor = NeonEmerald,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SaaSStatCard(
                        title = "Pending Leaves",
                        value = "${uiStats.pendingLeavesCount}",
                        subtitle = "Awaiting decision",
                        icon = Icons.Filled.EventBusy,
                        iconColor = NeonAmber,
                        iconBgColor = Color(0x33F59E0B),
                        badgeText = if (uiStats.pendingLeavesCount > 0) "Needs Action" else "Clear",
                        badgeColor = if (uiStats.pendingLeavesCount > 0) NeonAmber else NeonEmerald,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo(AppScreen.LEAVES) }
                    )
                    SaaSStatCard(
                        title = "Monthly Payroll",
                        value = formatCurrency(uiStats.totalMonthlyPayroll),
                        subtitle = "August 2026",
                        icon = Icons.Filled.Payments,
                        iconColor = NeonCyan,
                        iconBgColor = Color(0x3306B6D4),
                        badgeText = "Disbursed",
                        badgeColor = NeonEmerald,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo(AppScreen.PAYROLL) }
                    )
                }
            }

            // Real-Time Live Activity Feed (WebSocket Stream)
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0x221E1B4B),
                    borderColor = Color(0x4D818CF8)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ElectricBolt,
                                    contentDescription = null,
                                    tint = Color(0xFF818CF8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Live Stream Activity Feed",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Text(
                                text = "Auto-Updates Instantly",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }

                        if (liveEvents.isEmpty()) {
                            Text(
                                text = "Waiting for live WebSocket events...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                liveEvents.take(4).forEach { event ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0x1FFFFFFF),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x26FFFFFF)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when (event.type) {
                                                            LiveEventType.CHECK_IN -> Color(0xFF34D399)
                                                            LiveEventType.CHECK_OUT -> Color(0xFF38BDF8)
                                                            LiveEventType.STATUS_CHANGED -> Color(0xFFFBBF24)
                                                            LiveEventType.LEAVE_APPLIED -> Color(0xFFC084FC)
                                                            LiveEventType.LEAVE_APPROVED -> Color(0xFF34D399)
                                                            LiveEventType.LEAVE_REJECTED -> Color(0xFFFB7185)
                                                            LiveEventType.PAYROLL_PROCESSED -> Color(0xFF818CF8)
                                                        }
                                                    )
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = event.employeeName,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = event.timestamp,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFF94A3B8),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                Text(
                                                    text = event.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFFCBD5E1),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Admin Action Buttons
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "ADMIN ACTIONS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                        )
                                    )
                                    .border(1.dp, Color(0x80FFFFFF), RoundedCornerShape(16.dp))
                                    .clickable { viewModel.navigateTo(AppScreen.PAYROLL) }
                                    .testTag("quick_run_payroll_btn")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Run Payroll", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0x26FFFFFF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x38FFFFFF)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable { viewModel.navigateTo(AppScreen.EMPLOYEES) }
                                    .testTag("quick_add_employee_btn")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Staff", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Weekly Attendance Chart
            item {
                WeeklyAttendanceChart()
            }

            // Monthly Salary Trend
            item {
                MonthlySalaryTrendChart()
            }

            // =========================================================================
            // HR EMPLOYEE SEARCH BAR & DIRECT DIRECTORY MANAGEMENT
            // =========================================================================
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0x2E1E1B4B),
                    borderColor = Color(0x66818CF8)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title & Add Staff Shortcut
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x336366F1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PeopleAlt,
                                        contentDescription = null,
                                        tint = Color(0xFF818CF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "HR Staff Directory & Search",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${filteredHREmployees.size} matching team member(s)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            TextButton(
                                onClick = { viewModel.navigateTo(AppScreen.EMPLOYEES) },
                                modifier = Modifier.testTag("hr_view_all_staff_btn")
                            ) {
                                Text("Manage All", color = Color(0xFF818CF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // Search Bar Input Field
                        OutlinedTextField(
                            value = hrSearchQuery,
                            onValueChange = { viewModel.setHrSearchQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("hr_search_bar_input"),
                            placeholder = {
                                Text(
                                    "Search employee by name, code, or role...",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF818CF8),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (hrSearchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.setHrSearchQuery("") },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Clear Search",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = Color(0x40FFFFFF),
                                focusedContainerColor = Color(0x26FFFFFF),
                                unfocusedContainerColor = Color(0x14FFFFFF),
                                cursorColor = Color(0xFF818CF8)
                            ),
                            singleLine = true
                        )

                        // Department Filter Chips Row
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val departments = listOf("All", "Engineering", "Design", "Product", "HR & Admin", "Sales", "Operations")
                            items(departments.size) { index ->
                                val dept = departments[index]
                                val isSelected = hrDepartmentFilter == dept
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) Color(0xFF6366F1) else Color(0x1FFFFFFF),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFFA5B4FC) else Color(0x2EFFFFFF)
                                    ),
                                    modifier = Modifier
                                        .clickable { viewModel.setHrDepartmentFilter(dept) }
                                        .testTag("hr_dept_filter_$dept")
                                ) {
                                    Text(
                                        text = dept,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Filtered Employees List Preview (Cards with Quick Pay Salary & Remove Actions)
                        if (filteredHREmployees.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PersonSearch,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = "No employees match your search criteria.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                filteredHREmployees.take(6).forEach { emp ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0x1FFFFFFF),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2EFFFFFF)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Top Row: Avatar, Name, Role, Base Salary in Rupees
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                Brush.linearGradient(
                                                                    listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                                                                )
                                                            )
                                                            .border(1.dp, Color(0x66FFFFFF), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = emp.name.take(1).uppercase(),
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }

                                                    Column {
                                                        Text(
                                                            text = emp.name,
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = emp.empCode,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFF38BDF8),
                                                                fontSize = 10.sp
                                                            )
                                                            Text(
                                                                text = "• ${emp.department}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = Color(0xFF94A3B8),
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                    }
                                                }

                                                // Salary in Rupees Badge
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = formatCurrency(emp.baseSalary),
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF34D399)
                                                    )
                                                    Text(
                                                        text = "Base / mo",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFF94A3B8),
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            }

                                            // Bottom Action Buttons: Edit Details, Pay Salary, Attendance Log, Remove Staff
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Edit Profile Button (Indigo)
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color(0x336366F1),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x80818CF8)),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { employeeToEdit = emp }
                                                        .testTag("edit_employee_btn_${emp.id}")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Edit,
                                                            contentDescription = "Edit Staff Details",
                                                            tint = Color(0xFFC7D2FE),
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = "Edit",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFC7D2FE),
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }

                                                // Pay Salary Button
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color(0x3310B981),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x8034D399)),
                                                    modifier = Modifier
                                                        .weight(1.2f)
                                                        .clickable {
                                                            viewModel.paySalary(emp.id, "August 2026", emp.baseSalary)
                                                        }
                                                        .testTag("pay_salary_btn_${emp.id}")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Payments,
                                                            contentDescription = null,
                                                            tint = Color(0xFF6EE7B7),
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = "Pay Salary",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF6EE7B7),
                                                            fontSize = 10.5.sp
                                                        )
                                                    }
                                                }

                                                // Attendance Log Shortcut
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color(0x266366F1),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66818CF8)),
                                                    modifier = Modifier
                                                        .weight(0.9f)
                                                        .clickable {
                                                            viewModel.selectEmployee(emp)
                                                            viewModel.navigateTo(AppScreen.ATTENDANCE)
                                                        }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.History,
                                                            contentDescription = null,
                                                            tint = Color(0xFFA5B4FC),
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = "Log",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFA5B4FC),
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }

                                                // Remove Staff Button (Red Glass)
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color(0x2EF43F5E),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FB7185)),
                                                    modifier = Modifier
                                                        .weight(0.9f)
                                                        .clickable { employeeToDelete = emp }
                                                        .testTag("remove_employee_btn_${emp.id}")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.PersonRemove,
                                                            contentDescription = "Remove Staff",
                                                            tint = Color(0xFFFB7185),
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = "Del",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFFB7185),
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Today's Live Attendance Stream with Status Override
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Live Attendance Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(onClick = { viewModel.navigateTo(AppScreen.ATTENDANCE) }) {
                        Text("View All", color = Color(0xFF818CF8), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (todayRecords.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF38BDF8))
                            Text(
                                text = "No punches recorded yet today. Use 'Test Push' or check in.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            } else {
                items(todayRecords.take(5)) { item ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0x1AFFFFFF),
                        borderColor = Color(0x33FFFFFF)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x336366F1)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.employeeName.take(1),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = item.employeeName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${item.empCode} • In: ${item.checkInTime ?: "--"} | Out: ${item.checkOutTime ?: "--"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                AttendanceStatusBadge(status = item.status)
                            }

                            // Quick 1-Click Status Override Row for Admin
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Override: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AttendanceStatus.entries.forEach { statusOption ->
                                    if (statusOption != item.status && statusOption != AttendanceStatus.ON_LEAVE) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0x1FFFFFFF),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2EFFFFFF)),
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp)
                                                .clickable {
                                                    viewModel.overrideAttendanceStatus(
                                                        item.id,
                                                        statusOption,
                                                        item.employeeName,
                                                        item.empCode
                                                    )
                                                }
                                        ) {
                                            Text(
                                                text = when (statusOption) {
                                                    AttendanceStatus.PRESENT -> "P"
                                                    AttendanceStatus.LATE -> "L"
                                                    AttendanceStatus.HALF_DAY -> "HD"
                                                    AttendanceStatus.ABSENT -> "A"
                                                    AttendanceStatus.ON_LEAVE -> "LV"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pending Leave Approvals Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pending Leave Requests",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (pendingLeaves.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color(0x2EF59E0B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FBBF24))
                            ) {
                                Text(
                                    text = "${pendingLeaves.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFBBF24),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    TextButton(onClick = { viewModel.navigateTo(AppScreen.LEAVES) }) {
                        Text("View All", color = Color(0xFF818CF8), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (pendingLeaves.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.TaskAlt, contentDescription = null, tint = Color(0xFF34D399))
                            Text(
                                text = "All leave requests have been reviewed!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            } else {
                items(pendingLeaves.take(3)) { leave ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0x1FFFFFFF),
                        borderColor = Color(0x33FFFFFF)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = leave.employeeName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${leave.empCode} • ${leave.leaveType.name} Leave",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color(0x336366F1),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66818CF8))
                                ) {
                                    Text(
                                        text = "${leave.daysCount} Days",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE0E7FF),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = "\"${leave.reason}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE2E8F0)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0x2EF43F5E),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FB7185)),
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clickable { viewModel.rejectLeave(leave.id) }
                                ) {
                                    Text(
                                        text = "Reject",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFB7185),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0x3310B981),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x8034D399)),
                                    modifier = Modifier
                                        .clickable { viewModel.approveLeave(leave.id) }
                                ) {
                                    Text(
                                        text = "Approve (Live)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6EE7B7),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Removing Employee from Company
    employeeToDelete?.let { emp ->
        AlertDialog(
            onDismissRequest = { employeeToDelete = null },
            title = {
                Text(
                    "Remove ${emp.name}?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    "Are you sure you want to remove ${emp.name} (${emp.empCode}, ${emp.department}) from the company? This action will permanently remove their records from active staff.",
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEmployee(emp)
                        employeeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("confirm_remove_employee_btn")
                ) {
                    Text("Remove Employee", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { employeeToDelete = null }
                ) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E1B4B),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Edit Employee Profile Dialog
    employeeToEdit?.let { emp ->
        EditEmployeeDialog(
            employee = emp,
            onDismiss = { employeeToEdit = null },
            onSave = { updatedEmp ->
                viewModel.updateEmployee(updatedEmp)
                employeeToEdit = null
            }
        )
    }
}
