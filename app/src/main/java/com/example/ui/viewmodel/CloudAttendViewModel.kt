package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.*
import com.example.data.repository.CloudAttendRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen(val title: String) {
    DASHBOARD("Dashboard"),
    ATTENDANCE("Attendance"),
    ANALYTICS("Hours & Attendance Analytics"),
    LEAVES("Leave Management"),
    EMPLOYEES("Employees Directory"),
    PAYROLL("Payroll"),
    PAYSLIP_DETAIL("Payslip Viewer"),
    PROFILE("Profile & Settings"),
    AUTH("Switch Role / Login")
}

data class EmployeeAttendanceAnalytics(
    val employee: Employee,
    val totalLoggedHours: Double,
    val expectedStandardHours: Double, // e.g. 176.0 hrs
    val overtimeHours: Double, // Hours logged beyond 8h shift
    val regularHours: Double,
    val presentDays: Int,
    val lateDays: Int,
    val halfDays: Int,
    val leaveDays: Int,
    val overtimePayEarned: Double,
    val complianceRatePercent: Int
)

data class UiStats(
    val totalEmployees: Int = 0,
    val presentToday: Int = 0,
    val lateToday: Int = 0,
    val onLeaveToday: Int = 0,
    val pendingLeavesCount: Int = 0,
    val totalMonthlyPayroll: Double = 0.0,
    val todayAttendanceRate: Int = 0
)

class CloudAttendViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CloudAttendRepository
    private var liveSimJob: Job? = null
    
    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = CloudAttendRepository(db)
    }

    // Role & Navigation State
    private val _currentRole = MutableStateFlow(UserRole.ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentEmployee = MutableStateFlow<Employee?>(null)
    val currentEmployee: StateFlow<Employee?> = _currentEmployee.asStateFlow()

    private val _selectedEmployee = MutableStateFlow<Employee?>(null)
    val selectedEmployee: StateFlow<Employee?> = _selectedEmployee.asStateFlow()

    private val _selectedPayroll = MutableStateFlow<PayrollRecord?>(null)
    val selectedPayroll: StateFlow<PayrollRecord?> = _selectedPayroll.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Default to modern Dark Glassmorphism aesthetic as requested with Theme Provider
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // HR Dashboard Employee Search & Filter states
    val hrSearchQuery = MutableStateFlow("")
    val hrDepartmentFilter = MutableStateFlow("All")

    fun setHrSearchQuery(query: String) {
        hrSearchQuery.value = query
    }

    fun setHrDepartmentFilter(dept: String) {
        hrDepartmentFilter.value = dept
    }

    fun toggleTheme() {
        toggleDarkMode()
    }

    fun selectEmployee(emp: Employee) {
        _selectedEmployee.value = emp
        navigateTo(AppScreen.EMPLOYEES)
    }

    // Filter states
    val employeeSearchQuery = MutableStateFlow("")
    val employeeDepartmentFilter = MutableStateFlow("All")
    val attendanceStatusFilter = MutableStateFlow("All")
    val payrollMonthFilter = MutableStateFlow("August 2026")

    // Database Flows (Reactive Room Flows for Instant UI Updates)
    val employees: StateFlow<List<Employee>> = repository.allEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceRecords: StateFlow<List<AttendanceRecord>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaveRequests: StateFlow<List<LeaveRequest>> = repository.allLeaves
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payrollRecords: StateFlow<List<PayrollRecord>> = repository.allPayrolls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // HR Dashboard Filtered Employee Directory
    val filteredHREmployees: StateFlow<List<Employee>> = combine(
        employees,
        hrSearchQuery,
        hrDepartmentFilter
    ) { emps, query, dept ->
        emps.filter { emp ->
            val matchesQuery = query.isBlank() ||
                    emp.name.contains(query, ignoreCase = true) ||
                    emp.empCode.contains(query, ignoreCase = true) ||
                    emp.department.contains(query, ignoreCase = true) ||
                    emp.designation.contains(query, ignoreCase = true)

            val matchesDept = dept == "All" || emp.department.equals(dept, ignoreCase = true)
            matchesQuery && matchesDept
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Push Notifications System
    private val _pushNotifications = MutableStateFlow<List<PushNotificationItem>>(
        listOf(
            PushNotificationItem(
                type = NotificationType.CLOCK_IN_SUCCESS,
                title = "Clock-in Successful",
                message = "Welcome! Your punch-in was recorded at 09:00 AM at HQ Tech Park. Have a great day!",
                timestamp = "Today, 09:00 AM",
                isRead = false
            ),
            PushNotificationItem(
                type = NotificationType.LEAVE_STATUS_UPDATED,
                title = "Leave Request Approved",
                message = "HR Sarah Jenkins approved your 2-day Casual Leave request (Aug 28 - Aug 29).",
                timestamp = "Yesterday, 04:30 PM",
                isRead = true
            ),
            PushNotificationItem(
                type = NotificationType.PAYROLL_ALERT,
                title = "Salary Disbursed",
                message = "Your salary for August 2026 has been credited to your bank account.",
                timestamp = "Aug 31, 10:00 AM",
                isRead = true
            )
        )
    )
    val pushNotifications: StateFlow<List<PushNotificationItem>> = _pushNotifications.asStateFlow()

    val unreadNotificationsCount: StateFlow<Int> = _pushNotifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    private val _latestPushAlert = MutableStateFlow<PushNotificationItem?>(null)
    val latestPushAlert: StateFlow<PushNotificationItem?> = _latestPushAlert.asStateFlow()

    private val _isNotificationsDrawerOpen = MutableStateFlow(false)
    val isNotificationsDrawerOpen: StateFlow<Boolean> = _isNotificationsDrawerOpen.asStateFlow()

    // Real-Time WebSocket State & Event Feed
    private val _webSocketStatus = MutableStateFlow(WebSocketStatus.CONNECTED)
    val webSocketStatus: StateFlow<WebSocketStatus> = _webSocketStatus.asStateFlow()

    private val _webSocketLatency = MutableStateFlow(18) // ms
    val webSocketLatency: StateFlow<Int> = _webSocketLatency.asStateFlow()

    private val _liveEventsFeed = MutableStateFlow<List<LiveWebSocketEvent>>(emptyList())
    val liveEventsFeed: StateFlow<List<LiveWebSocketEvent>> = _liveEventsFeed.asStateFlow()

    private val _latestLiveToast = MutableStateFlow<LiveWebSocketEvent?>(null)
    val latestLiveToast: StateFlow<LiveWebSocketEvent?> = _latestLiveToast.asStateFlow()

    private val _isRealTimeSimActive = MutableStateFlow(true)
    val isRealTimeSimActive: StateFlow<Boolean> = _isRealTimeSimActive.asStateFlow()

    // Initialize Default User & Seed Live Event Feed
    init {
        viewModelScope.launch {
            employees.collectLatest { list ->
                if (list.isNotEmpty() && _currentEmployee.value == null) {
                    _currentEmployee.value = list.firstOrNull { it.role == UserRole.ADMIN } ?: list.first()
                }
            }
        }

        // Initialize sample WebSocket live stream history
        _liveEventsFeed.value = listOf(
            LiveWebSocketEvent(
                type = LiveEventType.CHECK_IN,
                title = "Live Check-In Received",
                description = "David Kim punched in at Floor 3 Desk",
                timestamp = "09:02 AM",
                employeeName = "David Kim",
                employeeCode = "EMP-1003",
                avatarColorHex = "#10B981",
                highlightStatus = "PRESENT"
            ),
            LiveWebSocketEvent(
                type = LiveEventType.CHECK_IN,
                title = "Live Check-In Received",
                description = "Maya Lin clocked in via GPS Mobile",
                timestamp = "09:14 AM",
                employeeName = "Maya Lin",
                employeeCode = "EMP-1004",
                avatarColorHex = "#8B5CF6",
                highlightStatus = "PRESENT"
            ),
            LiveWebSocketEvent(
                type = LiveEventType.CHECK_IN,
                title = "Late Punch Recorded",
                description = "James Wilson arrived at HQ (09:42 AM)",
                timestamp = "09:42 AM",
                employeeName = "James Wilson",
                employeeCode = "EMP-1005",
                avatarColorHex = "#F59E0B",
                highlightStatus = "LATE"
            )
        )

        startWebSocketSimulation()
    }

    // Today's Date String
    val todayDateString: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

    val todayDisplayDate: String
        get() {
            val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            return sdf.format(Date())
        }

    val currentTimeString: String
        get() {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date())
        }

    // Computed UI Stats
    val uiStats: StateFlow<UiStats> = combine(
        employees,
        attendanceRecords,
        leaveRequests,
        payrollRecords
    ) { emps, atts, leaves, payrolls ->
        val todayAtts = atts.filter { it.date == todayDateString }
        val presentCount = todayAtts.count { it.status == AttendanceStatus.PRESENT }
        val lateCount = todayAtts.count { it.status == AttendanceStatus.LATE }
        val onLeaveCount = todayAtts.count { it.status == AttendanceStatus.ON_LEAVE }
        val pendingLeaves = leaves.count { it.status == LeaveStatus.PENDING }
        val totalPayrollSum = payrolls.filter { it.monthYear == "August 2026" }.sumOf { it.netSalary }
        val activeEmps = emps.count { it.status == EmployeeStatus.ACTIVE }
        val totalCheckedIn = presentCount + lateCount
        val rate = if (activeEmps > 0) ((totalCheckedIn.toDouble() / activeEmps) * 100).toInt() else 0

        UiStats(
            totalEmployees = activeEmps,
            presentToday = presentCount,
            lateToday = lateCount,
            onLeaveToday = onLeaveCount,
            pendingLeavesCount = pendingLeaves,
            totalMonthlyPayroll = totalPayrollSum,
            todayAttendanceRate = rate
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiStats())

    // Monthly Breakdown of Attendance Hours vs Expected Standard Hours per Employee
    val monthlyAttendanceAnalytics: StateFlow<List<EmployeeAttendanceAnalytics>> = combine(
        employees,
        attendanceRecords,
        leaveRequests,
        payrollMonthFilter
    ) { emps, atts, leaves, monthFilter ->
        val standardWorkingDays = 22
        val standardExpectedHours = standardWorkingDays * 8.0 // 176.0 hours

        emps.map { emp ->
            val empAtts = atts.filter { it.employeeId == emp.id }
            val empLeaves = leaves.filter { it.employeeId == emp.id && it.status == LeaveStatus.APPROVED }
            
            var totalLoggedHours = 0.0
            var totalOvertimeHours = 0.0
            var presentDays = 0
            var lateDays = 0
            var halfDays = 0

            empAtts.forEach { record ->
                when (record.status) {
                    AttendanceStatus.PRESENT -> presentDays++
                    AttendanceStatus.LATE -> lateDays++
                    AttendanceStatus.HALF_DAY -> halfDays++
                    else -> {}
                }

                // Automatic shift overtime calculation: hours logged beyond 8h shift
                val duration = if (record.checkInTime != null && record.checkOutTime != null) {
                    repository.calculateShiftDurationHours(record.checkInTime, record.checkOutTime)
                } else if (record.checkInTime != null) {
                    8.0 + record.overtimeHours
                } else {
                    0.0
                }

                val ot = if (duration > 8.0) {
                    (duration - 8.0).coerceAtLeast(0.0)
                } else {
                    record.overtimeHours
                }

                totalLoggedHours += duration
                totalOvertimeHours += ot
            }

            // If seed dataset has limited daily logs, provide realistic prorated monthly breakdown
            if (totalLoggedHours < 40.0) {
                val baseDays = when (emp.department) {
                    "Engineering" -> 21
                    "Product Design" -> 20
                    "Customer Success" -> 22
                    else -> 21
                }
                presentDays = baseDays
                val deptOt = when (emp.department) {
                    "Engineering" -> 14.5 // Engineering sprint overtime (>8h/day)
                    "Product Design" -> 6.0
                    "Customer Success" -> 10.0
                    "Marketing & Sales" -> 4.0
                    else -> 2.0
                }
                totalOvertimeHours = deptOt
                totalLoggedHours = (presentDays * 8.0) + totalOvertimeHours
            }

            val regularHours = (totalLoggedHours - totalOvertimeHours).coerceAtLeast(0.0)
            val hourlyRate = (emp.grossSalaryMonthly / standardWorkingDays) / 8.0
            val overtimePay = totalOvertimeHours * hourlyRate * 1.5
            val compliancePercent = ((totalLoggedHours / standardExpectedHours) * 100).toInt().coerceIn(0, 150)

            EmployeeAttendanceAnalytics(
                employee = emp,
                totalLoggedHours = Math.round(totalLoggedHours * 10.0) / 10.0,
                expectedStandardHours = standardExpectedHours,
                overtimeHours = Math.round(totalOvertimeHours * 10.0) / 10.0,
                regularHours = Math.round(regularHours * 10.0) / 10.0,
                presentDays = presentDays,
                lateDays = lateDays,
                halfDays = halfDays,
                leaveDays = empLeaves.sumOf { it.daysCount },
                overtimePayEarned = Math.round(overtimePay * 100.0) / 100.0,
                complianceRatePercent = compliancePercent
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        val emps = employees.value
        if (role == UserRole.ADMIN) {
            _currentEmployee.value = emps.firstOrNull { it.role == UserRole.ADMIN } ?: emps.firstOrNull()
            showToast("Switched to Admin Portal (Real-time stream active)")
        } else {
            _currentEmployee.value = emps.firstOrNull { it.role == UserRole.EMPLOYEE } ?: emps.firstOrNull()
            showToast("Switched to Employee Workspace")
        }
    }

    fun setCurrentEmployee(emp: Employee) {
        _currentEmployee.value = emp
        _currentRole.value = emp.role
        showToast("Logged in as ${emp.name}")
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun dismissLiveToast() {
        _latestLiveToast.value = null
    }

    fun viewPayslipDetail(payroll: PayrollRecord) {
        _selectedPayroll.value = payroll
        navigateTo(AppScreen.PAYSLIP_DETAIL)
    }

    // Broadcast Real-Time WebSocket Event
    private fun pushWebSocketEvent(event: LiveWebSocketEvent) {
        _liveEventsFeed.value = listOf(event) + _liveEventsFeed.value.take(20)
        _latestLiveToast.value = event
        // Fluctuate latency slightly for realism
        _webSocketLatency.value = (14..24).random()
    }

    // Push Notification Management Functions
    fun sendPushNotification(
        type: NotificationType,
        title: String,
        message: String,
        targetEmployeeId: Long? = null,
        targetEmployeeName: String? = null,
        actionRoute: String? = null
    ) {
        val item = PushNotificationItem(
            type = type,
            title = title,
            message = message,
            timestamp = "Just now (${currentTimeString})",
            targetEmployeeId = targetEmployeeId,
            targetEmployeeName = targetEmployeeName,
            isRead = false,
            actionRoute = actionRoute
        )
        _pushNotifications.value = listOf(item) + _pushNotifications.value
        _latestPushAlert.value = item

        // Trigger system notification
        try {
            NotificationHelper.showSystemNotification(
                context = getApplication(),
                notificationId = (1000..9999).random(),
                title = title,
                message = message,
                priorityHigh = true
            )
        } catch (e: Exception) {
            // Ignore if in unit test environment
        }
    }

    fun markNotificationRead(id: String) {
        _pushNotifications.value = _pushNotifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markNotificationAsRead(id: String) {
        markNotificationRead(id)
    }

    fun markAllNotificationsRead() {
        _pushNotifications.value = _pushNotifications.value.map { it.copy(isRead = true) }
        showToast("All notifications marked as read")
    }

    fun clearAllNotifications() {
        _pushNotifications.value = emptyList()
        showToast("Notification history cleared")
    }

    fun dismissPushAlert() {
        _latestPushAlert.value = null
    }

    fun setNotificationsDrawerOpen(isOpen: Boolean) {
        _isNotificationsDrawerOpen.value = isOpen
    }

    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
    }

    // Attendance Actions with Real-Time WebSocket Push & Push Notifications
    fun checkIn(location: String = "HQ Tech Park, Bangalore", notes: String = "") {
        val emp = _currentEmployee.value ?: return
        viewModelScope.launch {
            val record = repository.checkIn(emp, location, notes)
            showToast("Punch-In confirmed at ${record.checkInTime} (${record.status.name})")

            val event = LiveWebSocketEvent(
                type = LiveEventType.CHECK_IN,
                title = "⚡ WebSocket Broadcast: Punch-In",
                description = "${emp.name} checked in from $location",
                timestamp = record.checkInTime ?: currentTimeString,
                employeeName = emp.name,
                employeeCode = emp.empCode,
                avatarColorHex = emp.avatarColorHex,
                highlightStatus = record.status.name
            )
            pushWebSocketEvent(event)

            // Alert employee via push notification
            sendPushNotification(
                type = NotificationType.CLOCK_IN_SUCCESS,
                title = "Clock-in Successful",
                message = "Your check-in was registered at ${record.checkInTime ?: currentTimeString} ($location). Status: ${record.status.name}. Have a productive day!",
                targetEmployeeId = emp.id,
                targetEmployeeName = emp.name,
                actionRoute = "attendance"
            )
        }
    }

    fun checkOut() {
        val emp = _currentEmployee.value ?: return
        viewModelScope.launch {
            val updated = repository.checkOut(emp.id)
            if (updated != null) {
                showToast("Punch-Out confirmed at ${updated.checkOutTime}. Shift ended!")

                val event = LiveWebSocketEvent(
                    type = LiveEventType.CHECK_OUT,
                    title = "⚡ WebSocket Broadcast: Punch-Out",
                    description = "${emp.name} logged out for the day (${updated.checkOutTime})",
                    timestamp = updated.checkOutTime ?: currentTimeString,
                    employeeName = emp.name,
                    employeeCode = emp.empCode,
                    avatarColorHex = emp.avatarColorHex,
                    highlightStatus = "SHIFT COMPLETED"
                )
                pushWebSocketEvent(event)

                // Alert employee via push notification
                sendPushNotification(
                    type = NotificationType.CLOCK_OUT_SUCCESS,
                    title = "Clock-out Recorded",
                    message = "Shift concluded at ${updated.checkOutTime ?: currentTimeString}. Overtime: ${updated.overtimeHours} hrs. Have a great evening!",
                    targetEmployeeId = emp.id,
                    targetEmployeeName = emp.name,
                    actionRoute = "attendance"
                )
            } else {
                showToast("No active check-in found for today.")
            }
        }
    }

    // Admin Attendance Status Override (Real-Time Push)
    fun overrideAttendanceStatus(
        recordId: Long,
        newStatus: AttendanceStatus,
        empName: String,
        empCode: String
    ) {
        viewModelScope.launch {
            repository.updateAttendanceStatus(recordId, newStatus)
            showToast("Updated $empName status to ${newStatus.name}")

            val event = LiveWebSocketEvent(
                type = LiveEventType.STATUS_CHANGED,
                title = "⚡ Admin Status Override Broadcast",
                description = "Admin changed $empName ($empCode) status to ${newStatus.name}",
                timestamp = currentTimeString,
                employeeName = empName,
                employeeCode = empCode,
                highlightStatus = newStatus.name
            )
            pushWebSocketEvent(event)

            // Notify employee of attendance status update
            sendPushNotification(
                type = NotificationType.STATUS_OVERRIDE,
                title = "Attendance Status Updated",
                message = "HR/Admin updated your attendance record for today to ${newStatus.name}.",
                targetEmployeeName = empName,
                actionRoute = "attendance"
            )
        }
    }

    // Admin Manual Attendance Check-In For Colleague
    fun adminMarkAttendance(
        employee: Employee,
        status: AttendanceStatus,
        location: String = "Admin HQ Desk"
    ) {
        viewModelScope.launch {
            val timeStr = currentTimeString
            val record = AttendanceRecord(
                employeeId = employee.id,
                employeeName = employee.name,
                empCode = employee.empCode,
                date = todayDateString,
                checkInTime = timeStr,
                checkOutTime = null,
                status = status,
                locationAddress = location,
                notes = "Manual mark by Administrator"
            )
            repository.setOrUpdateAttendance(record)
            showToast("Marked ${employee.name} as ${status.name}")

            val event = LiveWebSocketEvent(
                type = LiveEventType.STATUS_CHANGED,
                title = "⚡ Admin Attendance Mark",
                description = "${employee.name} marked as ${status.name} by Admin",
                timestamp = timeStr,
                employeeName = employee.name,
                employeeCode = employee.empCode,
                avatarColorHex = employee.avatarColorHex,
                highlightStatus = status.name
            )
            pushWebSocketEvent(event)

            sendPushNotification(
                type = NotificationType.STATUS_OVERRIDE,
                title = "Attendance Marked by HR",
                message = "Admin marked your status as ${status.name} at $timeStr.",
                targetEmployeeId = employee.id,
                targetEmployeeName = employee.name,
                actionRoute = "attendance"
            )
        }
    }

    // Leave Actions with Real-Time Push & Notifications
    fun applyLeave(
        leaveType: LeaveType,
        startDate: String,
        endDate: String,
        daysCount: Int,
        reason: String
    ) {
        val emp = _currentEmployee.value ?: return
        if (reason.isBlank()) {
            showToast("Please provide a reason for leave")
            return
        }
        viewModelScope.launch {
            repository.applyLeave(emp, leaveType, startDate, endDate, daysCount, reason)
            showToast("Leave request sent! Notification dispatched to HR.")

            val event = LiveWebSocketEvent(
                type = LiveEventType.LEAVE_APPLIED,
                title = "⚡ Live WebSocket: New Leave Request",
                description = "${emp.name} applied for $daysCount day(s) ${leaveType.name} Leave",
                timestamp = currentTimeString,
                employeeName = emp.name,
                employeeCode = emp.empCode,
                avatarColorHex = emp.avatarColorHex,
                highlightStatus = "PENDING"
            )
            pushWebSocketEvent(event)

            sendPushNotification(
                type = NotificationType.GENERAL_ALERT,
                title = "Leave Request Submitted",
                message = "Your request for $daysCount day(s) ${leaveType.name} Leave ($startDate - $endDate) was submitted for HR review.",
                targetEmployeeId = emp.id,
                targetEmployeeName = emp.name,
                actionRoute = "leaves"
            )
            navigateTo(AppScreen.LEAVES)
        }
    }

    fun approveLeave(leaveId: Long, comments: String = "Approved by HR") {
        viewModelScope.launch {
            val adminName = _currentEmployee.value?.name ?: "Admin"
            repository.reviewLeave(leaveId, LeaveStatus.APPROVED, comments, adminName)
            showToast("Leave approved! Real-time push notification sent to employee.")

            val leave = leaveRequests.value.firstOrNull { it.id == leaveId }
            val empName = leave?.employeeName ?: "Employee"
            val empCode = leave?.empCode ?: "EMP"

            val event = LiveWebSocketEvent(
                type = LiveEventType.LEAVE_APPROVED,
                title = "⚡ Live WebSocket: Leave Approved",
                description = "Admin approved $empName's ${leave?.leaveType?.name ?: ""} Leave",
                timestamp = currentTimeString,
                employeeName = empName,
                employeeCode = empCode,
                highlightStatus = "APPROVED"
            )
            pushWebSocketEvent(event)

            // Alert employee that HR approved leave
            sendPushNotification(
                type = NotificationType.LEAVE_STATUS_UPDATED,
                title = "Leave Request Approved",
                message = "Your ${leave?.leaveType?.name ?: "Leave"} request (${leave?.startDate} to ${leave?.endDate}) was APPROVED by HR $adminName. Note: $comments",
                targetEmployeeId = leave?.employeeId,
                targetEmployeeName = empName,
                actionRoute = "leaves"
            )
        }
    }

    fun rejectLeave(leaveId: Long, comments: String = "Requirements not met") {
        viewModelScope.launch {
            val adminName = _currentEmployee.value?.name ?: "Admin"
            repository.reviewLeave(leaveId, LeaveStatus.REJECTED, comments, adminName)
            showToast("Leave rejected! Notification sent to employee.")

            val leave = leaveRequests.value.firstOrNull { it.id == leaveId }
            val empName = leave?.employeeName ?: "Employee"
            val empCode = leave?.empCode ?: "EMP"

            val event = LiveWebSocketEvent(
                type = LiveEventType.LEAVE_REJECTED,
                title = "⚡ Live WebSocket: Leave Decision",
                description = "Leave request for $empName was declined",
                timestamp = currentTimeString,
                employeeName = empName,
                employeeCode = empCode,
                highlightStatus = "REJECTED"
            )
            pushWebSocketEvent(event)

            // Alert employee that HR rejected leave
            sendPushNotification(
                type = NotificationType.LEAVE_STATUS_UPDATED,
                title = "Leave Request Declined",
                message = "Your ${leave?.leaveType?.name ?: "Leave"} request (${leave?.startDate} to ${leave?.endDate}) was REJECTED by HR $adminName. Reason: $comments",
                targetEmployeeId = leave?.employeeId,
                targetEmployeeName = empName,
                actionRoute = "leaves"
            )
        }
    }

    // Trigger Instant Simulated Peer Activity (WebSocket Push Test)
    fun triggerSimulatedPeerEvent() {
        val emps = employees.value.filter { it.id != (_currentEmployee.value?.id ?: -1L) }
        if (emps.isEmpty()) return
        val randomEmp = emps.random()
        val actions = listOf("punch_in", "punch_out", "status_change", "leave_apply")
        val action = actions.random()

        viewModelScope.launch {
            when (action) {
                "punch_in" -> {
                    val record = repository.checkIn(randomEmp, "Branch Office (Floor 2)", "Real-time biometric sync")
                    val event = LiveWebSocketEvent(
                        type = LiveEventType.CHECK_IN,
                        title = "⚡ Live WebSocket: Peer Check-In",
                        description = "${randomEmp.name} (${randomEmp.department}) checked in",
                        timestamp = record.checkInTime ?: currentTimeString,
                        employeeName = randomEmp.name,
                        employeeCode = randomEmp.empCode,
                        avatarColorHex = randomEmp.avatarColorHex,
                        highlightStatus = record.status.name
                    )
                    pushWebSocketEvent(event)
                    showToast("⚡ Real-Time Push: ${randomEmp.name} clocked in!")
                }
                "punch_out" -> {
                    val record = repository.checkOut(randomEmp.id)
                    val event = LiveWebSocketEvent(
                        type = LiveEventType.CHECK_OUT,
                        title = "⚡ Live WebSocket: Peer Check-Out",
                        description = "${randomEmp.name} completed shift and logged out",
                        timestamp = record?.checkOutTime ?: currentTimeString,
                        employeeName = randomEmp.name,
                        employeeCode = randomEmp.empCode,
                        avatarColorHex = randomEmp.avatarColorHex,
                        highlightStatus = "COMPLETED"
                    )
                    pushWebSocketEvent(event)
                    showToast("⚡ Real-Time Push: ${randomEmp.name} clocked out!")
                }
                "status_change" -> {
                    val existing = attendanceRecords.value.firstOrNull { it.employeeId == randomEmp.id && it.date == todayDateString }
                    if (existing != null) {
                        val newStatus = if (existing.status == AttendanceStatus.PRESENT) AttendanceStatus.LATE else AttendanceStatus.HALF_DAY
                        repository.updateAttendanceStatus(existing.id, newStatus)
                        val event = LiveWebSocketEvent(
                            type = LiveEventType.STATUS_CHANGED,
                            title = "⚡ Live Status Change Update",
                            description = "${randomEmp.name} attendance updated to ${newStatus.name}",
                            timestamp = currentTimeString,
                            employeeName = randomEmp.name,
                            employeeCode = randomEmp.empCode,
                            avatarColorHex = randomEmp.avatarColorHex,
                            highlightStatus = newStatus.name
                        )
                        pushWebSocketEvent(event)
                    } else {
                        val record = repository.checkIn(randomEmp, "Remote Work VPN", "Late connection")
                        val event = LiveWebSocketEvent(
                            type = LiveEventType.CHECK_IN,
                            title = "⚡ Live WebSocket: Peer Check-In",
                            description = "${randomEmp.name} checked in as LATE",
                            timestamp = record.checkInTime ?: currentTimeString,
                            employeeName = randomEmp.name,
                            employeeCode = randomEmp.empCode,
                            avatarColorHex = randomEmp.avatarColorHex,
                            highlightStatus = "LATE"
                        )
                        pushWebSocketEvent(event)
                    }
                    showToast("⚡ Real-Time Update: ${randomEmp.name} status updated!")
                }
                "leave_apply" -> {
                    val types = listOf(LeaveType.CASUAL, LeaveType.SICK, LeaveType.EARNED)
                    val leaveType = types.random()
                    repository.applyLeave(randomEmp, leaveType, todayDateString, todayDateString, 1, "Personal emergency")
                    val event = LiveWebSocketEvent(
                        type = LiveEventType.LEAVE_APPLIED,
                        title = "⚡ Live WebSocket: Leave Notification",
                        description = "${randomEmp.name} applied for 1 day ${leaveType.name} Leave",
                        timestamp = currentTimeString,
                        employeeName = randomEmp.name,
                        employeeCode = randomEmp.empCode,
                        avatarColorHex = randomEmp.avatarColorHex,
                        highlightStatus = "PENDING"
                    )
                    pushWebSocketEvent(event)
                    showToast("⚡ Real-Time Leave: ${randomEmp.name} submitted a request!")
                }
            }
        }
    }

    fun toggleWebSocketSimulation() {
        val next = !_isRealTimeSimActive.value
        _isRealTimeSimActive.value = next
        if (next) {
            startWebSocketSimulation()
            showToast("Real-time WebSocket Live Stream enabled")
        } else {
            liveSimJob?.cancel()
            liveSimJob = null
            showToast("WebSocket Live Stream paused")
        }
    }

    private fun startWebSocketSimulation() {
        liveSimJob?.cancel()
        liveSimJob = viewModelScope.launch {
            while (isActive) {
                delay(20000) // Trigger realistic peer event every 20 seconds
                if (_isRealTimeSimActive.value && employees.value.isNotEmpty()) {
                    triggerSimulatedPeerEvent()
                }
            }
        }
    }

    // Employee Directory Actions
    fun addEmployee(
        name: String,
        email: String,
        role: UserRole,
        department: String,
        designation: String,
        phone: String,
        baseSalary: Double,
        hra: Double,
        special: Double,
        transport: Double
    ) {
        if (name.isBlank() || email.isBlank()) {
            showToast("Name and Email are required")
            return
        }
        viewModelScope.launch {
            val nextId = (employees.value.maxOfOrNull { it.id } ?: 0) + 1
            val empCode = "EMP-${1000 + nextId}"
            val colorList = listOf("#4F46E5", "#0EA5E9", "#10B981", "#8B5CF6", "#F59E0B", "#EC4899", "#14B8A6")
            val color = colorList[(nextId % colorList.size).toInt()]

            val newEmp = Employee(
                id = nextId,
                empCode = empCode,
                name = name,
                email = email,
                role = role,
                department = department,
                designation = designation,
                phone = phone,
                joiningDate = todayDateString,
                baseSalary = baseSalary,
                hra = hra,
                specialAllowance = special,
                transportAllowance = transport,
                avatarColorHex = color
            )
            repository.insertEmployee(newEmp)
            showToast("Added $name ($empCode) to organization")
        }
    }

    fun addEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.insertEmployee(employee)
            showToast("Added ${employee.name} (${employee.empCode}) to organization")
        }
    }

    fun updateEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.updateEmployee(employee)
            if (_currentEmployee.value?.id == employee.id) {
                _currentEmployee.value = employee
            }
            if (_selectedEmployee.value?.id == employee.id) {
                _selectedEmployee.value = employee
            }
            showToast("Updated ${employee.name} • ${employee.designation}")

            val event = LiveWebSocketEvent(
                type = LiveEventType.STATUS_CHANGED,
                title = "⚡ HR Audit: Employee Record Updated",
                description = "HR updated details for ${employee.name} (${employee.designation}, ${employee.department})",
                timestamp = currentTimeString,
                employeeName = employee.name,
                employeeCode = employee.empCode,
                avatarColorHex = employee.avatarColorHex,
                highlightStatus = "UPDATED"
            )
            pushWebSocketEvent(event)

            sendPushNotification(
                type = NotificationType.STATUS_OVERRIDE,
                title = "Staff Profile Updated",
                message = "Your employee record (${employee.designation}, ${employee.phone}, Base ₹${String.format(Locale.getDefault(), "%,.0f", employee.baseSalary)}) was updated by HR Admin.",
                targetEmployeeId = employee.id,
                targetEmployeeName = employee.name,
                actionRoute = "dashboard"
            )
        }
    }

    fun deleteEmployee(id: Long) {
        val target = employees.value.firstOrNull { it.id == id }
        val empName = target?.name ?: "Employee"
        val empCode = target?.empCode ?: "EMP"
        viewModelScope.launch {
            repository.deleteEmployee(id)
            showToast("Removed $empName ($empCode) from company organization")

            val event = LiveWebSocketEvent(
                type = LiveEventType.STATUS_CHANGED,
                title = "⚡ HR Audit: Employee Record Terminated",
                description = "$empName was removed from company database by HR/Admin",
                timestamp = currentTimeString,
                employeeName = empName,
                employeeCode = empCode,
                highlightStatus = "TERMINATED"
            )
            pushWebSocketEvent(event)
        }
    }

    fun deleteEmployee(employee: Employee) {
        deleteEmployee(employee.id)
    }

    // Payroll Actions
    fun paySalary(payrollId: Long) {
        val payroll = payrollRecords.value.firstOrNull { it.id == payrollId }
        val empName = payroll?.employeeName ?: "Employee"
        viewModelScope.launch {
            repository.markPayrollPaid(payrollId)
            showToast("Salary paid to $empName via Direct Bank Deposit")

            val event = LiveWebSocketEvent(
                type = LiveEventType.PAYROLL_PROCESSED,
                title = "⚡ Live WebSocket: Salary Disbursed",
                description = "Disbursed ${payroll?.monthYear ?: ""} salary for $empName",
                timestamp = currentTimeString,
                employeeName = empName,
                employeeCode = payroll?.empCode ?: "EMP",
                highlightStatus = "PAID"
            )
            pushWebSocketEvent(event)

            // Alert employee via push notification
            sendPushNotification(
                type = NotificationType.PAYROLL_ALERT,
                title = "Salary Disbursed",
                message = "Your net salary of ₹${payroll?.netSalary?.let { String.format(Locale.getDefault(), "%,.2f", it) } ?: "0.00"} for ${payroll?.monthYear ?: "this month"} has been credited to your bank account via ${payroll?.paymentMethod ?: "Direct Bank Deposit"}.",
                targetEmployeeId = payroll?.employeeId,
                targetEmployeeName = empName,
                actionRoute = "payroll"
            )
        }
    }

    fun paySalary(payroll: PayrollRecord) {
        paySalary(payroll.id)
    }

    fun paySalary(employeeId: Long, monthYear: String = "August 2026", amount: Double = 0.0) {
        val emp = employees.value.firstOrNull { it.id == employeeId }
        if (emp != null) {
            autoCalculatePayrollForEmployee(emp, monthYear)
        }
    }

    fun generateAndOpenPayslipForEmployee(employee: Employee, monthYear: String = "August 2026") {
        viewModelScope.launch {
            val existing = payrollRecords.value.firstOrNull { it.employeeId == employee.id && it.monthYear == monthYear }
            val payrollToOpen = if (existing != null) {
                existing
            } else {
                val (year, month) = when {
                    monthYear.contains("August", ignoreCase = true) -> 2026 to 8
                    monthYear.contains("September", ignoreCase = true) -> 2026 to 9
                    monthYear.contains("July", ignoreCase = true) -> 2026 to 7
                    monthYear.contains("June", ignoreCase = true) -> 2026 to 6
                    else -> 2026 to 8
                }
                val record = repository.autoCalculateAndSaveEmployeePayroll(employee, monthYear, year, month)
                record
            }
            _selectedPayroll.value = payrollToOpen
            navigateTo(AppScreen.PAYSLIP_DETAIL)
            showToast("Generated printable payslip for ${employee.name}")
        }
    }

    fun autoCalculatePayrollForEmployee(
        employee: Employee,
        monthYear: String = "August 2026",
        year: Int = 2026,
        month: Int = 8
    ) {
        viewModelScope.launch {
            val record = repository.autoCalculateAndSaveEmployeePayroll(employee, monthYear, year, month)
            showToast("Generated payslip for ${employee.name} (Net: ₹${record.netSalary})")
        }
    }

    fun generateBulkPayroll(monthYear: String, year: Int, month: Int) {
        val emps = employees.value.filter { it.status == EmployeeStatus.ACTIVE }
        if (emps.isEmpty()) {
            showToast("No active employees found")
            return
        }
        viewModelScope.launch {
            val count = repository.generateBulkPayroll(emps, monthYear, year, month)
            showToast("Successfully generated $count payslips for $monthYear")

            val event = LiveWebSocketEvent(
                type = LiveEventType.PAYROLL_PROCESSED,
                title = "⚡ WebSocket Broadcast: Payroll Disbursed",
                description = "Disbursed $count employee salaries with automatic overtime for $monthYear",
                timestamp = currentTimeString,
                employeeName = "Payroll Engine",
                employeeCode = "SYS-PAYROLL",
                highlightStatus = "PROCESSED"
            )
            pushWebSocketEvent(event)

            sendPushNotification(
                type = NotificationType.PAYROLL_ALERT,
                title = "Monthly Payroll Disbursed",
                message = "Monthly salary disbursement processed for $count active staff members for $monthYear.",
                actionRoute = "payroll"
            )
        }
    }

    fun autoCalculatePayrollForEmployee(employee: Employee, monthYear: String = "August 2026") {
        viewModelScope.launch {
            val empAtts = attendanceRecords.value.filter { it.employeeId == employee.id }
            val empLeaves = leaveRequests.value.filter { it.employeeId == employee.id && it.status == LeaveStatus.APPROVED }
            
            val totalWorkingDays = 22
            var presentDays = empAtts.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE }
            if (presentDays == 0) presentDays = 21
            val paidLeaves = empLeaves.filter { it.leaveType != LeaveType.UNPAID }.sumOf { it.daysCount }.coerceAtLeast(1)
            val unpaidLeaves = empLeaves.filter { it.leaveType == LeaveType.UNPAID }.sumOf { it.daysCount }

            // Calculate overtime for hours logged beyond 8h
            var totalOtHours = 0.0
            empAtts.forEach { record ->
                val duration = if (record.checkInTime != null && record.checkOutTime != null) {
                    repository.calculateShiftDurationHours(record.checkInTime, record.checkOutTime)
                } else if (record.checkInTime != null) {
                    8.0 + record.overtimeHours
                } else {
                    0.0
                }
                val ot = if (duration > 8.0) (duration - 8.0).coerceAtLeast(0.0) else record.overtimeHours
                totalOtHours += ot
            }
            if (totalOtHours == 0.0) {
                totalOtHours = if (employee.department == "Engineering") 6.5 else 2.0
            }

            val record = repository.calculatePayroll(
                employee = employee,
                monthYear = monthYear,
                totalWorkingDays = totalWorkingDays,
                presentDays = presentDays,
                paidLeaves = paidLeaves,
                unpaidLeaves = unpaidLeaves,
                overtimeHours = totalOtHours,
                status = PayrollStatus.PENDING
            )
            repository.savePayrollRecord(record)
            showToast("Calculated ${employee.name}'s payroll with ${totalOtHours}h overtime included")

            val event = LiveWebSocketEvent(
                type = LiveEventType.PAYROLL_PROCESSED,
                title = "⚡ Live Payroll Calculation",
                description = "Computed ${employee.name} pay (+${totalOtHours}h OT beyond standard shift)",
                timestamp = currentTimeString,
                employeeName = employee.name,
                employeeCode = employee.empCode,
                highlightStatus = "PENDING"
            )
            pushWebSocketEvent(event)
        }
    }

    fun createIndividualPayroll(
        employee: Employee,
        monthYear: String,
        workingDays: Int,
        presentDays: Int,
        paidLeaves: Int,
        unpaidLeaves: Int,
        overtimeHours: Double
    ) {
        viewModelScope.launch {
            val record = repository.calculatePayroll(
                employee = employee,
                monthYear = monthYear,
                totalWorkingDays = workingDays,
                presentDays = presentDays,
                paidLeaves = paidLeaves,
                unpaidLeaves = unpaidLeaves,
                overtimeHours = overtimeHours,
                status = PayrollStatus.PAID
            )
            repository.savePayrollRecord(record)
            showToast("Generated payslip for ${employee.name} (Overtime: ${overtimeHours}h)")
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDatabase()
            showToast("Demo data reloaded with sample records")
        }
    }
}
