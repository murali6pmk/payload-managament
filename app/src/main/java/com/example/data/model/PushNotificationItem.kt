package com.example.data.model

import java.util.UUID

enum class NotificationType {
    CLOCK_IN_SUCCESS,
    CLOCK_OUT_SUCCESS,
    LEAVE_STATUS_UPDATED,
    PAYROLL_ALERT,
    STATUS_OVERRIDE,
    GENERAL_ALERT
}

data class PushNotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: String,
    val targetEmployeeId: Long? = null,
    val targetEmployeeName: String? = null,
    val isRead: Boolean = false,
    val actionRoute: String? = null
)
