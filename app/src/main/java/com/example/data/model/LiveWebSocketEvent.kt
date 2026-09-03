package com.example.data.model

import java.util.UUID

enum class LiveEventType {
    CHECK_IN,
    CHECK_OUT,
    LEAVE_APPLIED,
    LEAVE_APPROVED,
    LEAVE_REJECTED,
    STATUS_CHANGED,
    PAYROLL_PROCESSED
}

enum class WebSocketStatus {
    CONNECTED,
    CONNECTING,
    DISCONNECTED
}

data class LiveWebSocketEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: LiveEventType,
    val title: String,
    val description: String,
    val timestamp: String,
    val employeeName: String,
    val employeeCode: String,
    val avatarColorHex: String = "#4F46E5",
    val highlightStatus: String? = null
)
