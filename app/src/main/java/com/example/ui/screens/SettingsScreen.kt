package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTrash: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val autoSync by viewModel.autoSync.collectAsState()
    val wifiOnly by viewModel.wifiOnly.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val pinNewDefault by viewModel.pinNewDefault.collectAsState()

    var showRemoveUserDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("การตั้งค่า", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ย้อนกลับ")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            SectionTitle("บัญชีและการซิงค์")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val user = currentUser
                    if (user == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ใช้งานออฟไลน์ได้ทันที", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    "ไม่ต้องสร้างบัญชีก็จดโน้ตได้ ข้อมูลจะอยู่ในเครื่องนี้ และเมื่อเข้าสู่ระบบจะสามารถสำรองและซิงค์ออนไลน์ได้",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onNavigateToLogin,
                            modifier = Modifier.fillMaxWidth().testTag("settings_login_prompt_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("เข้าสู่ระบบ / สมัครสมาชิก", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                if (user.imageUrl == "G") {
                                    Text("G", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                } else {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (user.accountType == "GOOGLE") "บัญชี Google" else "บัญชีอีเมล",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (lastSyncTime > 0) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = if (lastSyncTime > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("สถานะซิงค์", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(
                                    text = when {
                                        isSyncing -> "กำลังซิงค์ข้อมูล..."
                                        lastSyncTime > 0 -> "ซิงค์ล่าสุด: " + SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(lastSyncTime))
                                        else -> "ยังไม่เคยซิงค์"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { viewModel.triggerSimulatedCloudSync() },
                                enabled = !isSyncing,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ซิงค์", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ระบบออกแบบให้ใช้ได้ทั้งออฟไลน์และออนไลน์: โน้ตจะอยู่ในเครื่องเสมอ และบัญชีจะใช้สำหรับสำรอง/ซิงค์อัตโนมัติเมื่อเชื่อมระบบออนไลน์ครบ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            SectionTitle("ตัวเลือกการซิงค์ออนไลน์")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Column {
                    SettingsSwitchRow(
                        icon = Icons.Default.Sync,
                        title = "ซิงค์อัตโนมัติ",
                        subtitle = "เมื่อเข้าสู่ระบบ โน้ตใหม่และโน้ตที่แก้ไขจะถูกซิงค์กับบัญชีของคุณอัตโนมัติ",
                        checked = autoSync,
                        onCheckedChange = { viewModel.setAutoSync(it) },
                        enabled = currentUser != null,
                        tag = "setting_auto_sync_switch"
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsSwitchRow(
                        icon = Icons.Default.Wifi,
                        title = "ซิงค์เฉพาะ Wi-Fi",
                        subtitle = "ช่วยประหยัดอินเทอร์เน็ตมือถือ โดยให้ซิงค์ออนไลน์เมื่อเชื่อมต่อ Wi-Fi เท่านั้น",
                        checked = wifiOnly,
                        onCheckedChange = { viewModel.setWifiOnly(it) },
                        enabled = currentUser != null,
                        tag = "setting_wifi_switch"
                    )
                }
            }

            SectionTitle("หน้าตาและค่าเริ่มต้น")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Column {
                    SettingsSwitchRow(
                        icon = Icons.Default.DarkMode,
                        title = "ธีมมืด",
                        subtitle = "เปลี่ยนเป็นโทนมืดสำหรับใช้งานเวลากลางคืน",
                        checked = isDarkTheme,
                        onCheckedChange = { viewModel.setDarkTheme(it) },
                        tag = "setting_dark_theme_switch"
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsSwitchRow(
                        icon = Icons.Default.Pin,
                        title = "ปักหมุดโน้ตใหม่เป็นค่าเริ่มต้น",
                        subtitle = "โน้ตที่สร้างใหม่จะถูกตรึงไว้ด้านบนอัตโนมัติ",
                        checked = pinNewDefault,
                        onCheckedChange = { viewModel.setPinNewDefault(it) },
                        tag = "setting_pin_default_switch"
                    )
                }
            }

            SectionTitle("โน้ตที่ลบแล้ว")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTrash() }
                        .padding(16.dp)
                        .testTag("settings_trash_row")
                ) {
                    Icon(Icons.Default.RestoreFromTrash, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ถังขยะ", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("ดู กู้คืน หรือลบถาวรโน้ตที่เคยลบไปแล้ว", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (currentUser != null) {
                SectionTitle("จัดการผู้ใช้งาน")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.signOutUser() }.padding(16.dp).testTag("settings_logout_row")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ออกจากระบบ", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("โน้ตในเครื่องยังอยู่ และสามารถใช้งานออฟไลน์ต่อได้", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { showRemoveUserDialog = true }.padding(16.dp).testTag("settings_remove_user_row")
                        ) {
                            Icon(Icons.Default.PersonRemove, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ลบผู้ใช้งาน", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                                Text("นำบัญชีนี้ออกจากแอพบนเครื่องนี้ โน้ตในเครื่องยังคงอยู่", fontSize = 11.sp, color = MaterialTheme.colorScheme.error.copy(alpha = 0.75f))
                            }
                        }
                    }
                }
            }

            Text(
                text = "iM Notes Minimal v1.0.0 • Offline-first + Online Sync",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            )
        }
    }

    if (showRemoveUserDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveUserDialog = false },
            icon = { Icon(Icons.Default.PersonRemove, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("ลบผู้ใช้งาน?", fontWeight = FontWeight.Bold) },
            text = { Text("ต้องการนำผู้ใช้งานนี้ออกจากแอพบนเครื่องนี้ใช่ไหม? โน้ตที่อยู่ในเครื่องจะยังคงอยู่") },
            confirmButton = {
                Button(
                    onClick = {
                        showRemoveUserDialog = false
                        viewModel.signOutUser()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ยืนยันลบผู้ใช้งาน")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveUserDialog = false }) { Text("ยกเลิก") }
            }
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    tag: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(
                if (checked && enabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked && enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f).alpha(if (enabled) 1.0f else 0.4f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled, modifier = Modifier.testTag(tag))
    }
}
