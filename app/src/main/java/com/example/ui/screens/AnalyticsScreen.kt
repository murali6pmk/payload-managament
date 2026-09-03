package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.SaaSStatCard
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatCurrencyDetailed
import com.example.ui.theme.*
import com.example.ui.viewmodel.CloudAttendViewModel
import com.example.ui.viewmodel.EmployeeAttendanceAnalytics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: CloudAttendViewModel,
    modifier: Modifier = Modifier
) {
    val analyticsList by viewModel.monthlyAttendanceAnalytics.collectAsState()
    var selectedMonth by remember { mutableStateOf("August 2026") }
    val months = listOf("August 2026", "September 2026", "July 2026", "June 2026")

    var selectedEmployeeId by remember { mutableStateOf<Long?>(null) }

    // Set default selected employee if available
    LaunchedEffect(analyticsList) {
        if (selectedEmployeeId == null && analyticsList.isNotEmpty()) {
            selectedEmployeeId = analyticsList.first().employee.id
        }
    }

    val selectedEmployeeData = remember(analyticsList, selectedEmployeeId) {
        analyticsList.firstOrNull { it.employee.id == selectedEmployeeId } ?: analyticsList.firstOrNull()
    }

    val totalLoggedHoursSum = remember(analyticsList) {
        analyticsList.sumOf { it.totalLoggedHours }
    }

    val totalExpectedHoursSum = remember(analyticsList) {
        analyticsList.sumOf { it.expectedStandardHours }
    }

    val totalOvertimeHoursSum = remember(analyticsList) {
        analyticsList.sumOf { it.overtimeHours }
    }

    val totalOvertimePaySum = remember(analyticsList) {
        analyticsList.sumOf { it.overtimePayEarned }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Month Selector
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0x281E1B4B),
                borderColor = Color(0x66818CF8)
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
                        Column {
                            Text(
                                text = "Attendance & Hours Analytics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Monthly Actual Hours vs Standard 8-Hour Quota",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1)
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
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF34D399))
                                )
                                Text(
                                    text = "Auto OT Engine",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    }

                    // Month Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(months) { monthName ->
                            val isSelected = selectedMonth == monthName
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (isSelected) Color(0x336366F1) else Color(0x1AFFFFFF),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0x80818CF8) else Color(0x26FFFFFF)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable { selectedMonth = monthName }
                            ) {
                                Text(
                                    text = monthName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF818CF8) else Color(0xFFCBD5E1),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Summary Metric SaaS Stat Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SaaSStatCard(
                    title = "Total Hours Logged",
                    value = "${totalLoggedHoursSum.toInt()} hrs",
                    subtitle = "Across all staff",
                    icon = Icons.Filled.Timelapse,
                    iconColor = NeonIndigo,
                    iconBgColor = Color(0x336366F1),
                    modifier = Modifier.weight(1f)
                )
                SaaSStatCard(
                    title = "Overtime Accrued",
                    value = "+${totalOvertimeHoursSum.toInt()} hrs",
                    subtitle = "Hours beyond 8h/day",
                    icon = Icons.Filled.MoreTime,
                    iconColor = NeonCyan,
                    iconBgColor = Color(0x3306B6D4),
                    modifier = Modifier.weight(1f)
                )
                SaaSStatCard(
                    title = "Overtime Payout",
                    value = formatCurrency(totalOvertimePaySum),
                    subtitle = "1.5x rate incentive",
                    icon = Icons.Filled.Savings,
                    iconColor = NeonEmerald,
                    iconBgColor = Color(0x3310B981),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Primary Data Visualization: Grouped Bar Chart (Actual Hours vs Expected Standard Hours)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0x221E1B4B),
                borderColor = Color(0x4D818CF8)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Monthly Hours Breakdown per Employee",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Comparison of Logged Work Hours vs Expected 176h Quota",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF6366F1), Color(0xFF06B6D4))
                                        )
                                    )
                            )
                            Text(
                                text = "Actual Logged Hours",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0x4094A3B8))
                                    .border(1.dp, Color(0x8094A3B8), RoundedCornerShape(3.dp))
                            )
                            Text(
                                text = "Standard Expected (176h)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFFF59E0B))
                            )
                            Text(
                                text = "Overtime (>8h/shift)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFBBF24),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Jetpack Compose Canvas Interactive Grouped Bar Chart
                    val textMeasurer = rememberTextMeasurer()
                    val activeId = selectedEmployeeId

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .testTag("attendance_hours_bar_chart")
                            .pointerInput(analyticsList) {
                                detectTapGestures { offset ->
                                    val itemCount = analyticsList.size
                                    if (itemCount > 0) {
                                        val colWidth = size.width / itemCount
                                        val tappedIndex = (offset.x / colWidth).toInt().coerceIn(0, itemCount - 1)
                                        selectedEmployeeId = analyticsList[tappedIndex].employee.id
                                    }
                                }
                            }
                    ) {
                        val width = size.width
                        val height = size.height
                        val bottomY = height - 30.dp.toPx()
                        val topY = 20.dp.toPx()
                        val chartHeight = bottomY - topY
                        val maxScaleHours = 220f

                        // Draw background horizontal grid lines
                        val gridSteps = listOf(0, 50, 100, 150, 176, 200)
                        gridSteps.forEach { stepVal ->
                            val y = bottomY - ((stepVal / maxScaleHours) * chartHeight)
                            val isStandardQuota = stepVal == 176

                            drawLine(
                                color = if (isStandardQuota) Color(0x6638BDF8) else Color(0x1AFFFFFF),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = if (isStandardQuota) 1.5.dp.toPx() else 1.dp.toPx(),
                                pathEffect = if (isStandardQuota) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                            )

                            // Y-axis label
                            drawText(
                                textMeasurer = textMeasurer,
                                text = "${stepVal}h",
                                topLeft = Offset(4.dp.toPx(), y - 14.dp.toPx()),
                                style = TextStyle(
                                    color = if (isStandardQuota) Color(0xFF38BDF8) else Color(0x8094A3B8),
                                    fontSize = 9.sp,
                                    fontWeight = if (isStandardQuota) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }

                        // Draw Grouped Bars for Each Employee
                        val count = analyticsList.size
                        if (count > 0) {
                            val groupWidth = width / count
                            val barWidth = (groupWidth * 0.32f).coerceAtMost(22.dp.toPx())
                            val barSpacing = 4.dp.toPx()

                            analyticsList.forEachIndexed { index, item ->
                                val groupCenterX = (index * groupWidth) + (groupWidth / 2f)
                                val isSelected = item.employee.id == activeId

                                val expectedHeight = ((item.expectedStandardHours.toFloat() / maxScaleHours) * chartHeight).coerceAtLeast(4f)
                                val actualHeight = ((item.totalLoggedHours.toFloat() / maxScaleHours) * chartHeight).coerceAtLeast(4f)
                                val overtimePortionHeight = ((item.overtimeHours.toFloat() / maxScaleHours) * chartHeight)

                                val bar1Left = groupCenterX - barWidth - (barSpacing / 2f)
                                val bar2Left = groupCenterX + (barSpacing / 2f)

                                // If selected, draw luminous glow column background
                                if (isSelected) {
                                    drawRoundRect(
                                        color = Color(0x246366F1),
                                        topLeft = Offset(groupCenterX - (groupWidth * 0.45f), topY - 8.dp.toPx()),
                                        size = Size(groupWidth * 0.9f, chartHeight + 36.dp.toPx()),
                                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                                    )
                                    drawRoundRect(
                                        color = Color(0x66818CF8),
                                        topLeft = Offset(groupCenterX - (groupWidth * 0.45f), topY - 8.dp.toPx()),
                                        size = Size(groupWidth * 0.9f, chartHeight + 36.dp.toPx()),
                                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                                        style = Stroke(width = 1.dp.toPx())
                                    )
                                }

                                // Bar 1: Standard Expected Hours (Slate frosted)
                                drawRoundRect(
                                    color = if (isSelected) Color(0x6694A3B8) else Color(0x3394A3B8),
                                    topLeft = Offset(bar1Left, bottomY - expectedHeight),
                                    size = Size(barWidth, expectedHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )

                                // Bar 2: Actual Logged Hours (Vibrant Gradient)
                                val gradientBrush = if (isSelected) {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF38BDF8), Color(0xFF6366F1))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF818CF8), Color(0xFF4F46E5))
                                    )
                                }

                                drawRoundRect(
                                    brush = gradientBrush,
                                    topLeft = Offset(bar2Left, bottomY - actualHeight),
                                    size = Size(barWidth, actualHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )

                                // Highlight Overtime Segment on top of Bar 2
                                if (item.overtimeHours > 0) {
                                    drawRoundRect(
                                        color = Color(0xFFF59E0B),
                                        topLeft = Offset(bar2Left, bottomY - actualHeight),
                                        size = Size(barWidth, overtimePortionHeight),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                }

                                // Top Value Tag
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = "${item.totalLoggedHours.toInt()}h",
                                    topLeft = Offset(bar2Left - 6.dp.toPx(), bottomY - actualHeight - 14.dp.toPx()),
                                    style = TextStyle(
                                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                // Bottom Employee Initial / Code
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = item.employee.name.split(" ").firstOrNull() ?: item.employee.empCode,
                                    topLeft = Offset(groupCenterX - 14.dp.toPx(), bottomY + 8.dp.toPx()),
                                    style = TextStyle(
                                        color = if (isSelected) Color(0xFF818CF8) else Color(0xFF94A3B8),
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }

                    Text(
                        text = "💡 Tap on any bar or staff member below to inspect daily shift details and calculate overtime pay.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Selected Employee Deep-Dive Inspection Pane
        if (selectedEmployeeData != null) {
            val item = selectedEmployeeData
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0x281E1B4B),
                    borderColor = Color(0x80818CF8)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.employee.name.take(1),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Column {
                                    Text(
                                        text = item.employee.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${item.employee.empCode} • ${item.employee.designation} (${item.employee.department})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color(0x2EF59E0B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FBBF24))
                            ) {
                                Text(
                                    text = "+${item.overtimeHours}h OT Logged",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFBBF24),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Grid of Detailed Hours & Overtime Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x1A6366F1),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("ACTUAL LOGGED", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color(0xFF94A3B8))
                                    Text("${item.totalLoggedHours} hrs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Standard: 176h", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color(0xFF64748B))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x1AF59E0B),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("OVERTIME (>8H/DAY)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color(0xFFFBBF24))
                                    Text("${item.overtimeHours} hrs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                                    Text("1.5x multiplier", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color(0xFF94A3B8))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x2E10B981),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("OT EARNINGS", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color(0xFF34D399))
                                    Text(formatCurrencyDetailed(item.overtimePayEarned), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                                    Text("Added to base", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color(0xFF6EE7B7))
                                }
                            }
                        }

                        // Shift Performance Metrics
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x0DFFFFFF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Average Daily Shift Duration:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                                    Text(
                                        "${String.format("%.1f", if (item.presentDays > 0) item.totalLoggedHours / item.presentDays else 8.0)} hrs/day",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Attendance Quota Completion:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                                    Text(
                                        "${item.complianceRatePercent}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.complianceRatePercent >= 100) Color(0xFF34D399) else Color(0xFFFBBF24)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Attendance Days Breakdown:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                                    Text(
                                        "${item.presentDays} Present • ${item.lateDays} Late • ${item.leaveDays} Leaves",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }

                        // Button: Calculate Payroll with Automatic Overtime
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                    )
                                )
                                .border(1.dp, Color(0x80FFFFFF), RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.autoCalculatePayrollForEmployee(item.employee, selectedMonth)
                                }
                                .testTag("auto_calculate_payroll_button")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Calculate,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Auto-Calculate Payroll with Overtime",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Staff Overtime Ranking List
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0x1FFFFFFF),
                borderColor = Color(0x33FFFFFF)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Employee Attendance & Overtime Leaderboard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    analyticsList.sortedByDescending { it.overtimeHours }.forEach { empAnalytics ->
                        val isSelected = empAnalytics.employee.id == selectedEmployeeId
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0x336366F1) else Color(0x0DFFFFFF),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0x80818CF8) else Color(0x1AFFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedEmployeeId = empAnalytics.employee.id }
                                .testTag("analytics_row_${empAnalytics.employee.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
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
                                            .background(Color(0xFF4F46E5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = empAnalytics.employee.name.take(1),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = empAnalytics.employee.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${empAnalytics.employee.empCode} • ${empAnalytics.employee.department}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${empAnalytics.totalLoggedHours}h Logged",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (empAnalytics.overtimeHours > 0) "+${empAnalytics.overtimeHours}h OT (${formatCurrency(empAnalytics.overtimePayEarned)})" else "Standard shift",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (empAnalytics.overtimeHours > 0) Color(0xFF38BDF8) else Color(0xFF64748B)
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
