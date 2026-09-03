package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AttendanceStatus {
    PRESENT,
    LATE,
    HALF_DAY,
    ABSENT,
    ON_LEAVE
}

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val empCode: String,
    val date: String, // Format: YYYY-MM-DD
    val checkInTime: String? = null, // e.g. "09:04 AM"
    val checkOutTime: String? = null, // e.g. "06:15 PM"
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val locationAddress: String = "HQ Tech Park, San Francisco",
    val locationLat: Double = 37.7749,
    val locationLng: Double = -122.4194,
    val overtimeHours: Double = 0.0,
    val notes: String = ""
)
