package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
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
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()

    val autoSync by viewModel.autoSync.collectAsState()
    val wifiOnly by viewModel.wifiOnly.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val pinNewDefault by viewModel.pinNewDefault.collectAsState()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteFailsafeDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("การตั้งค่าใช้งาน (Settings)", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ย้อนกลับ"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            
            // SECTION 1: USER PROFILE & SYNC STATUS
            Text(
                text = "โปรไฟล์และการซิงค์ข้อมูล",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val user = currentUser
                    if (user != null) {
                        // User is Logged In
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // User visual avatar block
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                if (user.imageUrl == "G") {
                                    Text("G", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = user.email,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Badge(
                                    containerColor = if (user.accountType == "GOOGLE") Color(0xFFE8F5E9) else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (user.accountType == "GOOGLE") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = if (user.accountType == "GOOGLE") "เชื่อมต่อด้วย Google" else "บัญชีแบบปกติ",
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                        )

                        // Sync Status Information block
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (lastSyncTime > 0) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = if (lastSyncTime > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("สถานภาพการสำรองข้อมูล", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(
                                    text = if (isSyncing) "กำลังบันทึกลงคลาวด์สำรอง..." 
                                           else if (lastSyncTime > 0) "ซิงค์ล่าสุดเมื่อ: " + SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(lastSyncTime))
                                           else "ยังไม่ผ่านการซิงค์",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { viewModel.triggerSimulatedCloudSync() },
                                enabled = !isSyncing,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ซิงค์ด่วน", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Guest State (Prompt login)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("เล่นแอปโหมดออฟไลน์", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ไม่มีการสำรองบนคลาวด์ โปรดยึดเข้าสู่ระบบเพื่อความปลอดภัยเมื่อย้ายเครื่อง", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onNavigateToLogin,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_login_prompt_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("เข้าสู่ระบบ / สมัครสมาชิก", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }


            // SECTION 2: SYNC PREFERENCES (SWITCHES)
            Text(
                text = "พิกัดส่งข้อมูลสำรอง (Sync Configuration)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column {
                    SettingsSwitchRow(
                        icon = Icons.Default.Sync,
                        title = "ซิงค์ข้อมูลอัตโนมัติ (Auto Cloud Sync)",
                        subtitle = "อัปโหลดข้อมูลคำนวณบันทึกเข้าเซิร์ฟเวอร์สำรองทันทีเมื่อเขียนโน้ตใหม่เสร็จสิ้น",
                        checked = autoSync,
                        onCheckedChange = { viewModel.setAutoSync(it) },
                        enabled = currentUser != null,
                        tag = "setting_auto_sync_switch"
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    SettingsSwitchRow(
                        icon = Icons.Default.Wifi,
                        title = "ซิงค์เฉพาะใช้ Wi-Fi เท่านั้น",
                        subtitle = "ประหยัดปริมาณอินเทอร์เน็ตมือถือและหลีกเลี่ยงการเชื่อมโยงความเร็วต่ำ",
                        checked = wifiOnly,
                        onCheckedChange = { viewModel.setWifiOnly(it) },
                        enabled = currentUser != null,
                        tag = "setting_wifi_switch"
                    )
                }
            }

            // SECTION 3: APP DESIGN CUSTOMIZATION
            Text(
                text = "รูปแบบหน้าตาธีมและการตั้งค่าเริ่มต้น",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column {
                    SettingsSwitchRow(
                        icon = Icons.Default.DarkMode,
                        title = "ธีมมืด (Dark Mode)",
                        subtitle = "เปิดโทนมืด ถนอมสายตาสำหรับการบันทึกเวลากลางคืนเพื่อสุขภาพ",
                        checked = isDarkTheme,
                        onCheckedChange = { viewModel.setDarkTheme(it) },
                        tag = "setting_dark_theme_switch"
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    SettingsSwitchRow(
                        icon = Icons.Default.Pin,
                        title = "ปักหมุดโน้ตใหม่เสมอเป็นค่าเริ่มต้น",
                        subtitle = "เมื่อพิมพ์สร้างบันทึกใหม่ จะถูกตรึงให้อยู่บนส่วนบนสุดของจอทันที",
                        checked = pinNewDefault,
                        onCheckedChange = { viewModel.setPinNewDefault(it) },
                        tag = "setting_pin_default_switch"
                    )
                }
            }

            // SECTION 4: ACCOUNT ADMINISTRATION (DANGER ZONE & LOGOUT)
            Text(
                text = "ข้อมูลความร้อนแรงและบัญชีเข้าใช้งาน",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column {
                    if (currentUser != null) {
                        // Sign out item
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.signOutUser()
                                }
                                .padding(16.dp)
                                .testTag("settings_logout_row")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ออกจากระบบบัญชีผู้ใช้นี้", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("ลงชื่อออก และใช้งานแบบผู้เยี่ยมชมออฟไลน์ในเครื่องนี้", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    }

                    // Delete Account Visual row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeleteConfirmDialog = true }
                            .padding(16.dp)
                            .testTag("settings_delete_account_row")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "ลบบัญชีผู้ใช้และทำลายข้อมูลบันทึกทั้งหมดถาวร", 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "ลบบัญชี ประวัติการสำรอง ตลอดจนโน้ตทั้งหมดบนเครื่องออกจากระบบโดยถาวร (ถอนคืนไม่ได้)", 
                                fontSize = 11.sp, 
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Tech Stack & Version Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "iM Notes Minimal v1.2.0-Alpha Edition",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "Secure local SQLite with simulated multi-device sync encryption.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // DIALOG 1: Primary Account Deletion Double Confirmation Warn
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp)) },
            title = {
                Text(
                    "ยืนยันที่จะทำลายล้างข้อมูลจริงใช่หรือไม่?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    "การทำลายบัญชีผู้ใช้งาน จะดำเนินการกวาดล้างข้อมูลโน้ตทั้งหมดที่บันทึกไว้ และปิดบัญชีในคลังเซิร์ฟเวอร์อย่างสมบูรณ์แบบ ข้อมูลทุกรหัสบนอุปกรณ์เครื่องนี้จะถูกเคลียร์ทำความสะอาดทั้งหมด กรุณายืนยันความประสงค์ของคุณด่วนที่สุด",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        showDeleteFailsafeDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                ) {
                    Text("ใช่, ฉันมั่นใจและขอดำเนินการต่อ", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ยกเลิก, ยกเลิกการกระทำนี้", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // DIALOG 2: Final Failsafe confirmation dialog (Guarantees zero accidental deletions)
    if (showDeleteFailsafeDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteFailsafeDialog = false },
            title = {
                Text(
                    "ดำเนินการทำร้ายระบบถาวรขั้นสุดท้าย",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    "ตรวจสอบความถูกต้องเป็นครั้งสุดท้าย: หากกดยืนยันแล้วจะไม่สามารถกู้คืนบัญชีหรือข้อเขียนใดๆ กลับมาได้อีก ปลอบใจตัวเองให้ดีก่อนจะลงทำสั่งตัดสินใจลบล้าง",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteFailsafeDialog = false
                        viewModel.deleteUserAccount {
                            onNavigateBack() // Go back home safely
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                ) {
                    Text("อนุมัติทำลายข้อมูลทั้งหมดทันที", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteFailsafeDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ย้อนกลับเดี๋ยวนี้", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (checked && enabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceVariant
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

        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(if (enabled) 1.0f else 0.4f)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.testTag(tag)
        )
    }
}
