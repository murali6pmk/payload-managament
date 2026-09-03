package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LeaveType {
    SICK,
    CASUAL,
    EARNED,
    UNPAID
}

enum class LeaveStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Entity(tableName = "leave_requests")
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val empCode: String,
    val leaveType: LeaveType,
    val startDate: String, // YYYY-MM-DD
    val endDate: String,   // YYYY-MM-DD
    val daysCount: Int,
    val reason: String,
    val status: LeaveStatus = LeaveStatus.PENDING,
    val appliedOn: String, // YYYY-MM-DD
    val reviewedBy: String? = null,
    val adminComments: String? = null
)
