package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Employee
import com.example.data.model.LeaveRequest
import com.example.data.model.LeaveStatus
import com.example.data.model.LeaveType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class CalendarDayType {
    WORKED_PRESENT,
    WORKED_LATE,
    WORKED_HALF_DAY,
    APPROVED_LEAVE,
    PENDING_LEAVE,
    WEEKEND_REST,
    UPCOMING_WORKDAY,
    EMPTY_SLOT
}

data class CalendarDayModel(
    val dayNumber: Int,
    val dateString: String, // YYYY-MM-DD
    val dayOfWeek: Int,    // 1 = Monday, 7 = Sunday
    val isWeekend: Boolean,
    val isToday: Boolean,
    val isCurrentMonth: Boolean,
    val type: CalendarDayType,
    val attendanceRecord: AttendanceRecord?,
    val approvedLeave: LeaveRequest?,
    val pendingLeave: LeaveRequest?
)

/**
 * Interactive Calendar View for Employee Dashboard.
 * Visually distinguishes worked days, approved leave dates, pending leaves, and weekends.
 */
@Composable
fun EmployeeAttendanceCalendarCard(
    employee: Employee?,
    attendanceRecords: List<AttendanceRecord>,
    leaveRequests: List<LeaveRequest>,
    todayDateString: String,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current

    // Calendar navigation state (defaults to August 2026 or current month/year)
    var selectedYear by remember { mutableIntStateOf(2026) }
    var selectedMonth by remember { mutableIntStateOf(8) } // 1-indexed (8 = August)
    var selectedDateString by remember { mutableStateOf(todayDateString) }

    // Month Names lookup
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Calculate calendar grid days
    val calendarDays = remember(selectedYear, selectedMonth, attendanceRecords, leaveRequests, employee, todayDateString) {
        val days = mutableListOf<CalendarDayModel>()
        val empId = employee?.id ?: 0L

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        // Offset for Monday as first column (Monday = 0, Sunday = 6)
        val leadingBlanks = (firstDayOfWeek - Calendar.MONDAY + 7) % 7

        // Add leading blanks
        for (i in 0 until leadingBlanks) {
            days.add(
                CalendarDayModel(
                    dayNumber = 0,
                    dateString = "",
                    dayOfWeek = 0,
                    isWeekend = false,
                    isToday = false,
                    isCurrentMonth = false,
                    type = CalendarDayType.EMPTY_SLOT,
                    attendanceRecord = null,
                    approvedLeave = null,
                    pendingLeave = null
                )
            )
        }

        // Add actual days
        for (day in 1..daysInMonth) {
            val dateStr = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth, day)
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            val isWeekend = (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY)
            val isToday = (dateStr == todayDateString)

            // Find attendance record
            val attRecord = attendanceRecords.firstOrNull { it.employeeId == empId && it.date == dateStr }

            // Find approved leave spanning this date
            val approvedLeave = leaveRequests.firstOrNull {
                it.employeeId == empId &&
                        it.status == LeaveStatus.APPROVED &&
                        dateStr >= it.startDate &&
                        dateStr <= it.endDate
            }

            // Find pending leave spanning this date
            val pendingLeave = leaveRequests.firstOrNull {
                it.employeeId == empId &&
                        it.status == LeaveStatus.PENDING &&
                        dateStr >= it.startDate &&
                        dateStr <= it.endDate
            }

            val type = when {
                approvedLeave != null -> CalendarDayType.APPROVED_LEAVE
                pendingLeave != null -> CalendarDayType.PENDING_LEAVE
                attRecord != null -> when (attRecord.status) {
                    AttendanceStatus.PRESENT -> CalendarDayType.WORKED_PRESENT
                    AttendanceStatus.LATE -> CalendarDayType.WORKED_LATE
                    AttendanceStatus.HALF_DAY -> CalendarDayType.WORKED_HALF_DAY
                    AttendanceStatus.ON_LEAVE -> CalendarDayType.APPROVED_LEAVE
                    AttendanceStatus.ABSENT -> CalendarDayType.UPCOMING_WORKDAY
                }
                isWeekend -> CalendarDayType.WEEKEND_REST
                else -> CalendarDayType.UPCOMING_WORKDAY
            }

            days.add(
                CalendarDayModel(
                    dayNumber = day,
                    dateString = dateStr,
                    dayOfWeek = if (dow == Calendar.SUNDAY) 7 else dow - 1,
                    isWeekend = isWeekend,
                    isToday = isToday,
                    isCurrentMonth = true,
                    type = type,
                    attendanceRecord = attRecord,
                    approvedLeave = approvedLeave,
                    pendingLeave = pendingLeave
                )
            )
        }

        days
    }

    // Selected day model
    val selectedDayModel = remember(calendarDays, selectedDateString) {
        calendarDays.firstOrNull { it.isCurrentMonth && it.dateString == selectedDateString }
            ?: calendarDays.firstOrNull { it.isCurrentMonth && it.isToday }
            ?: calendarDays.firstOrNull { it.isCurrentMonth }
    }

    // Month summary metrics
    val workedDaysCount = remember(calendarDays) {
        calendarDays.count {
            it.isCurrentMonth && (it.type == CalendarDayType.WORKED_PRESENT || it.type == CalendarDayType.WORKED_LATE || it.type == CalendarDayType.WORKED_HALF_DAY)
        }
    }
    val approvedLeavesCount = remember(calendarDays) {
        calendarDays.count { it.isCurrentMonth && it.type == CalendarDayType.APPROVED_LEAVE }
    }
    val pendingLeavesCount = remember(calendarDays) {
        calendarDays.count { it.isCurrentMonth && it.type == CalendarDayType.PENDING_LEAVE }
    }
    val weekendCount = remember(calendarDays) {
        calendarDays.count { it.isCurrentMonth && it.isWeekend }
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("employee_attendance_calendar_card"),
        backgroundColor = if (isDark) Color(0x281E1B4B) else Color(0x70FFFFFF),
        borderColor = if (isDark) Color(0x66818CF8) else Color(0x66C7D2FE)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Title, Month Selector & Navigation Controls
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Attendance & Leave Visualizer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E1B4B)
                        )
                        Text(
                            text = "Color-coded daily shifts & approved leaves",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }

                // Jump to Today button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0x33818CF8) else Color(0xFFEEF2FF),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDark) Color(0x66818CF8) else Color(0xFFA5B4FC)
                    ),
                    modifier = Modifier
                        .clickable {
                            selectedYear = 2026
                            selectedMonth = 8
                            selectedDateString = todayDateString
                        }
                        .testTag("calendar_today_btn")
                ) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFC7D2FE) else Color(0xFF4338CA),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Month Navigation Bar (< August 2026 >)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0x1FFFFFFF) else Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDark) Color(0x26FFFFFF) else Color(0xFFE2E8F0)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (selectedMonth == 1) {
                                selectedMonth = 12
                                selectedYear -= 1
                            } else {
                                selectedMonth -= 1
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("calendar_prev_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            tint = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "${monthNames[selectedMonth - 1]} $selectedYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        letterSpacing = 0.5.sp
                    )

                    IconButton(
                        onClick = {
                            if (selectedMonth == 12) {
                                selectedMonth = 1
                                selectedYear += 1
                            } else {
                                selectedMonth += 1
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("calendar_next_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            tint = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Summary Metrics Row (Worked, Leaves, Pending, Weekends)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Worked Days Pill
                SummaryMetricPill(
                    count = workedDaysCount,
                    label = "Worked",
                    icon = Icons.Filled.CheckCircle,
                    accentColor = Color(0xFF10B981),
                    bgColor = if (isDark) Color(0x2810B981) else Color(0xFFECFDF5),
                    borderColor = if (isDark) Color(0x6634D399) else Color(0xFFA7F3D0),
                    modifier = Modifier.weight(1f)
                )

                // Approved Leaves Pill
                SummaryMetricPill(
                    count = approvedLeavesCount,
                    label = "Approved Leaves",
                    icon = Icons.Filled.EventAvailable,
                    accentColor = Color(0xFF8B5CF6),
                    bgColor = if (isDark) Color(0x288B5CF6) else Color(0xFFF5F3FF),
                    borderColor = if (isDark) Color(0x66A78BFA) else Color(0xFFDDD6FE),
                    modifier = Modifier.weight(1.2f)
                )

                // Pending Leaves Pill
                SummaryMetricPill(
                    count = pendingLeavesCount,
                    label = "Pending",
                    icon = Icons.Filled.HourglassEmpty,
                    accentColor = Color(0xFFF59E0B),
                    bgColor = if (isDark) Color(0x28F59E0B) else Color(0xFFFFFBEB),
                    borderColor = if (isDark) Color(0x66FBBF24) else Color(0xFFFDE68A),
                    modifier = Modifier.weight(1f)
                )

                // Rest Days Pill
                SummaryMetricPill(
                    count = weekendCount,
                    label = "Rest Days",
                    icon = Icons.Filled.Bedtime,
                    accentColor = Color(0xFF64748B),
                    bgColor = if (isDark) Color(0x2864748B) else Color(0xFFF1F5F9),
                    borderColor = if (isDark) Color(0x6694A3B8) else Color(0xFFCBD5E1),
                    modifier = Modifier.weight(1f)
                )
            }

            // Calendar Grid Container
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0x14000000) else Color(0xFFFFFFFF),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDark) Color(0x33FFFFFF) else Color(0xFFE2E8F0)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Day-of-Week Header Row (Mon, Tue, Wed, Thu, Fri, Sat, Sun)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val daysOfWeek = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                        daysOfWeek.forEach { dayName ->
                            val isWknd = dayName == "SAT" || dayName == "SUN"
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                color = if (isWknd) (if (isDark) Color(0xFF94A3B8) else Color(0xFF94A3B8))
                                else (if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = if (isDark) Color(0x26FFFFFF) else Color(0xFFF1F5F9),
                        thickness = 1.dp
                    )

                    // Calendar Grid Rows (Chunks of 7)
                    val rows = calendarDays.chunked(7)
                    rows.forEach { weekRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Ensure 7 cells per row
                            for (cellIndex in 0 until 7) {
                                val dayModel = weekRow.getOrNull(cellIndex)
                                if (dayModel != null && dayModel.isCurrentMonth) {
                                    val isSelected = dayModel.dateString == (selectedDayModel?.dateString ?: "")
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(2.dp)
                                    ) {
                                        CalendarDayCell(
                                            day = dayModel,
                                            isSelected = isSelected,
                                            onClick = {
                                                selectedDateString = dayModel.dateString
                                            }
                                        )
                                    }
                                } else {
                                    // Empty slot filler
                                    Spacer(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Selected Day Details Card
            selectedDayModel?.let { day ->
                SelectedDayDetailsCard(day = day)
            }

            // Legend Row for quick visual scanning
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendIndicatorItem(color = Color(0xFF10B981), label = "Worked")
                LegendIndicatorItem(color = Color(0xFF8B5CF6), label = "Approved Leave")
                LegendIndicatorItem(color = Color(0xFFF59E0B), label = "Pending / Late")
                LegendIndicatorItem(color = Color(0xFF64748B), label = "Weekend")
            }
        }
    }
}

@Composable
private fun SummaryMetricPill(
    count: Int,
    label: String,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDayModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = LocalThemeIsDark.current

    val cellBg = when {
        isSelected -> if (isDark) Color(0x666366F1) else Color(0xFFE0E7FF)
        day.type == CalendarDayType.APPROVED_LEAVE -> if (isDark) Color(0x388B5CF6) else Color(0xFFF3E8FF)
        day.type == CalendarDayType.PENDING_LEAVE -> if (isDark) Color(0x28F59E0B) else Color(0xFFFEF3C7)
        day.type == CalendarDayType.WORKED_PRESENT -> if (isDark) Color(0x2E10B981) else Color(0xFFD1FAE5)
        day.type == CalendarDayType.WORKED_LATE -> if (isDark) Color(0x2EF59E0B) else Color(0xFFFEF3C7)
        day.type == CalendarDayType.WORKED_HALF_DAY -> if (isDark) Color(0x2E0EA5E9) else Color(0xFFE0F2FE)
        day.isWeekend -> if (isDark) Color(0x0FFFFFFF) else Color(0xFFF8FAFC)
        else -> if (isDark) Color(0x14FFFFFF) else Color(0xFFFFFFFF)
    }

    val borderStroke = when {
        isSelected -> androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF818CF8))
        day.isToday -> androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8))
        day.type == CalendarDayType.APPROVED_LEAVE -> androidx.compose.foundation.BorderStroke(1.dp, Color(0x99A78BFA))
        day.type == CalendarDayType.PENDING_LEAVE -> androidx.compose.foundation.BorderStroke(1.dp, Color(0x80FBBF24))
        day.type == CalendarDayType.WORKED_PRESENT -> androidx.compose.foundation.BorderStroke(1.dp, Color(0x8034D399))
        else -> androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0x1FFFFFFF) else Color(0xFFE2E8F0))
    }

    val textColor = when {
        day.type == CalendarDayType.APPROVED_LEAVE -> if (isDark) Color(0xFFC084FC) else Color(0xFF7E22CE)
        day.type == CalendarDayType.WORKED_PRESENT -> if (isDark) Color(0xFF34D399) else Color(0xFF047857)
        day.type == CalendarDayType.WORKED_LATE -> if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
        day.isWeekend -> if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
        else -> if (isDark) Color.White else Color(0xFF1E293B)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = cellBg,
        border = borderStroke,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { onClick() }
            .testTag("calendar_day_${day.dateString}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Day Number & Today indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${day.dayNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (day.isToday || isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = textColor
                )

                if (day.isToday) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8))
                    )
                }
            }

            // Bottom Badges/Dots
            when (day.type) {
                CalendarDayType.APPROVED_LEAVE -> {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isDark) Color(0xFF7C3AED) else Color(0xFF9333EA)
                    ) {
                        Text(
                            text = when (day.approvedLeave?.leaveType) {
                                LeaveType.SICK -> "SL"
                                LeaveType.CASUAL -> "CL"
                                LeaveType.EARNED -> "PL"
                                LeaveType.UNPAID -> "UL"
                                else -> "LV"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }
                }
                CalendarDayType.PENDING_LEAVE -> {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0x4DF59E0B),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFF59E0B))
                    ) {
                        Text(
                            text = "PEND",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
                CalendarDayType.WORKED_PRESENT -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        if (day.attendanceRecord?.checkOutTime != null) {
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
                CalendarDayType.WORKED_LATE -> {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isDark) Color(0xFFD97706) else Color(0xFFF59E0B)
                    ) {
                        Text(
                            text = "LATE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
                CalendarDayType.WORKED_HALF_DAY -> {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF0284C7)
                    ) {
                        Text(
                            text = "HALF",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
                CalendarDayType.WEEKEND_REST -> {
                    Text(
                        text = "REST",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 7.sp,
                        color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
                    )
                }
                CalendarDayType.UPCOMING_WORKDAY -> {
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x33FFFFFF) else Color(0xFFCBD5E1))
                    )
                }
                CalendarDayType.EMPTY_SLOT -> {}
            }
        }
    }
}

