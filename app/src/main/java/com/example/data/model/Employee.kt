package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,
    EMPLOYEE
}

enum class EmployeeStatus {
    ACTIVE,
    ON_LEAVE,
    INACTIVE
}

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val empCode: String,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.EMPLOYEE,
    val department: String,
    val designation: String,
    val phone: String,
    val joiningDate: String,
    val baseSalary: Double,
    val hra: Double, // House Rent Allowance
    val specialAllowance: Double,
    val transportAllowance: Double,
    val taxRatePercent: Double = 10.0,
    val pfRatePercent: Double = 12.0,
    val insuranceDeduction: Double = 50.0,
    val bankAccount: String = "•••• 4829",
    val status: EmployeeStatus = EmployeeStatus.ACTIVE,
    val avatarColorHex: String = "#4F46E5"
) {
    val grossSalaryMonthly: Double
        get() = baseSalary + hra + specialAllowance + transportAllowance
}
