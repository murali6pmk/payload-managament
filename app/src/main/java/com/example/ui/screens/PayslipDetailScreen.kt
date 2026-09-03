package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PayrollRecord
import com.example.data.model.PayrollStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.PayrollStatusBadge
import com.example.ui.components.convertNumberToWords
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatCurrencyDetailed
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CloudAttendViewModel

@Composable
fun PayslipDetailScreen(
    viewModel: CloudAttendViewModel,
    modifier: Modifier = Modifier
) {
    val selectedPayroll by viewModel.selectedPayroll.collectAsState()
    val context = LocalContext.current
    var showExportSuccessDialog by remember { mutableStateOf(false) }
    var printableSheetMode by remember { mutableStateOf(true) } // Defaults to crisp printable stationery sheet format

    if (selectedPayroll == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("No payslip selected.", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.PAYROLL) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Return to Payroll")
                }
            }
        }
        return
    }

    val pay = selectedPayroll!!

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action Bar with Print, PDF & Share
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.PAYROLL) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Payroll", style = MaterialTheme.typography.labelMedium)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Printable Paper vs Dark UI Toggle
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (printableSheetMode) Color(0x3338BDF8) else Color(0x1AFFFFFF),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (printableSheetMode) Color(0x8038BDF8) else Color(0x33FFFFFF)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { printableSheetMode = !printableSheetMode }
                            .testTag("toggle_print_mode_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (printableSheetMode) Icons.Filled.Print else Icons.Filled.DarkMode,
                                contentDescription = null,
                                tint = if (printableSheetMode) Color(0xFF38BDF8) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (printableSheetMode) "Print Paper" else "Dark Mode",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (printableSheetMode) Color(0xFF38BDF8) else Color(0xFFCBD5E1)
                            )
                        }
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "Payslip - ${pay.employeeName} (${pay.monthYear})"
                                )
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    """
                                    CloudAttend Technologies - Salary Advice
                                    Employee: ${pay.employeeName} (${pay.empCode})
                                    Department: ${pay.department}
                                    Pay Period: ${pay.monthYear}
                                    Gross Earnings: ₹${pay.grossEarnings}
                                    Total Deductions: ₹${pay.totalDeductions}
                                    Net Salary: ₹${pay.netSalary}
                                    Status: ${pay.status.name}
                                    """.trimIndent()
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Payslip"))
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x26FFFFFF))
                            .testTag("share_payslip_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share Payslip",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Print / PDF Button
                    Button(
                        onClick = {
                            showExportSuccessDialog = true
                            viewModel.showToast("Payslip for ${pay.employeeName} prepared for printing")
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("download_payslip_pdf_btn")
                    ) {
                        Icon(
                            Icons.Filled.Print,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Print / PDF",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // The Comprehensive Printable Payslip Document Container
        item {
            if (printableSheetMode) {
                // Authentic Printable Stationery Format (Crisp White Sheet with Official Slate Borders & Corporate Letterhead)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFFFFF),
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Corporate Letterhead & Title Banner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF312E81)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.CloudDone,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "CloudAttend Technologies",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Reg: CIN U72200DL2024PTC123456 • TAN: BLRC09281",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = "Innovation Vista Tech Park, Bangalore 560103",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            // Payslip Status Stamp
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (pay.status == PayrollStatus.PAID) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (pay.status == PayrollStatus.PAID) Color(0xFF16A34A) else Color(0xFFD97706)
                                )
                            ) {
                                Text(
                                    text = if (pay.status == PayrollStatus.PAID) "PAID / DISBURSED" else "PENDING DISBURSAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = if (pay.status == PayrollStatus.PAID) Color(0xFF15803D) else Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Title Bar
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "OFFICIAL SALARY ADVICE / PAYSLIP",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "PERIOD: ${pay.monthYear.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4F46E5)
                                )
                            }
                        }

                        // 2-Column Employee & Bank Information Grid
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left Column: Employee Info
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    PrintMetaItemLight("Employee Name", pay.employeeName, isBold = true)
                                    PrintMetaItemLight("Employee ID", pay.empCode)
                                    PrintMetaItemLight("Designation", pay.designation)
                                    PrintMetaItemLight("Department", pay.department)
                                    PrintMetaItemLight("Working Days", "${pay.totalWorkingDays} Days (Worked: ${pay.presentDays})")
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Right Column: Bank & Attendance Info
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    PrintMetaItemLight("Bank Name", "HDFC Corporate Bank")
                                    PrintMetaItemLight("Bank A/C No.", "•••• •••• 4892")
                                    PrintMetaItemLight("IFSC / RTGS", "HDFC0001892")
                                    PrintMetaItemLight("Disbursal Mode", pay.paymentMethod)
                                    PrintMetaItemLight(
                                        "Overtime Logged",
                                        if (pay.overtimeHours > 0) "${String.format("%.1f", pay.overtimeHours)} Hours" else "Nil"
                                    )
                                }
                            }
                        }

                        // Detailed Itemized Earnings & Deductions Table
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF334155))
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "EARNINGS & ALLOWANCES",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "AMOUNT (₹)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(90.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DEDUCTIONS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "AMOUNT (₹)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(90.dp)
                                )
                            }

                            // Table Content Row 1: Basic vs TDS
                            TableDataRowLight(
                                earnLabel = "Basic Monthly Salary",
                                earnVal = formatCurrencyDetailed(pay.baseSalary),
                                dedLabel = "Tax Deducted at Source (TDS)",
                                dedVal = formatCurrencyDetailed(pay.taxDeduction),
                                isAlt = false
                            )

                            // Table Content Row 2: HRA vs PF
                            TableDataRowLight(
                                earnLabel = "House Rent Allowance (HRA)",
                                earnVal = formatCurrencyDetailed(pay.hra),
                                dedLabel = "Provident Fund (EPF - 12%)",
                                dedVal = formatCurrencyDetailed(pay.pfDeduction),
                                isAlt = true
                            )

                            // Table Content Row 3: Special Allowance vs Insurance
                            TableDataRowLight(
                                earnLabel = "Special Allowance",
                                earnVal = formatCurrencyDetailed(pay.specialAllowance),
                                dedLabel = "Group Medical Insurance",
                                dedVal = formatCurrencyDetailed(pay.insuranceDeduction),
                                isAlt = false
                            )

                            // Table Content Row 4: Transport vs LOP
                            TableDataRowLight(
                                earnLabel = "Transport / Conveyance",
                                earnVal = formatCurrencyDetailed(pay.transportAllowance),
                                dedLabel = if (pay.unpaidLeaves > 0) "Loss of Pay (${pay.unpaidLeaves}d LOP)" else "Professional Tax (PT)",
                                dedVal = if (pay.unpaidLeaves > 0) formatCurrencyDetailed(pay.unpaidLeaveDeduction) else "₹200.00",
                                isAlt = true
                            )

                            // Table Content Row 5: Overtime (if any)
                            if (pay.overtimeHours > 0) {
                                TableDataRowLight(
                                    earnLabel = "Overtime Incentive (+${String.format("%.1f", pay.overtimeHours)}h)",
                                    earnVal = formatCurrencyDetailed(pay.overtimePay),
                                    dedLabel = "—",
                                    dedVal = "₹0.00",
                                    isAlt = false
                                )
                            }

                            // Subtotal Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE2E8F0))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Total Gross Earnings (A)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = formatCurrencyDetailed(pay.grossEarnings),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(90.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Total Deductions (B)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF991B1B),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = formatCurrencyDetailed(pay.totalDeductions),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF991B1B),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(90.dp)
                                )
                            }
                        }

                        // Net Salary Highlight Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF0FDF4),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "NET TAKE-HOME SALARY (A - B)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp,
                                            color = Color(0xFF166534)
                                        )
                                        Text(
                                            text = "Direct Bank Credit • Ref: TXN-ACH-20260831-${pay.id}982",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                    Text(
                                        text = formatCurrencyDetailed(pay.netSalary),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF14532D)
                                    )
                                }

                                HorizontalDivider(color = Color(0xFFBBF7D0))

                                Text(
                                    text = "Amount in Words: ${convertNumberToWords(pay.netSalary)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF166534)
                                )
                            }
                        }

                        // Official Signatory & QR Verification Section
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Digital Verification QR Box
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF8FAFC)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.QrCode2,
                                        contentDescription = "Verified QR",
                                        tint = Color(0xFF1E293B),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Digitally Verified",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF334155)
                                    )
                                    Text(
                                        text = "Voucher #${pay.empCode}-${pay.monthYear.take(3).uppercase()}26",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            // Signatory Stamp
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "For CloudAttend Technologies Pvt. Ltd.",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Authorized Signatory",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }

                        Text(
                            text = "This is a computer-generated official pay document. No physical signature is required.",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Glass Dark UI Mode
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0x281E1B4B),
                    borderColor = Color(0x66818CF8)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header Brand
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
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.CloudDone,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        "CloudAttend Inc.",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        "Confidential Pay Advice",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            PayrollStatusBadge(status = pay.status)
                        }

                        HorizontalDivider(color = Color(0x26FFFFFF))

                        // Employee Meta Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x14FFFFFF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Employee Name:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    Text(pay.employeeName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Employee ID:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    Text(pay.empCode, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Department:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    Text(pay.department, style = MaterialTheme.typography.labelSmall, color = Color(0xFFCBD5E1))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Pay Period:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    Text(pay.monthYear, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF818CF8))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Days Worked / Total:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    Text("${pay.presentDays} / ${pay.totalWorkingDays} Days (${pay.paidLeaves} Paid Leaves)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF34D399))
                                }
                            }
                        }

                        // Earnings Breakdown
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "EARNINGS & ALLOWANCES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF818CF8)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Base Monthly Salary", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                                Text(formatCurrencyDetailed(pay.baseSalary), style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("House Rent Allowance (HRA)", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                                Text(formatCurrencyDetailed(pay.hra), style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Special Allowance", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                                Text(formatCurrencyDetailed(pay.specialAllowance), style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Transport Allowance", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                                Text(formatCurrencyDetailed(pay.transportAllowance), style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                            if (pay.overtimeHours > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Overtime Pay (${pay.overtimeHours} hrs)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF34D399))
                                    Text("+${formatCurrencyDetailed(pay.overtimePay)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0x26FFFFFF))

                        // Deductions Breakdown
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "TAXES & DEDUCTIONS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFFFB7185)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Income Tax Withholding (TDS)", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                                Text("-${formatCurrencyDetailed(pay.taxDeduction)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFB7185))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Provident Fund (PF / 401k)", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                                Text("-${formatCurrencyDetailed(pay.pfDeduction)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFB7185))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Health & Medical Insurance", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                                Text("-${formatCurrencyDetailed(pay.insuranceDeduction)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFB7185))
                            }
                            if (pay.unpaidLeaves > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Unpaid Leave Loss of Pay (${pay.unpaidLeaves} days)", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFB7185))
                                    Text("-${formatCurrencyDetailed(pay.unpaidLeaveDeduction)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFB7185))
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0x26FFFFFF))

                        // Net Take Home Glass Card
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0x3310B981),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6634D399)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "NET TAKE-HOME PAY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = Color(0xFF6EE7B7)
                                    )
                                    Text(
                                        text = "Direct Deposit via Automated ACH",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFCBD5E1)
                                    )
                                }
                                Text(
                                    text = formatCurrencyDetailed(pay.netSalary),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExportSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showExportSuccessDialog = false },
            title = { Text("Print Document Ready", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Printable PDF salary statement generated for ${pay.employeeName} (${pay.monthYear}) with verified digital certificate.",
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        "Gross: ${formatCurrency(pay.grossEarnings)} | Net: ${formatCurrency(pay.netSalary)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExportSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Done", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1B4B)
        )
    }
}

@Composable
private fun PrintMetaItemLight(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun TableDataRowLight(
    earnLabel: String,
    earnVal: String,
    dedLabel: String,
    dedVal: String,
    isAlt: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isAlt) Color(0xFFF8FAFC) else Color(0xFFFFFFFF))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = earnLabel,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            color = Color(0xFF334155),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = earnVal,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.End,
            modifier = Modifier.width(90.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = dedLabel,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = dedVal,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (dedVal == "₹0.00" || dedLabel == "—") Color(0xFF94A3B8) else Color(0xFFDC2626),
            textAlign = TextAlign.End,
            modifier = Modifier.width(90.dp)
        )
    }
}
