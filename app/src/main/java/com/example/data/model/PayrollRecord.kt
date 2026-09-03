package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PayrollStatus {
    PAID,
    PENDING,
    PROCESSING
}

@Entity(tableName = "payroll_records")
data class PayrollRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val empCode: String,
    val department: String,
    val designation: String,
    val monthYear: String, // e.g. "August 2026"
    val year: Int = 2026,
    val month: Int = 8,
    val totalWorkingDays: Int = 22,
    val presentDays: Int = 21,
    val paidLeaves: Int = 1,
    val unpaidLeaves: Int = 0,
    val overtimeHours: Double = 4.0,
    val baseSalary: Double,
    val hra: Double,
    val specialAllowance: Double,
    val transportAllowance: Double,
    val overtimePay: Double,
    val grossEarnings: Double,
    val unpaidLeaveDeduction: Double = 0.0,
    val taxDeduction: Double,
    val pfDeduction: Double,
    val insuranceDeduction: Double,
    val totalDeductions: Double,
    val netSalary: Double,
    val status: PayrollStatus = PayrollStatus.PAID,
    val paymentDate: String = "2026-08-31",
    val paymentMethod: String = "Direct Bank Deposit",
    val generatedDate: String = "2026-08-31"
)