@Composable
private fun SelectedDayDetailsCard(day: CalendarDayModel) {
    val isDark = LocalThemeIsDark.current

    // Format display date string (e.g. "Monday, August 31, 2026")
    val formattedDate = remember(day.dateString) {
        try {
            val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val sdfOut = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
            val date = sdfIn.parse(day.dateString)
            if (date != null) sdfOut.format(date) else day.dateString
        } catch (e: Exception) {
            day.dateString
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDark) Color(0x28000000) else Color(0xFFF1F5F9),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) Color(0x33FFFFFF) else Color(0xFFCBD5E1)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("selected_day_details_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title & Status Badge
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
                        imageVector = Icons.Filled.Event,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                }

                // Status Pill
                val (statusText, statusBg, statusColor) = when (day.type) {
                    CalendarDayType.WORKED_PRESENT -> Triple("PRESENT", Color(0x3310B981), Color(0xFF34D399))
                    CalendarDayType.WORKED_LATE -> Triple("LATE PUNCH", Color(0x33F59E0B), Color(0xFFFBBF24))
                    CalendarDayType.WORKED_HALF_DAY -> Triple("HALF DAY", Color(0x330EA5E9), Color(0xFF38BDF8))
                    CalendarDayType.APPROVED_LEAVE -> Triple(
                        "APPROVED ${day.approvedLeave?.leaveType?.name ?: "LEAVE"}",
                        Color(0x338B5CF6),
                        Color(0xFFC084FC)
                    )
                    CalendarDayType.PENDING_LEAVE -> Triple(
                        "PENDING ${day.pendingLeave?.leaveType?.name ?: "LEAVE"}",
                        Color(0x33F59E0B),
                        Color(0xFFFBBF24)
                    )
                    CalendarDayType.WEEKEND_REST -> Triple("WEEKEND", Color(0x2864748B), Color(0xFF94A3B8))
                    CalendarDayType.UPCOMING_WORKDAY -> Triple("SCHEDULED SHIFT", Color(0x286366F1), Color(0xFFA5B4FC))
                    CalendarDayType.EMPTY_SLOT -> Triple("", Color.Transparent, Color.Transparent)
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = statusBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Detailed Content based on type
            when (day.type) {
                CalendarDayType.WORKED_PRESENT, CalendarDayType.WORKED_LATE, CalendarDayType.WORKED_HALF_DAY -> {
                    val record = day.attendanceRecord
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DetailItem(label = "Check-In", value = record?.checkInTime ?: "09:00 AM", color = Color(0xFF34D399))
                            DetailItem(label = "Check-Out", value = record?.checkOutTime ?: "In Progress (Active)", color = Color(0xFF38BDF8))
                            DetailItem(label = "Overtime", value = if ((record?.overtimeHours ?: 0.0) > 0) "+${record?.overtimeHours} hrs" else "None", color = Color(0xFFA5B4FC))
                        }

                        if (!record?.locationAddress.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                                Text(
                                    text = record?.locationAddress ?: "HQ Tech Park",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                                )
                            }
                        }

                        if (!record?.notes.isNullOrBlank()) {
                            Text(
                                text = "Notes: ${record?.notes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                CalendarDayType.APPROVED_LEAVE -> {
                    val leave = day.approvedLeave
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DetailItem(label = "Leave Category", value = "${leave?.leaveType?.name ?: "Casual"} Leave (${leave?.daysCount ?: 1} Days)", color = Color(0xFFC084FC))
                            DetailItem(label = "Applied On", value = leave?.appliedOn ?: "--", color = Color(0xFFCBD5E1))
                        }

                        if (!leave?.reason.isNullOrBlank()) {
                            Text(
                                text = "Reason: \"${leave?.reason}\"",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp,
                                color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
                            )
                        }

                        if (!leave?.reviewedBy.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(13.dp))
                                Text(
                                    text = "Approved by ${leave?.reviewedBy}${if (leave?.adminComments != null) " • \"${leave.adminComments}\"" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF34D399),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                CalendarDayType.PENDING_LEAVE -> {
                    val leave = day.pendingLeave
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Pending Leave Request: ${leave?.leaveType?.name} Leave (${leave?.daysCount} day(s))",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFBBF24)
                        )
                        Text(
                            text = "Reason: \"${leave?.reason}\" • Awaiting HR approval.",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                        )
                    }
                }

                CalendarDayType.WEEKEND_REST -> {
                    Text(
                        text = "Scheduled Weekend Rest Day (Standard Non-working day)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }

                CalendarDayType.UPCOMING_WORKDAY -> {
                    Text(
                        text = "Scheduled Standard Shift (09:00 AM - 05:00 PM). Biometric & mobile punch enabled.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                    )
                }

                CalendarDayType.EMPTY_SLOT -> {}
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, color: Color) {
    val isDark = LocalThemeIsDark.current
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = color
        )
    }
}

@Composable
private fun LegendIndicatorItem(color: Color, label: String) {
    val isDark = LocalThemeIsDark.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.5.sp,
            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
        )
    }
}
