package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Employee
import com.example.data.model.EmployeeStatus
import com.example.ui.theme.LocalThemeIsDark

@Composable
fun EditEmployeeDialog(
    employee: Employee,
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    val isDark = LocalThemeIsDark.current

    var name by remember { mutableStateOf(employee.name) }
    var phone by remember { mutableStateOf(employee.phone) }
    var designation by remember { mutableStateOf(employee.designation) }
    var department by remember { mutableStateOf(employee.department) }
    var baseSalaryText by remember { mutableStateOf(employee.baseSalary.toInt().toString()) }
    var email by remember { mutableStateOf(employee.email) }
    var selectedStatus by remember { mutableStateOf(employee.status) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val departments = listOf(
        "Engineering", "Design", "Product", "HR & Admin", "Sales", "Operations", "Marketing", "Finance"
    )

    // Calculate dynamic allowances and gross preview based on edited base salary
    val parsedBaseSalary = baseSalaryText.toDoubleOrNull() ?: 0.0
    val dynamicHra = parsedBaseSalary * 0.40 // 40% of base
    val dynamicSpecial = parsedBaseSalary * 0.20 // 20% of base
    val dynamicTransport = 3500.0
    val estimatedGross = parsedBaseSalary + dynamicHra + dynamicSpecial + dynamicTransport

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) Color(0xFF1E1B4B) else Color.White,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isDark) Color(0x66818CF8) else Color(0xFFC7D2FE)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("edit_employee_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Edit Employee Profile",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Text(
                                text = "${employee.empCode} • Update details & salary",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }

                // Error banner if any
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x33EF4444),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFF818CF8))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_employee_name_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFF818CF8),
                        unfocusedBorderColor = if (isDark) Color(0x40FFFFFF) else Color(0xFFCBD5E1),
                        focusedContainerColor = if (isDark) Color(0x26FFFFFF) else Color(0xFFF8FAFC),
                        unfocusedContainerColor = if (isDark) Color(0x14FFFFFF) else Color(0xFFF8FAFC)
                    ),
                    singleLine = true
                )

                // Phone Number
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = {
                        Icon(Icons.Filled.Phone, contentDescription = null, tint = Color(0xFF818CF8))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_employee_phone_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFF818CF8),
                        unfocusedBorderColor = if (isDark) Color(0x40FFFFFF) else Color(0xFFCBD5E1),
                        focusedContainerColor = if (isDark) Color(0x26FFFFFF) else Color(0xFFF8FAFC),
                        unfocusedContainerColor = if (isDark) Color(0x14FFFFFF) else Color(0xFFF8FAFC)
                    ),
                    singleLine = true
                )

                // Designation / Job Title
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Designation / Title") },
                    leadingIcon = {
                        Icon(Icons.Filled.Badge, contentDescription = null, tint = Color(0xFF818CF8))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_employee_designation_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFF818CF8),
                        unfocusedBorderColor = if (isDark) Color(0x40FFFFFF) else Color(0xFFCBD5E1),
                        focusedContainerColor = if (isDark) Color(0x26FFFFFF) else Color(0xFFF8FAFC),
                        unfocusedContainerColor = if (isDark) Color(0x14FFFFFF) else Color(0xFFF8FAFC)
                    ),
                    singleLine = true
                )

                // Base Salary in Rupees (₹)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = baseSalaryText,
                        onValueChange = { baseSalaryText = it },
                        label = { Text("Monthly Base Salary (₹)") },
                        leadingIcon = {
                            Text(
                                text = "₹",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_employee_salary_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                            focusedBorderColor = Color(0xFF34D399),
                            unfocusedBorderColor = if (isDark) Color(0x40FFFFFF) else Color(0xFFCBD5E1),
                            focusedContainerColor = if (isDark) Color(0x26FFFFFF) else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) Color(0x14FFFFFF) else Color(0xFFF8FAFC)
                        ),
                        singleLine = true
                    )

                    // Live Calculated Gross Preview Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0x2610B981) else Color(0xFFECFDF5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x8034D399)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Est. Gross Monthly Pay:",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF065F46),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatCurrencyDetailed(estimatedGross),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }

                // Department Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Department",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(departments) { dept ->
                            val isSelected = department == dept
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF6366F1) else (if (isDark) Color(0x1FFFFFFF) else Color(0xFFF1F5F9)),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFFA5B4FC) else (if (isDark) Color(0x2EFFFFFF) else Color(0xFFCBD5E1))
                                ),
                                modifier = Modifier
                                    .clickable { department = dept }
                                    .testTag("edit_dept_option_$dept")
                            ) {
                                Text(
                                    text = dept,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Work Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Work Email") },
                    leadingIcon = {
                        Icon(Icons.Filled.Email, contentDescription = null, tint = Color(0xFF818CF8))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_employee_email_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFF818CF8),
                        unfocusedBorderColor = if (isDark) Color(0x40FFFFFF) else Color(0xFFCBD5E1),
                        focusedContainerColor = if (isDark) Color(0x26FFFFFF) else Color(0xFFF8FAFC),
                        unfocusedContainerColor = if (isDark) Color(0x14FFFFFF) else Color(0xFFF8FAFC)
                    ),
                    singleLine = true
                )

                // Employee Status
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Employment Status",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EmployeeStatus.entries.forEach { status ->
                            val isSelected = selectedStatus == status
                            val (chipBg, chipBorder, chipColor) = when (status) {
                                EmployeeStatus.ACTIVE -> Triple(Color(0x3310B981), Color(0xFF10B981), Color(0xFF34D399))
                                EmployeeStatus.ON_LEAVE -> Triple(Color(0x33F59E0B), Color(0xFFF59E0B), Color(0xFFFBBF24))
                                EmployeeStatus.INACTIVE -> Triple(Color(0x33EF4444), Color(0xFFEF4444), Color(0xFFFCA5A5))
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) chipBg else (if (isDark) Color(0x14FFFFFF) else Color(0xFFF8FAFC)),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) chipBorder else (if (isDark) Color(0x2EFFFFFF) else Color(0xFFCBD5E1))
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedStatus = status }
                            ) {
                                Text(
                                    text = status.name.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) (if (isDark) chipColor else Color(0xFF0F172A)) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Action Buttons (Cancel / Save Changes)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Cancel",
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Employee name cannot be empty."
                                return@Button
                            }
                            if (designation.isBlank()) {
                                errorMessage = "Designation cannot be empty."
                                return@Button
                            }
                            if (phone.isBlank()) {
                                errorMessage = "Phone number cannot be empty."
                                return@Button
                            }
                            val salary = baseSalaryText.toDoubleOrNull()
                            if (salary == null || salary <= 0) {
                                errorMessage = "Please enter a valid base salary amount."
                                return@Button
                            }

                            val updated = employee.copy(
                                name = name.trim(),
                                phone = phone.trim(),
                                designation = designation.trim(),
                                department = department,
                                baseSalary = salary,
                                hra = dynamicHra,
                                specialAllowance = dynamicSpecial,
                                transportAllowance = dynamicTransport,
                                email = email.trim(),
                                status = selectedStatus
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("save_employee_changes_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
