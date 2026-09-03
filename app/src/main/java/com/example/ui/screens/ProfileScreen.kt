package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.GlassCard
import com.example.ui.components.SaaSStatCard
import com.example.ui.components.formatCurrency
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CloudAttendViewModel

@Composable
fun ProfileScreen(
    viewModel: CloudAttendViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentEmployee by viewModel.currentEmployee.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val isSimActive by viewModel.isRealTimeSimActive.collectAsState()
    val webSocketLatency by viewModel.webSocketLatency.collectAsState()

    var showPersonaPicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Glass Card Header
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0x281E1B4B),
                borderColor = Color(0x66818CF8)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                                )
                            )
                            .border(2.dp, Color(0x80FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentEmployee?.name?.take(2)?.uppercase() ?: "CA",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentEmployee?.name ?: "Sarah Jenkins",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${currentEmployee?.designation ?: "VP of People & Ops"} • ${currentEmployee?.department ?: "Human Resources"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1)
                        )
                        Text(
                            text = currentEmployee?.email ?: "sarah.j@cloudattend.io",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF818CF8)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (currentRole == UserRole.ADMIN) Color(0x334F46E5) else Color(0x3310B981),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (currentRole == UserRole.ADMIN) Color(0x80818CF8) else Color(0x8034D399)
                            )
                        ) {
                            Text(
                                text = if (currentRole == UserRole.ADMIN) "ROLE: SYSTEM ADMINISTRATOR" else "ROLE: VERIFIED EMPLOYEE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                color = if (currentRole == UserRole.ADMIN) Color(0xFFE0E7FF) else Color(0xFF6EE7B7),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Real-Time WebSocket Diagnostics Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0x1FFFFFFF),
                borderColor = Color(0x33FFFFFF)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(20.dp))
                            Text("Real-Time Stream Gateway", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Switch(
                            checked = isSimActive,
                            onCheckedChange = { viewModel.toggleWebSocketSimulation() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4F46E5)
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x14FFFFFF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Protocol:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                Text("WSS / Secure Bidirectional Socket", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Live Ping Latency:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                Text("${webSocketLatency}ms (Optimal)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Live Broadcast Push:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                Text(if (isSimActive) "Active (20s cycle)" else "Paused", style = MaterialTheme.typography.labelSmall, color = Color(0xFF38BDF8))
                            }
                        }
                    }

                    // Test Broadcast Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x334F46E5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66818CF8)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clickable { viewModel.triggerSimulatedPeerEvent() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Broadcast Test Event (Simulate Colleague)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Account Details Glass Box
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ACCOUNT & EMPLOYMENT PROFILE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF818CF8)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Employee Code", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        Text(currentEmployee?.empCode ?: "EMP-1001", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Join Date", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        Text(currentEmployee?.joiningDate ?: "2023-01-15", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Monthly CTC", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        Text(formatCurrency(currentEmployee?.grossSalaryMonthly ?: 8500.0), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                    }
                }
            }
        }

        // Switch Persona & Demo Actions
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ENVIRONMENT & CONTROLS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF818CF8)
                    )

                    // Theme Mode Switcher Row
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x1AFFFFFF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x26FFFFFF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleTheme() }
                            .testTag("profile_theme_toggle_row")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFF818CF8) else Color(0xFFFBBF24)
                                )
                                Column {
                                    Text(
                                        text = if (isDarkMode) "Dark Theme Active" else "Light Theme Active",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isDarkMode) "OLED Glassmorphic Midnight palette" else "Clean High-Contrast Bright palette",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.setDarkMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF6366F1),
                                    uncheckedThumbColor = Color(0xFF6366F1),
                                    uncheckedTrackColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.testTag("theme_switch_toggle")
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x1AFFFFFF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x26FFFFFF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPersonaPicker = true }
                            .testTag("switch_persona_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Filled.SupervisorAccount, contentDescription = null, tint = Color(0xFF818CF8))
                                Column {
                                    Text("Switch Active Persona / Role", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Toggle between Admin or other Staff members", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                                }
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF818CF8))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x1AFFFFFF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x26FFFFFF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.resetDemoData() }
                            .testTag("reset_demo_data_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = Color(0xFFFB7185))
                                Column {
                                    Text("Reload Sample Data", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Reset all attendance and payroll records", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                                }
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF818CF8))
                        }
                    }
                }
            }
        }
    }

    // Modal: Persona Picker
    if (showPersonaPicker) {
        AlertDialog(
            onDismissRequest = { showPersonaPicker = false },
            title = { Text("Select Employee Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(employees) { emp ->
                        val isSelected = currentEmployee?.id == emp.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0x336366F1) else Color(0x1AFFFFFF),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF818CF8) else Color(0x26FFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCurrentEmployee(emp)
                                    showPersonaPicker = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(emp.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${emp.empCode} • ${emp.designation} (${emp.role.name})", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                }
                                if (isSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF34D399))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPersonaPicker = false }) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E1B4B)
        )
    }
}
