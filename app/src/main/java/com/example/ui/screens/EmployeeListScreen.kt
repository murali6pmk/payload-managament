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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.Employee
import com.example.data.model.EmployeeStatus
import com.example.data.model.UserRole
import com.example.ui.components.EditEmployeeDialog
import com.example.ui.components.GlassCard
import com.example.ui.components.SaaSStatCard
import com.example.ui.components.formatCurrency
import com.example.ui.theme.*
import com.example.ui.viewmodel.CloudAttendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(
    viewModel: CloudAttendViewModel,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.employees.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf("All") }
    var showAddModal by remember { mutableStateOf(false) }
    var employeeForManualMark by remember { mutableStateOf<Employee?>(null) }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }

    // Add Form State
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.EMPLOYEE) }
    var department by remember { mutableStateOf("Engineering") }
    var designation by remember { mutableStateOf("Software Engineer") }
    var phone by remember { mutableStateOf("+1 (555) 123-4567") }
    var baseSalaryText by remember { mutableStateOf("6000") }
    var hraText by remember { mutableStateOf("1500") }
    var specialText by remember { mutableStateOf("800") }
    var transportText by remember { mutableStateOf("300") }

    val departments = listOf("All", "Engineering", "Human Resources", "Product Design", "Marketing & Sales", "Finance & Legal", "Customer Success")

    val filteredEmployees = remember(employees, searchQuery, selectedDept) {
        employees.filter { emp ->
            val matchesSearch = searchQuery.isBlank() ||
                    emp.name.contains(searchQuery, ignoreCase = true) ||
                    emp.empCode.contains(searchQuery, ignoreCase = true) ||
                    emp.email.contains(searchQuery, ignoreCase = true) ||
                    emp.designation.contains(searchQuery, ignoreCase = true)
            val matchesDept = if (selectedDept == "All") true else emp.department == selectedDept
            matchesSearch && matchesDept
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header with Add Employee Button
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
                            text = "Staff & Employee Directory",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${employees.size} active personnel • Live sync enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }

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
                            .clickable { showAddModal = true }
                            .testTag("add_employee_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonAdd,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add Staff",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
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
                    title = "Active Staff",
                    value = "${employees.count { it.status == EmployeeStatus.ACTIVE }}",
                    icon = Icons.Filled.Badge,
                    iconColor = NeonIndigo,
                    iconBgColor = Color(0x334F46E5),
                    modifier = Modifier.weight(1f)
                )
                SaaSStatCard(
                    title = "Engineering",
                    value = "${employees.count { it.department.contains("Eng", ignoreCase = true) }}",
                    icon = Icons.Filled.Code,
                    iconColor = NeonCyan,
                    iconBgColor = Color(0x3306B6D4),
                    modifier = Modifier.weight(1f)
                )
                SaaSStatCard(
                    title = "HR & Admin",
                    value = "${employees.count { it.role == UserRole.ADMIN }}",
                    icon = Icons.Filled.Shield,
                    iconColor = NeonEmerald,
                    iconBgColor = Color(0x3310B981),
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
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, code, designation...", color = Color(0xFF94A3B8)) },
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
                            .testTag("employee_search_input")
                    )

                    // Department Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(departments) { deptName ->
                            val isSelected = selectedDept == deptName
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (isSelected) Color(0x336366F1) else Color(0x1AFFFFFF),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0x80818CF8) else Color(0x26FFFFFF)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable { selectedDept = deptName }
                                    .testTag("dept_chip_$deptName")
                            ) {
                                Text(
                                    text = deptName,
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

        // Employee Cards List
        if (filteredEmployees.isEmpty()) {
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
                            imageVector = Icons.Filled.PersonOff,
                            contentDescription = null,
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No employees match your search",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            items(filteredEmployees, key = { it.id }) { emp ->
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
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4F46E5))
                                        .border(1.5.dp, Color(0x80818CF8), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emp.name.take(1),
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
                                    Text(
                                        text = "${emp.empCode} • ${emp.designation}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (emp.role == UserRole.ADMIN) Color(0x334F46E5) else Color(0x3310B981),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (emp.role == UserRole.ADMIN) Color(0x80818CF8) else Color(0x8034D399)
                                )
                            ) {
                                Text(
                                    text = if (emp.role == UserRole.ADMIN) "ADMIN" else "STAFF",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (emp.role == UserRole.ADMIN) Color(0xFFE0E7FF) else Color(0xFF6EE7B7),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Info Details Grid
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x14FFFFFF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Department:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    Text(emp.department, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Email:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    Text(emp.email, style = MaterialTheme.typography.labelSmall, color = Color(0xFFCBD5E1))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Base Salary:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    Text(
                                        "${formatCurrency(emp.grossSalaryMonthly)}/mo",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF818CF8)
                                    )
                                }
                            }
                        }

                        // Action Buttons: Edit, Remove, Mark Attendance, or Switch User
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // HR Edit Employee Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x336366F1),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66818CF8)),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable { employeeToEdit = emp }
                                    .testTag("edit_employee_${emp.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit Employee",
                                        tint = Color(0xFFC7D2FE),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Edit",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFFC7D2FE)
                                    )
                                }
                            }

                            // HR Remove Staff Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x2EF43F5E),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FB7185)),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable { employeeToDelete = emp }
                                    .testTag("remove_employee_${emp.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PersonRemove,
                                        contentDescription = null,
                                        tint = Color(0xFFFB7185),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Remove",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFFFB7185)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x26FFFFFF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x38FFFFFF)),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable { employeeForManualMark = emp }
                                    .testTag("mark_attendance_${emp.id}")
                            ) {
                                Text(
                                    text = "Mark Attendance",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFFE0E7FF),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x334F46E5),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66818CF8)),
                                modifier = Modifier
                                    .clickable { viewModel.setCurrentEmployee(emp) }
                                    .testTag("login_as_${emp.id}")
                            ) {
                                Text(
                                    text = "Switch User",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFF818CF8),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Manual Attendance Mark for Selected Employee
    if (employeeForManualMark != null) {
        val target = employeeForManualMark!!
        AlertDialog(
            onDismissRequest = { employeeForManualMark = null },
            title = {
                Text(
                    text = "Manual Attendance: ${target.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Mark today's attendance for this employee. A real-time WebSocket update will be broadcast.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )

                    AttendanceStatus.entries.forEach { statusOption ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x1AFFFFFF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x26FFFFFF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.adminMarkAttendance(target, statusOption)
                                    employeeForManualMark = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Mark as ${statusOption.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF818CF8))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { employeeForManualMark = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E1B4B)
        )
    }

    // Modal: Add Employee Form
    if (showAddModal) {
        AlertDialog(
            onDismissRequest = { showAddModal = false },
            title = {
                Text(
                    text = "Add New Staff Member",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name", color = Color(0xFF94A3B8)) },
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
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address", color = Color(0xFF94A3B8)) },
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
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Designation / Title", color = Color(0xFF94A3B8)) },
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
                        value = baseSalaryText,
                        onValueChange = { baseSalaryText = it },
                        label = { Text("Base Monthly Salary ($)", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val base = baseSalaryText.toDoubleOrNull() ?: 5000.0
                        val hra = hraText.toDoubleOrNull() ?: 1200.0
                        val special = specialText.toDoubleOrNull() ?: 600.0
                        val trans = transportText.toDoubleOrNull() ?: 200.0
                        viewModel.addEmployee(
                            name, email, role, department, designation, phone,
                            base, hra, special, trans
                        )
                        showAddModal = false
                        name = ""
                        email = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Save Staff Member", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddModal = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E1B4B)
        )
    }

    // Modal: Confirmation Dialog for Removing Employee from Company
    if (employeeToDelete != null) {
        val target = employeeToDelete!!
        AlertDialog(
            onDismissRequest = { employeeToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFB7185),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Remove Employee from Company?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Are you sure you want to remove ${target.name} (${target.empCode}) from the organization?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "Department: ${target.department}\nDesignation: ${target.designation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "This action will permanently delete this employee's record from the Room database and broadcast a termination event via the real-time stream.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFB7185)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEmployee(target.id)
                        employeeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    modifier = Modifier.testTag("confirm_remove_employee_btn")
                ) {
                    Text("Confirm Remove", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { employeeToDelete = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E1B4B)
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
