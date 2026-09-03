package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.Employee
import com.example.data.model.LeaveStatus
import com.example.data.model.LiveWebSocketEvent
import com.example.data.model.NotificationType
import com.example.data.model.PayrollRecord
import com.example.data.model.PayrollStatus
import com.example.data.model.PushNotificationItem
import com.example.data.model.UserRole
import com.example.data.model.WebSocketStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        format.maximumFractionDigits = 0
        format.format(amount)
    } catch (e: Exception) {
        val rounded = amount.toLong()
        "₹" + String.format(Locale.getDefault(), "%,d", rounded)
    }
}

fun formatCurrencyDetailed(amount: Double): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        format.maximumFractionDigits = 2
        format.minimumFractionDigits = 2
        format.format(amount)
    } catch (e: Exception) {
        "₹" + String.format(Locale.getDefault(), "%,.2f", amount)
    }
}

fun convertNumberToWords(amount: Double): String {
    val num = amount.toLong()
    if (num <= 0L) return "Zero Rupees Only"

    val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    )
    val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    fun convertLessThanThousand(n: Int): String {
        var current = ""
        var rem = n
        if (rem >= 100) {
            current += units[rem / 100] + " Hundred "
            rem %= 100
        }
        if (rem >= 20) {
            current += tens[rem / 10] + " "
            rem %= 10
        }
        if (rem > 0) {
            current += units[rem] + " "
        }
        return current.trim()
    }

    var result = ""
    var n = num

    val crores = n / 10000000
    n %= 10000000
    val lakhs = n / 100000
    n %= 100000
    val thousands = n / 1000
    n %= 1000

    if (crores > 0) {
        result += convertLessThanThousand(crores.toInt()) + " Crore "
    }
    if (lakhs > 0) {
        result += convertLessThanThousand(lakhs.toInt()) + " Lakh "
    }
    if (thousands > 0) {
        result += convertLessThanThousand(thousands.toInt()) + " Thousand "
    }
    if (n > 0) {
        result += convertLessThanThousand(n.toInt()) + " "
    }

    return "Rupees " + result.trim() + " Only"
}

/**
 * Full-bleed Adaptive Background: Deep Obsidian / Royal Indigo in Dark Mode, Crisp Modern Frosted Slate in Light Mode
 */
@Composable
fun GlassCanvasBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalThemeIsDark.current
    val infiniteTransition = rememberInfiniteTransition(label = "mesh_pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val backgroundColors = if (isDark) {
        listOf(
            Color(0xFF090D1A), // Deep obsidian slate
            Color(0xFF140F2D), // Deep royal indigo-purple
            Color(0xFF0B192E), // Deep navy ocean blue
            Color(0xFF1B0C30)  // Deep midnight violet
        )
    } else {
        listOf(
            Color(0xFFF8FAFC), // Ultra-clean slate white
            Color(0xFFEEF2FF), // Soft lavender indigo tint
            Color(0xFFF1F5F9), // Crisp airy silver
            Color(0xFFE0E7FF)  // Soft luminous royal tint
        )
    }

    val orbColor1 = if (isDark) Color(0x406366F1) else Color(0x2B6366F1)
    val orbColor2 = if (isDark) Color(0x33A855F7) else Color(0x22A855F7)
    val orbColor3 = if (isDark) Color(0x2E06B6D4) else Color(0x2006B6D4)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = backgroundColors,
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 2000f)
                )
            )
            .drawBehind {
                // Soft Ambient Glowing Mesh Orbs (Linear & Stripe Glass feel)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orbColor1, Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.2f),
                        radius = size.width * 0.55f * pulseAnim
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orbColor2, Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.45f),
                        radius = size.width * 0.6f * (2f - pulseAnim)
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orbColor3, Color.Transparent),
                        center = Offset(size.width * 0.4f, size.height * 0.85f),
                        radius = size.width * 0.5f * pulseAnim
                    )
                )
            }
    ) {
        content()
    }
}

