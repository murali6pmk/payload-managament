package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.LeaveRequest
import com.example.data.model.LeaveStatus
import com.example.data.model.LeaveType
import com.example.data.model.UserRole
import com.example.ui.components.GlassCard
import com.example.ui.components.LeaveStatusBadge
import com.example.ui.components.SaaSStatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.CloudAttendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveScreen(
    viewModel: CloudAttendViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentEmployee by viewModel.currentEmployee.collectAsState()
    val allLeaves by viewModel.leaveRequests.collectAsState()

    var showApplyModal by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("All") }

    // Apply Form State
    var selectedType by remember { mutableStateOf(LeaveType.CASUAL) }
    var startDate by remember { mutableStateOf("2026-09-07") }
    var endDate by remember { mutableStateOf("2026-09-08") }
    var daysCountText by remember { mutableStateOf("2") }
    var reasonText by remember { mutableStateOf("") }

    val filteredLeaves = remember(allLeaves, selectedTab, currentRole, currentEmployee) {
        allLeaves.filter { leave ->
            val matchesRole = if (currentRole == UserRole.ADMIN) true else leave.employeeId == currentEmployee?.id
            val matchesTab = when (selectedTab) {
                "Pending" -> leave.status == LeaveStatus.PENDING
                "Approved" -> leave.status == LeaveStatus.APPROVED
                "Rejected" -> leave.status == LeaveStatus.REJECTED
                else -> true
            }
            matchesRole && matchesTab
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Leave Balances Glass Cards (for employee) or Admin Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SaaSStatCard(
                    title = "Casual Leave",
                    value = "8 / 10",
                    subtitle = "2 days used",
                    icon = Icons.Filled.BeachAccess,
                    iconColor = NeonAmber,
                    iconBgColor = Color(0x33F59E0B),
                    modifier = Modifier.weight(1f)
                )
                SaaSStatCard(
                    title = "Sick Leave",
                    value = "5 / 6",
                    subtitle = "1 day used",
                    icon = Icons.Filled.LocalHospital,
                    iconColor = NeonRose,
                    iconBgColor = Color(0x33F43F5E),
                    modifier = Modifier.weight(1f)
                )
                SaaSStatCard(
                    title = "Earned Leave",
                    value = "12 / 15",
                    subtitle = "Paid balance",
                    icon = Icons.Filled.Stars,
                    iconColor = NeonEmerald,
                    iconBgColor = Color(0x3310B981),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Apply Button Glass Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0x281E1B4B),
                borderColor = Color(0x66818CF8)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentRole == UserRole.ADMIN) "Leave Approvals Hub" else "Apply for Time Off",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (currentRole == UserRole.ADMIN) "Review & approve employee requests instantly with real-time push." else "Submit leave requests with instant live status tracking.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                )
                            )
                            .border(1.dp, Color(0x80FFFFFF), RoundedCornerShape(14.dp))
                            .clickable { showApplyModal = true }
                            .testTag("apply_leave_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Apply Leave",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Filter Tabs
        item {
            val tabs = listOf("All", "Pending", "Approved", "Rejected")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tabs) { tabName ->
                    val isSelected = selectedTab == tabName
                    val count = when (tabName) {
                        "Pending" -> allLeaves.count { it.status == LeaveStatus.PENDING }
                        "Approved" -> allLeaves.count { it.status == LeaveStatus.APPROVED }
                        "Rejected" -> allLeaves.count { it.status == LeaveStatus.REJECTED }
                        else -> allLeaves.size
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (isSelected) Color(0x336366F1) else Color(0x1AFFFFFF),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0x80818CF8) else Color(0x26FFFFFF)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { selectedTab = tabName }
                            .testTag("leave_tab_$tabName")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = tabName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF818CF8) else Color(0xFFCBD5E1)
                            )
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (isSelected) Color(0x4D818CF8) else Color(0x26FFFFFF)
                            ) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Leave Requests List
        if (filteredLeaves.isEmpty()) {
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
                            imageVector = Icons.Filled.BeachAccess,
                            contentDescription = null,
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No leave applications in this view",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Submit a request or select another status filter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredLeaves, key = { it.id }) { leave ->
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
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x33818CF8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (leave.leaveType) {
                                            LeaveType.SICK -> Icons.Filled.LocalHospital
                                            LeaveType.CASUAL -> Icons.Filled.BeachAccess
                                            LeaveType.EARNED -> Icons.Filled.Stars
                                            LeaveType.UNPAID -> Icons.Filled.MoneyOff
                                        },
                                        contentDescription = null,
                                        tint = Color(0xFF818CF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = leave.employeeName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${leave.empCode} • ${leave.leaveType.name} LEAVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF818CF8),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            LeaveStatusBadge(status = leave.status)
                        }

                        // Dates & Duration
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x14FFFFFF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "DURATION",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = "${leave.startDate} to ${leave.endDate}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color(0x334F46E5),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66818CF8))
                                ) {
                                    Text(
                                        text = "${leave.daysCount} Day${if (leave.daysCount > 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE0E7FF),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // Reason Box
                        Text(
                            text = "\"${leave.reason}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0)
                        )

                        // Admin Action Buttons (Real-time Instant Decision)
                        if (currentRole == UserRole.ADMIN && leave.status == LeaveStatus.PENDING) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0x2EF43F5E),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FB7185)),
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clickable { viewModel.rejectLeave(leave.id) }
                                        .testTag("reject_leave_${leave.id}")
                                ) {
                                    Text(
                                        text = "Reject",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFB7185),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0x3310B981),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x8034D399)),
                                    modifier = Modifier
                                        .clickable { viewModel.approveLeave(leave.id) }
                                        .testTag("approve_leave_${leave.id}")
                                ) {
                                    Text(
                                        text = "Approve (Live Push)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6EE7B7),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Leave Application Dialog
    if (showApplyModal) {
        AlertDialog(
            onDismissRequest = { showApplyModal = false },
            title = {
                Text(
                    text = "Apply for Leave (Instant Stream)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Leave Type",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Leave type selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LeaveType.entries.forEach { type ->
                            val isSel = selectedType == type
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) Color(0x336366F1) else Color(0x1AFFFFFF),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) Color(0xFF818CF8) else Color(0x26FFFFFF)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedType = type }
                            ) {
                                Text(
                                    text = type.name.take(4),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color(0xFF818CF8) else Color(0xFFCBD5E1),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date", color = Color(0xFF94A3B8)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date", color = Color(0xFF94A3B8)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = daysCountText,
                        onValueChange = { daysCountText = it },
                        label = { Text("Total Days", color = Color(0xFF94A3B8)) },
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
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Reason for leave", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("e.g. Medical checkup, Family event", color = Color(0xFF64748B)) },
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
                        val days = daysCountText.toIntOrNull() ?: 1
                        viewModel.applyLeave(selectedType, startDate, endDate, days, reasonText)
                        showApplyModal = false
                        reasonText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Submit Live Request", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyModal = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E1B4B)
        )
    }
}
