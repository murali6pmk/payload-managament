package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.UserRole
import com.example.ui.components.AttendanceStatusBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.SaaSStatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.CloudAttendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: CloudAttendViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentEmployee by viewModel.currentEmployee.collectAsState()
    val allAttendance by viewModel.attendanceRecords.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var showPunchDialog by remember { mutableStateOf(false) }
    var punchNote by remember { mutableStateOf("") }
    var punchLocation by remember { mutableStateOf("HQ Tech Park, San Francisco") }
    var selectedRecordForOverride by remember { mutableStateOf<AttendanceRecord?>(null) }

    // Filter attendance records
    val filteredAttendance = remember(allAttendance, searchQuery, selectedStatusFilter, currentRole, currentEmployee) {
        allAttendance.filter { record ->
            val matchesRole = if (currentRole == UserRole.ADMIN) true else record.employeeId == currentEmployee?.id
            val matchesSearch = searchQuery.isBlank() ||
                    record.employeeName.contains(searchQuery, ignoreCase = true) ||
                    record.empCode.contains(searchQuery, ignoreCase = true) ||
                    record.locationAddress.contains(searchQuery, ignoreCase = true)
            val matchesStatus = when (selectedStatusFilter) {
                "All" -> true
                "Present" -> record.status == AttendanceStatus.PRESENT
                "Late" -> record.status == AttendanceStatus.LATE
                "Half Day" -> record.status == AttendanceStatus.HALF_DAY
                "On Leave" -> record.status == AttendanceStatus.ON_LEAVE
                "Absent" -> record.status == AttendanceStatus.ABSENT
                else -> true
            }
            matchesRole && matchesSearch && matchesStatus
        }
    }

    val todayRecord = remember(allAttendance, currentEmployee, viewModel.todayDateString) {
        currentEmployee?.let { emp ->
            allAttendance.firstOrNull { it.employeeId == emp.id && it.date == viewModel.todayDateString }
        }
    }

    val isCheckedIn = todayRecord?.checkInTime != null
    val isCheckedOut = todayRecord?.checkOutTime != null

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Action & GPS Check-In Glass Panel
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
                                text = "Biometric & GPS Attendance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = viewModel.todayDisplayDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0x2E06B6D4),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6638BDF8))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "GPS ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }

                    // GPS Location Card Info
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x1AFFFFFF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x26FFFFFF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Place,
                                contentDescription = null,
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Location Verified",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "37.7749° N, 122.4194° W (HQ Campus)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "± 4m Accuracy",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF34D399),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Quick Punch Trigger
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                    )
                                )
                                .border(1.dp, Color(0x80FFFFFF), RoundedCornerShape(14.dp))
                                .clickable { showPunchDialog = true }
                                .testTag("open_punch_dialog_btn")
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
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (!isCheckedIn) "Punch In (Live)" else if (!isCheckedOut) "Punch Out (Live)" else "Punch Again",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        if (currentRole == UserRole.ADMIN) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0x26FFFFFF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x38FFFFFF)),
                                modifier = Modifier
                                    .height(46.dp)
                                    .clickable { viewModel.triggerSimulatedPeerEvent() }
                                    .testTag("admin_sim_peer_punch")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bolt,
                                        contentDescription = null,
                                        tint = Color(0xFFC084FC),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Simulate Peer",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Summary Stat Mini-Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SaaSStatCard(
                    title = "Total Logs",
                    value = "${filteredAttendance.size}",
                    icon = Icons.Filled.ListAlt,
                    iconColor = NeonIndigo,
                    iconBgColor = Color(0x334F46E5),
                    modifier = Modifier.weight(1f)
                )
                SaaSStatCard(
                    title = "Present",
                    value = "${filteredAttendance.count { it.status == AttendanceStatus.PRESENT }}",
                    icon = Icons.Filled.Check,
                    iconColor = NeonEmerald,
                    iconBgColor = Color(0x3310B981),
                    modifier = Modifier.weight(1f)
                )
                SaaSStatCard(
                    title = "Late/HD",
                    value = "${filteredAttendance.count { it.status == AttendanceStatus.LATE || it.status == AttendanceStatus.HALF_DAY }}",
                    icon = Icons.Filled.Schedule,
                    iconColor = NeonAmber,
                    iconBgColor = Color(0x33F59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search & Filter Glass Box
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, code, or location...", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFF818CF8))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = Color.White)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedContainerColor = Color(0x1AFFFFFF),
                            unfocusedContainerColor = Color(0x14FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attendance_search_input")
                    )

                    // Status Filter Chips
                    val statusChips = listOf("All", "Present", "Late", "Half Day", "On Leave", "Absent")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(statusChips) { statusName ->
                            val isSelected = selectedStatusFilter == statusName
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (isSelected) Color(0x336366F1) else Color(0x1AFFFFFF),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0x80818CF8) else Color(0x26FFFFFF)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable { selectedStatusFilter = statusName }
                                    .testTag("filter_chip_$statusName")
                            ) {
                                Text(
                                    text = statusName,
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

        // Attendance Record List
        if (filteredAttendance.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EventBusy,
                            contentDescription = null,
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No attendance records found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Try adjusting your search criteria or punch in a record.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredAttendance, key = { it.id }) { record ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0x1FFFFFFF),
                    borderColor = Color(0x33FFFFFF)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x336366F1))
                                        .border(1.dp, Color(0x4D818CF8), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = record.employeeName.take(1),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Column {
                                    Text(
                                        text = record.employeeName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${record.empCode} • ${record.date}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            AttendanceStatusBadge(status = record.status)
                        }

                        // Punch times & Location
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0x14FFFFFF),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "CHECK IN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = record.checkInTime ?: "--:--",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (record.checkInTime != null) Color(0xFF34D399) else Color(0xFF64748B)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0x14FFFFFF),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "CHECK OUT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = record.checkOutTime ?: "--:--",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (record.checkOutTime != null) Color(0xFF38BDF8) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        // Location chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Place,
                                    contentDescription = null,
                                    tint = Color(0xFF818CF8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = record.locationAddress,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp
                                )
                            }

                            if (currentRole == UserRole.ADMIN) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x266366F1),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4D818CF8)),
                                    modifier = Modifier
                                        .clickable { selectedRecordForOverride = record }
                                        .testTag("override_status_btn_${record.id}")
                                ) {
                                    Text(
                                        text = "Override Status",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFFE0E7FF),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Punch In / Out Form
    if (showPunchDialog) {
        AlertDialog(
            onDismissRequest = { showPunchDialog = false },
            title = {
                Text(
                    text = if (!isCheckedIn) "Live Attendance Punch In" else "Live Attendance Punch Out",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Your real-time biometric timestamp and GPS coordinates will be verified and broadcast via WebSocket to the Admin dashboard.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )

                    OutlinedTextField(
                        value = punchLocation,
                        onValueChange = { punchLocation = it },
                        label = { Text("Verified GPS Location", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = punchNote,
                        onValueChange = { punchNote = it },
                        label = { Text("Note / Project reference (Optional)", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isCheckedIn) {
                            viewModel.checkIn(punchLocation, punchNote)
                        } else {
                            viewModel.checkOut()
                        }
                        showPunchDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Confirm Punch", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPunchDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E1B4B)
        )
    }

    // Modal: Admin Status Override
    if (selectedRecordForOverride != null) {
        val target = selectedRecordForOverride!!
        AlertDialog(
            onDismissRequest = { selectedRecordForOverride = null },
            title = {
                Text(
                    text = "Override Status for ${target.employeeName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Select new attendance status. Changes update immediately and trigger a real-time WebSocket event.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )

                    AttendanceStatus.entries.forEach { statusOption ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (target.status == statusOption) Color(0x336366F1) else Color(0x1AFFFFFF),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (target.status == statusOption) Color(0xFF818CF8) else Color(0x26FFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.overrideAttendanceStatus(
                                        target.id,
                                        statusOption,
                                        target.employeeName,
                                        target.empCode
                                    )
                                    selectedRecordForOverride = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = statusOption.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                AttendanceStatusBadge(status = statusOption)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedRecordForOverride = null }) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E1B4B)
        )
    }
}