/**
 * Reusable Glassmorphism Card with frosted specular highlight and responsive dark/light styling
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = LocalThemeIsDark.current
    val shape = RoundedCornerShape(cornerRadius)
    val clickModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier

    val actualBg = backgroundColor ?: if (isDark) Color(0x1FFFFFFF) else Color(0xE6FFFFFF)
    val actualBorder = borderColor ?: if (isDark) Color(0x38FFFFFF) else Color(0xFFE2E8F0)

    Surface(
        shape = shape,
        color = actualBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, actualBorder),
        shadowElevation = if (isDark) 0.dp else 2.dp,
        modifier = modifier
            .clip(shape)
            .then(clickModifier)
            .drawBehind {
                // Subtle top-edge glossy specular shine
                val shineAlpha = if (isDark) 0.5f else 0.8f
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = shineAlpha * 0.6f),
                            Color.White.copy(alpha = shineAlpha),
                            Color.White.copy(alpha = shineAlpha * 0.6f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
    ) {
        Column(content = content)
    }
}

/**
 * Live WebSocket Connection Pill with Pulsing Glow Dot
 */
@Composable
fun LiveSocketStatusPill(
    status: WebSocketStatus = WebSocketStatus.CONNECTED,
    latencyMs: Int = 18,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ws_dot_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color(0x2610B981),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x5534D399)),
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("websocket_status_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34D399).copy(alpha = alpha))
            )
            Text(
                text = "WS LIVE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                color = Color(0xFF6EE7B7)
            )
            Text(
                text = "• ${latencyMs}ms",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = Color(0xFFD1FAE5)
            )
        }
    }
}

/**
 * Real-Time WebSocket Floating Banner Push Alert
 */
