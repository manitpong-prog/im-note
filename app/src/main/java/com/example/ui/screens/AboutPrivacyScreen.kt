package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPrivacyScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "เกี่ยวกับและความเป็นส่วนตัว",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard(
                icon = Icons.Default.Info,
                title = "iM Notes Minimal",
                body = "แอพจดโน้ตแบบเรียบง่าย ใช้งานออฟไลน์ได้ทันที และสามารถเข้าสู่ระบบเพื่อสำรอง/ซิงค์โน้ตออนไลน์ผ่าน Supabase"
            )

            InfoCard(
                icon = Icons.Default.Security,
                title = "ข้อมูลของคุณถูกเก็บที่ไหน",
                body = "ถ้าไม่ได้เข้าสู่ระบบ โน้ตจะถูกเก็บไว้ในเครื่องนี้เท่านั้น\n\nถ้าเข้าสู่ระบบ โน้ตจะถูกเก็บในเครื่องก่อน แล้วซิงค์ขึ้นฐานข้อมูลออนไลน์ของบัญชีคุณ เพื่อให้ติดตั้งใหม่หรือย้ายเครื่องแล้วยังดึงโน้ตกลับมาได้"
            )

            InfoCard(
                icon = Icons.Default.Security,
                title = "ความเป็นส่วนตัว",
                body = "แอพนี้ออกแบบให้ผู้ใช้แต่ละบัญชีเห็นเฉพาะโน้ตของตัวเองผ่านระบบ Row Level Security ของ Supabase\n\nผู้พัฒนาไม่ควรฝัง secret key หรือ service role key ในแอพมือถือ และแอพนี้ใช้เฉพาะค่า public/anon key สำหรับการเชื่อมต่อจากตัวแอพ"
            )

            InfoCard(
                icon = Icons.Default.Info,
                title = "การลบข้อมูล",
                body = "การลบโน้ตปกติจะย้ายไปถังขยะก่อน เพื่อให้กู้คืนได้\n\nถ้ากดลบถาวรจากถังขยะ โน้ตนั้นจะถูกลบออกจากเครื่อง และถ้าเคยซิงค์แล้ว ระบบจะลบข้อมูลออนไลน์ของโน้ตนั้นด้วย"
            )

            Text(
                text = "Version 1.0.0",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = body,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
