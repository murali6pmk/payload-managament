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
import com.example.data.model.PayrollRecord
import com.example.data.model.PayrollStatus
import com.example.data.model.UserRole
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CloudAttendViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollScreen(
    viewModel: CloudAttendViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentEmployee by viewModel.currentEmployee.collectAsState()
    val allPayrolls by viewModel.payrollRecords.collectAsState()
    val employees by viewModel.employees.collectAsState()

    var selectedMonth by remember { mutableStateOf("August 2026") }
    val monthsList = listOf("August 2026", "September 2026", "July 2026", "June 2026")

    val filteredPayrolls = remember(allPayrolls, selectedMonth, currentRole, currentEmployee) {
        allPayrolls.filter { p ->
            val matchesRole = if (currentRole == UserRole.ADMIN) true else p.employeeId == currentEmployee?.id
            val matchesMonth = p.monthYear == selectedMonth
            matchesRole && matchesMonth
        }
    }

    val totalDisbursed = remember(filteredPayrolls) {
        filteredPayrolls.sumOf { it.netSalary }
    }

    val totalDeductions = remember(filteredPayrolls) {
        filteredPayrolls.sumOf { it.totalDeductions }
    }

    val totalOvertimePay = remember(filteredPayrolls) {
        filteredPayrolls.sumOf { it.overtimePay }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector & Bulk Generation Glass Action Bar
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
                                text = "Automated Payroll Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Prorated attendance, tax & overtime calculations",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        if (currentRole == UserRole.ADMIN) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0x26FFFFFF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x38FFFFFF)),
                                    modifier = Modifier
                                        .clickable { viewModel.navigateTo(com.example.ui.viewmodel.AppScreen.ANALYTICS) }
                                        .testTag("payroll_to_analytics_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.BarChart,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Analytics",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                            )
                                        )
                                        .border(1.dp, Color(0x80FFFFFF), RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.generateBulkPayroll(selectedMonth, 2026, 8)
                                        }
                                        .testTag("run_bulk_payroll_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Bolt,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Compute All",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Month Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(monthsList) { monthName ->
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

        // Employee Payroll Summary Card (Shown prominently for Employee, or when viewing personal payroll)
        if (currentRole == UserRole.EMPLOYEE && currentEmployee != null) {
            val emp = currentEmployee!!
            val matchingPayroll = filteredPayrolls.firstOrNull { it.employeeId == emp.id }
            val grossEstimate = matchingPayroll?.grossEarnings ?: emp.grossSalaryMonthly
            val deductionsEstimate = matchingPayroll?.totalDeductions ?: (emp.baseSalary * (emp.taxRatePercent + emp.pfRatePercent) / 100.0 + emp.insuranceDeduction)
            val netEstimate = matchingPayroll?.netSalary ?: (grossEstimate - deductionsEstimate).coerceAtLeast(0.0)

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0x331E1B4B),
                    borderColor = Color(0x80818CF8)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title & Status
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
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "My Payroll Summary",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "$selectedMonth • ${emp.designation}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFCBD5E1)
                                    )
                                }
                            }

                            if (matchingPayroll != null) {
                                PayrollStatusBadge(status = matchingPayroll.status)
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x3338BDF8),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x8038BDF8))
                                ) {
                                    Text(
                                        text = "READY TO GENERATE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Compensation Breakdown Matrix
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0x1AFFFFFF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "GROSS EARNINGS",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = formatCurrencyDetailed(grossEstimate),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "DEDUCTIONS",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = "-${formatCurrencyDetailed(deductionsEstimate)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFB7185)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "NET TAKE-HOME",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = formatCurrencyDetailed(netEstimate),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF34D399)
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0x1FFFFFFF))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Base ₹${String.format(Locale.getDefault(), "%,.0f", emp.baseSalary)} + HRA ₹${String.format(Locale.getDefault(), "%,.0f", emp.hra)} + Allowances ₹${String.format(Locale.getDefault(), "%,.0f", emp.specialAllowance + emp.transportAllowance)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "TDS & PF Withheld",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Prominent Generate Payslip Button
                        Button(
                            onClick = {
                                viewModel.generateAndOpenPayslipForEmployee(emp, selectedMonth)
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4F46E5)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_payslip_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ReceiptLong,
                                contentDescription = "Generate Payslip",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate Payslip • $selectedMonth",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Summary Metric Glass Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SaaSStatCard(
                    title = "Net Disbursed",
                    value = formatCurrency(totalDisbursed),
                    subtitle = "$selectedMonth payroll",
                    icon = Icons.Filled.AccountBalance,
                    iconColor = NeonEmerald,
                    iconBgColor = Color(0x3310B981),
                    modifier = Modifier.weight(1f)
                )
                SaaSStatCard(
                    title = "Tax & Deductions",
                    value = formatCurrency(totalDeductions),
                    subtitle = "Withheld amount",
                    icon = Icons.Filled.ReceiptLong,
                    iconColor = NeonRose,
                    iconBgColor = Color(0x33F43F5E),
                    modifier = Modifier.weight(1f)
                )
                SaaSStatCard(
                    title = "Overtime Paid",
                    value = formatCurrency(totalOvertimePay),
                    subtitle = "Extra incentive",
                    icon = Icons.Filled.MoreTime,
                    iconColor = NeonCyan,
                    iconBgColor = Color(0x3306B6D4),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Payslip Records List
        if (filteredPayrolls.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x286366F1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = null,
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "No payslips calculated for $selectedMonth",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (currentRole == UserRole.ADMIN) {
                                "Run automated calculation engine to process payslips for all active employees."
                            } else {
                                "Tap 'Generate Payslip' to calculate and produce your official printable pay advice."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF94A3B8)
                        )

                        Button(
                            onClick = {
                                if (currentRole == UserRole.ADMIN) {
                                    viewModel.generateBulkPayroll(selectedMonth, 2026, 8)
                                } else {
                                    currentEmployee?.let {
                                        viewModel.generateAndOpenPayslipForEmployee(it, selectedMonth)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                            modifier = Modifier.testTag("empty_generate_payslip_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentRole == UserRole.ADMIN) "Compute All Payslips" else "Generate My Payslip",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            items(filteredPayrolls, key = { it.id }) { payroll ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0x1FFFFFFF),
                    borderColor = Color(0x33FFFFFF),
                    onClick = { viewModel.viewPayslipDetail(payroll) }
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
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF4F46E5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = payroll.employeeName.take(1),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Column {
                                    Text(
                                        text = payroll.employeeName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${payroll.empCode} • ${payroll.department}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            PayrollStatusBadge(status = payroll.status)
                        }

                        // Salary Breakdown Grid
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x14FFFFFF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "ATTENDANCE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = "${payroll.presentDays} / ${payroll.totalWorkingDays} Days",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }

                                    if (payroll.overtimeHours > 0.0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0x33F59E0B),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FBBF24))
                                        ) {
                                            Text(
                                                text = "+${String.format("%.1f", payroll.overtimeHours)}h OT (+${formatCurrency(payroll.overtimePay)})",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFBBF24),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "NET TAKE-HOME",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = formatCurrencyDetailed(payroll.netSalary),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF34D399)
                                        )
                                    }
                                }
                            }
                        }

                        // Footer Action Pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gross: ${formatCurrency(payroll.grossEarnings)} | Deductions: -${formatCurrency(payroll.totalDeductions)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (currentRole == UserRole.ADMIN && payroll.status == PayrollStatus.PENDING) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0x3310B981),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x8034D399)),
                                        modifier = Modifier
                                            .clickable { viewModel.paySalary(payroll.id) }
                                            .testTag("pay_salary_${payroll.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF6EE7B7),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Pay Salary",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF6EE7B7)
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x334F46E5),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x80818CF8)),
                                    modifier = Modifier
                                        .clickable { viewModel.viewPayslipDetail(payroll) }
                                        .testTag("generate_payslip_${payroll.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ReceiptLong,
                                            contentDescription = null,
                                            tint = Color(0xFF818CF8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Printable Payslip",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF818CF8)
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.ChevronRight,
                                            contentDescription = null,
                                            tint = Color(0xFF818CF8),
                                            modifier = Modifier.size(14.dp)
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