@Composable
fun LiveWebSocketBanner(
    event: LiveWebSocketEvent,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xD91E1B4B), // Translucent deep indigo-purple
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66818CF8)),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("live_websocket_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE0E7FF),
                            fontSize = 12.sp
                        )
                        Text(
                            text = event.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Top App Bar with Glassmorphic Translucency, Theme Toggle, Notification Bell, & Live Stream Controls
 */
@Composable
fun CloudAttendTopAppBar(
    currentScreen: AppScreen,
    currentRole: UserRole,
    currentUserName: String,
    isDarkMode: Boolean = true,
    unreadNotificationsCount: Int = 0,
    onRoleSwitchClick: () -> Unit,
    onThemeToggleClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current
    val barBg = if (isDark) Color(0xCC0B101E) else Color(0xF2FFFFFF)
    val barBorder = if (isDark) Color(0x26FFFFFF) else Color(0xFFE2E8F0)
    val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
    val actionBg = if (isDark) Color(0x26FFFFFF) else Color(0xFFF1F5F9)
    val actionBorder = if (isDark) Color(0x33FFFFFF) else Color(0xFFCBD5E1)
    val iconTint = if (isDark) Color.White else Color(0xFF334155)

    Surface(
        color = barBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, barBorder),
        shadowElevation = if (isDark) 0.dp else 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (onNavigateBack != null) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(actionBg)
                            .border(1.dp, actionBorder, RoundedCornerShape(10.dp))
                            .testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                                )
                            )
                            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudDone,
                            contentDescription = "App Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = if (currentScreen == AppScreen.DASHBOARD) "CloudAttend" else currentScreen.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (currentRole == UserRole.ADMIN) "HR ADMIN" else "STAFF",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontSize = 9.sp,
                            color = if (currentRole == UserRole.ADMIN) Color(0xFF6366F1) else Color(0xFF10B981)
                        )
                        Text(
                            text = "• REALTIME",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = Color(0xFF0284C7)
                        )
                    }
                }
            }

            // Right side: WebSocket Status Pill + Theme Provider Toggle + Notification Bell + Role Switcher + Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Live Status Pill
                LiveSocketStatusPill()

                // Theme Mode Switcher Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(actionBg)
                        .border(1.dp, actionBorder, RoundedCornerShape(10.dp))
                        .clickable { onThemeToggleClick() }
                        .testTag("theme_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                        tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFF6366F1),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Notification Bell with Badge
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(actionBg)
                        .border(1.dp, actionBorder, RoundedCornerShape(10.dp))
                        .clickable { onNotificationsClick() }
                        .testTag("notifications_bell_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Push Notifications",
                        tint = if (unreadNotificationsCount > 0) Color(0xFF6366F1) else iconTint,
                        modifier = Modifier.size(17.dp)
                    )
                    if (unreadNotificationsCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadNotificationsCount > 9) "9+" else unreadNotificationsCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Role Switcher Button with Glass glow
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (currentRole == UserRole.ADMIN) Color(0x334F46E5) else Color(0x3310B981),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (currentRole == UserRole.ADMIN) Color(0x80818CF8) else Color(0x8034D399)
                    ),
                    modifier = Modifier
                        .clickable { onRoleSwitchClick() }
                        .testTag("role_switcher_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = "Switch Role",
                            tint = if (isDark) Color.White else (if (currentRole == UserRole.ADMIN) Color(0xFF4338CA) else Color(0xFF047857)),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (currentRole == UserRole.ADMIN) "HR" else "Staff",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (isDark) Color.White else (if (currentRole == UserRole.ADMIN) Color(0xFF4338CA) else Color(0xFF047857))
                        )
                    }
                }

                // Avatar Icon with Glowing Glass Border
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(actionBg)
                        .border(1.dp, actionBorder, CircleShape)
                        .clickable { onSettingsClick() }
                        .testTag("settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Settings Profile",
                        tint = iconTint,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

/**
 * In-App Push Notification Alert Toast (Clock-in, Leave Status, Payroll)
 */
@Composable
fun PushNotificationBanner(
    notification: PushNotificationItem,
    onDismiss: () -> Unit,
    onOpen: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current
    val (icon, iconBg, borderTint) = when (notification.type) {
        NotificationType.CLOCK_IN_SUCCESS, NotificationType.CLOCK_OUT_SUCCESS ->
            Triple(Icons.Filled.Fingerprint, Color(0xFF10B981), Color(0xFF34D399))
        NotificationType.LEAVE_STATUS_UPDATED ->
            Triple(Icons.Filled.EventAvailable, Color(0xFF6366F1), Color(0xFF818CF8))
        NotificationType.PAYROLL_ALERT ->
            Triple(Icons.Filled.AccountBalanceWallet, Color(0xFF0EA5E9), Color(0xFF38BDF8))
        NotificationType.STATUS_OVERRIDE ->
            Triple(Icons.Filled.VerifiedUser, Color(0xFFF59E0B), Color(0xFFFBBF24))
        NotificationType.GENERAL_ALERT ->
            Triple(Icons.Filled.NotificationsActive, Color(0xFFA855F7), Color(0xFFC084FC))
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xF20F172A) else Color(0xF7FFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderTint.copy(alpha = 0.6f)),
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .then(if (onOpen != null) Modifier.clickable { onOpen() } else Modifier)
            .testTag("push_notification_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBg.copy(alpha = 0.2f))
                        .border(1.dp, iconBg.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconBg,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 13.sp
                        )
                        Text(
                            text = notification.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss Notification",
                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Push Notification History Bottom Sheet / Modal Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PushNotificationsModal(
    notifications: List<PushNotificationItem>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit,
    onItemClick: (PushNotificationItem) -> Unit
) {
    val isDark = LocalThemeIsDark.current
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredList = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "Attendance" -> notifications.filter { it.type == NotificationType.CLOCK_IN_SUCCESS || it.type == NotificationType.CLOCK_OUT_SUCCESS || it.type == NotificationType.STATUS_OVERRIDE }
            "Leaves" -> notifications.filter { it.type == NotificationType.LEAVE_STATUS_UPDATED }
            "Payroll" -> notifications.filter { it.type == NotificationType.PAYROLL_ALERT }
            else -> notifications
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0x336366F1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Push Notification Center",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = "${notifications.count { !it.isRead }} unread alert(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = onMarkAllRead,
                        modifier = Modifier.testTag("mark_all_read_button")
                    ) {
                        Text("Mark all read", fontSize = 11.sp, color = Color(0xFF6366F1))
                    }
                    TextButton(
                        onClick = onClearAll,
                        modifier = Modifier.testTag("clear_all_notifications_button")
                    ) {
                        Text("Clear", fontSize = 11.sp, color = Color(0xFFEF4444))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Attendance", "Leaves", "Payroll").forEach { tab ->
                    val isSelected = selectedFilter == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = tab },
                        label = { Text(tab, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6366F1),
                            selectedLabelColor = Color.White,
                            containerColor = if (isDark) Color(0x1AFFFFFF) else Color(0xFFF1F5F9),
                            labelColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsNone,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No notifications in this category",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList.size) { index ->
                        val item = filteredList[index]
                        val (icon, badgeColor) = when (item.type) {
                            NotificationType.CLOCK_IN_SUCCESS, NotificationType.CLOCK_OUT_SUCCESS ->
                                Pair(Icons.Filled.Fingerprint, Color(0xFF10B981))
                            NotificationType.LEAVE_STATUS_UPDATED ->
                                Pair(Icons.Filled.EventAvailable, Color(0xFF6366F1))
                            NotificationType.PAYROLL_ALERT ->
                                Pair(Icons.Filled.AccountBalanceWallet, Color(0xFF0EA5E9))
                            NotificationType.STATUS_OVERRIDE ->
                                Pair(Icons.Filled.VerifiedUser, Color(0xFFF59E0B))
                            NotificationType.GENERAL_ALERT ->
                                Pair(Icons.Filled.NotificationsActive, Color(0xFFA855F7))
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (item.isRead) {
                                if (isDark) Color(0x0FFFFFFF) else Color(0xFFF8FAFC)
                            } else {
                                if (isDark) Color(0x266366F1) else Color(0xFFEEF2FF)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (!item.isRead) Color(0x80818CF8) else (if (isDark) Color(0x1FFFFFFF) else Color(0xFFE2E8F0))
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor.copy(alpha = 0.15f))
                                        .border(1.dp, badgeColor.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isDark) Color.White else Color(0xFF0F172A),
                                            fontSize = 13.sp
                                        )
                                        if (!item.isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF6366F1))
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.timestamp,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                        fontSize = 10.sp
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

/**
 * High Density Glassmorphic SaaS Metric Stat Card
 */
@Composable
fun SaaSStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color = NeonIndigo,
    iconBgColor: Color = Color(0x334F46E5),
    badgeText: String? = null,
    badgeColor: Color = NeonEmerald,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.heightIn(min = 112.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBgColor)
                        .border(1.dp, iconColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = badgeColor.copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    color = Color.White
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

/**
 * Glossy Glassmorphic Attendance Status Badge
 */
@Composable
fun AttendanceStatusBadge(status: AttendanceStatus) {
    val (bg, border, fg, label) = when (status) {
        AttendanceStatus.PRESENT -> Quadruple(Color(0x2E10B981), Color(0x6634D399), Color(0xFF34D399), "PRESENT")
        AttendanceStatus.LATE -> Quadruple(Color(0x2EF59E0B), Color(0x66FBBF24), Color(0xFFFBBF24), "LATE")
        AttendanceStatus.HALF_DAY -> Quadruple(Color(0x2E0284C7), Color(0x6638BDF8), Color(0xFF38BDF8), "HALF DAY")
        AttendanceStatus.ABSENT -> Quadruple(Color(0x2EF43F5E), Color(0x66FB7185), Color(0xFFFB7185), "ABSENT")
        AttendanceStatus.ON_LEAVE -> Quadruple(Color(0x2EA855F7), Color(0x66C084FC), Color(0xFFC084FC), "ON LEAVE")
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = fg
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Glossy Glassmorphic Leave Status Badge
 */
@Composable
fun LeaveStatusBadge(status: LeaveStatus) {
    val (bg, border, fg, label) = when (status) {
        LeaveStatus.PENDING -> Quadruple(Color(0x2EF59E0B), Color(0x66FBBF24), Color(0xFFFBBF24), "PENDING")
        LeaveStatus.APPROVED -> Quadruple(Color(0x2E10B981), Color(0x6634D399), Color(0xFF34D399), "APPROVED")
        LeaveStatus.REJECTED -> Quadruple(Color(0x2EF43F5E), Color(0x66FB7185), Color(0xFFFB7185), "REJECTED")
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = fg
            )
        }
    }
}

/**
 * Glossy Glassmorphic Payroll Status Badge
 */
@Composable
fun PayrollStatusBadge(status: PayrollStatus) {
    val (bg, border, fg, label) = when (status) {
        PayrollStatus.PAID -> Quadruple(Color(0x2E10B981), Color(0x6634D399), Color(0xFF34D399), "PAID")
        PayrollStatus.PENDING -> Quadruple(Color(0x2EF59E0B), Color(0x66FBBF24), Color(0xFFFBBF24), "PENDING")
        PayrollStatus.PROCESSING -> Quadruple(Color(0x2E0284C7), Color(0x6638BDF8), Color(0xFF38BDF8), "PROCESSING")
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = fg
            )
        }
    }
}

/**
 * Glassmorphic Weekly Attendance Chart
 */
@Composable
fun WeeklyAttendanceChart(
    modifier: Modifier = Modifier
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val rates = listOf(95f, 92f, 88f, 100f, 85f, 75f)

    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Attendance Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Weekly check-in compliance rate",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0x2E10B981),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6634D399))
                ) {
                    Text(
                        text = "+4.2% vs last week",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val barWidth = 22.dp.toPx()
                val totalBars = days.size
                val spaceBetween = (size.width - (barWidth * totalBars)) / (totalBars + 1)
                val maxBarHeight = size.height - 20.dp.toPx()

                for (i in days.indices) {
                    val x = spaceBetween + i * (barWidth + spaceBetween)
                    val rate = rates[i]
                    val barHeight = (rate / 100f) * maxBarHeight

                    // Background track
                    drawRoundRect(
                        color = Color(0x1AFFFFFF),
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, maxBarHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Active bar with neon gradient
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF818CF8), Color(0xFF4F46E5), Color(0xFFA855F7)),
                            startY = maxBarHeight - barHeight,
                            endY = maxBarHeight
                        ),
                        topLeft = Offset(x, maxBarHeight - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            // Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                days.forEachIndexed { index, day ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "${rates[index].toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Glassmorphic Monthly Salary Trend Chart
 */
@Composable
fun MonthlySalaryTrendChart(
    modifier: Modifier = Modifier
) {
    val months = listOf("Apr", "May", "Jun", "Jul", "Aug", "Sep (Proj)")
    val values = listOf(46000f, 48200f, 49500f, 50100f, 52140f, 53800f)
    val maxVal = 60000f

    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Payroll Expenditure",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Total disbursed salaries (Last 6 months)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
                Text(
                    text = "$52,140",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF818CF8)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                val width = size.width
                val height = size.height
                val stepX = width / (months.size - 1)

                val points = values.mapIndexed { idx, v ->
                    val y = height - ((v / maxVal) * height)
                    Offset(idx * stepX, y)
                }

                for (i in 0 until points.size - 1) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFF06B6D4))
                        ),
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 3.5.dp.toPx()
                    )
                }

                points.forEach { pt ->
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color(0xFF6366F1),
                        radius = 2.dp.toPx(),
                        center = pt
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                months.forEach { m ->
                    Text(
                        text = m,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Translucent Glass Bottom Navigation Bar
 */
@Composable
fun CloudAttendBottomBar(
    currentScreen: AppScreen,
    currentRole: UserRole,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = if (currentRole == UserRole.ADMIN) {
        listOf(
            Triple(AppScreen.DASHBOARD, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
            Triple(AppScreen.ATTENDANCE, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
            Triple(AppScreen.LEAVES, Icons.Filled.EventNote, Icons.Outlined.EventNote),
            Triple(AppScreen.EMPLOYEES, Icons.Filled.People, Icons.Outlined.People),
            Triple(AppScreen.PAYROLL, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
        )
    } else {
        listOf(
            Triple(AppScreen.DASHBOARD, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
            Triple(AppScreen.ATTENDANCE, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
            Triple(AppScreen.LEAVES, Icons.Filled.EventNote, Icons.Outlined.EventNote),
            Triple(AppScreen.PAYROLL, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
            Triple(AppScreen.PROFILE, Icons.Filled.Person, Icons.Outlined.Person)
        )
    }

    Surface(
        color = Color(0xCC090E1A), // Frosted obsidian glass
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x26FFFFFF)),
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("main_navigation_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (screen, filledIcon, outlinedIcon) ->
                val isSelected = currentScreen == screen
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .then(
                            if (isSelected) Modifier.background(Color(0x2E6366F1)) else Modifier
                        )
                        .clickable { onNavigate(screen) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("nav_item_${screen.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) filledIcon else outlinedIcon,
                        contentDescription = screen.title,
                        tint = if (isSelected) Color(0xFF818CF8) else Color(0xFF64748B),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = when (screen) {
                            AppScreen.DASHBOARD -> "Home"
                            AppScreen.ATTENDANCE -> "Attendance"
                            AppScreen.LEAVES -> "Leaves"
                            AppScreen.EMPLOYEES -> "Staff"
                            AppScreen.PAYROLL -> "Payroll"
                            AppScreen.PROFILE -> "Settings"
                            else -> screen.title
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 10.sp,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

/**
 * Reusable Monthly Salary Summary Component
 * Displays calculated gross and net pay based on employee attendance data
 */
@Composable
fun MonthlySalarySummaryCard(
    employee: Employee,
    monthYear: String = "August 2026",
    totalWorkingDays: Int = 22,
    presentDays: Int = 21,
    paidLeaves: Int = 1,
    unpaidLeaves: Int = 0,
    overtimeHours: Double = 4.0,
    modifier: Modifier = Modifier,
    onViewBreakdownClick: (() -> Unit)? = null
) {
    // Attendance-based calculations
    val perDayRate = employee.grossSalaryMonthly / totalWorkingDays
    val hourlyRate = (employee.baseSalary / totalWorkingDays) / 8.0
    val overtimePay = overtimeHours * hourlyRate * 1.5
    val unpaidDeduction = unpaidLeaves * perDayRate

    val calculatedGross = employee.grossSalaryMonthly + overtimePay - unpaidDeduction
    val taxDeduction = (calculatedGross * (employee.taxRatePercent / 100.0)).coerceAtLeast(0.0)
    val pfDeduction = (employee.baseSalary * (employee.pfRatePercent / 100.0)).coerceAtLeast(0.0)
    val totalDeductions = taxDeduction + pfDeduction + employee.insuranceDeduction + unpaidDeduction
    val calculatedNetPay = (calculatedGross - taxDeduction - pfDeduction - employee.insuranceDeduction).coerceAtLeast(0.0)

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_salary_summary_card"),
        backgroundColor = Color(0x281E1B4B),
        borderColor = Color(0x66818CF8),
        onClick = onViewBreakdownClick
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Employee Details & Month
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
                        Text(
                            text = employee.name.take(1),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = employee.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${employee.empCode} • ${employee.designation}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0x2E6366F1),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66818CF8))
                ) {
                    Text(
                        text = monthYear,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF818CF8),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Attendance Factor Pill Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x14FFFFFF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x26FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EventAvailable,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Attendance: $presentDays/$totalWorkingDays Days Present",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    if (overtimeHours > 0) {
                        Text(
                            text = "+${overtimeHours}h OT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            // High-Contrast Salary Metric Cards: Calculated Gross Pay vs Calculated Net Pay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Gross Pay Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x1A6366F1),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x40818CF8)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "CALCULATED GROSS",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrencyDetailed(calculatedGross),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Base + Allowances + OT",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Net Pay Card (Highlighted Take-Home)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x2E10B981),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x8034D399)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "NET TAKE-HOME",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6EE7B7)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrencyDetailed(calculatedNetPay),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                        Text(
                            text = "After Taxes & PF Deductions",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = Color(0xFF6EE7B7).copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Detailed Line-Item Breakdown
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x0DFFFFFF),
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
                        Text("Base Monthly Salary", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                        Text(formatCurrency(employee.baseSalary), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("HRA & Allowances", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                        Text(formatCurrency(employee.hra + employee.specialAllowance + employee.transportAllowance), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    if (overtimePay > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Overtime Earnings (${overtimeHours}h)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF38BDF8))
                            Text("+${formatCurrencyDetailed(overtimePay)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF38BDF8))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TDS Withholding & PF (${employee.taxRatePercent}% + ${employee.pfRatePercent}%)", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFB7185))
                        Text("-${formatCurrencyDetailed(taxDeduction + pfDeduction)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFFFB7185))
                    }
                }
            }

            if (onViewBreakdownClick != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View Detailed Payslip",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF818CF8)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
