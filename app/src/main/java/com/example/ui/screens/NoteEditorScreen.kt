package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NoteViewModel
import com.example.ui.theme.NoteColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Int?,
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(0) }
    var isPinned by remember { mutableStateOf(false) }
    var isLoaded by remember { mutableStateOf(false) }

    // Fetch note data once if editing an existing note
    if (noteId != null && !isLoaded) {
        LaunchedEffect(noteId) {
            val note = viewModel.getNoteById(noteId)
            if (note != null) {
                title = note.title
                content = note.content
                colorIndex = note.colorIndex
                isPinned = note.isPinned
            }
            isLoaded = true
        }
    } else if (noteId == null) {
        isLoaded = true
    }

    val colorProfile = NoteColors.getProfile(colorIndex)
    // Animate background color morphing transitions
    val animatedBackground by animateColorAsState(targetValue = colorProfile.surface)

    fun handleSaveAndBack() {
        if (title.isNotBlank() || content.isNotBlank()) {
            viewModel.saveNote(
                noteId = noteId,
                title = title.trim(),
                content = content,
                colorIndex = colorIndex,
                isPinned = isPinned,
                onComplete = onNavigateBack
            )
        } else {
            // Empty note: just go back
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "สร้างบันทึก" else "แก้ไขบันทึก", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { handleSaveAndBack() },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ย้อนกลับและบันทึก"
                        )
                    }
                },
                actions = {
                    // Pin Note state indicator button
                    IconButton(
                        onClick = { isPinned = !isPinned },
                        modifier = Modifier.testTag("editor_pin_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (isPinned) "ถอนหมุด" else "ปักหมุด",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else colorProfile.textSecondaryOnSurface.copy(alpha = 0.5f)
                        )
                    }

                    // Delete button (only show if note actually exists)
                    if (noteId != null) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val note = viewModel.getNoteById(noteId)
                                    if (note != null) {
                                        viewModel.deleteNote(note)
                                        onNavigateBack()
                                    }
                                }
                            },
                            modifier = Modifier.testTag("editor_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "ลบบันทึกนี้",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Explicit Save Button
                    IconButton(
                        onClick = { handleSaveAndBack() },
                        modifier = Modifier.testTag("editor_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "บันทึก"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = animatedBackground,
                    navigationIconContentColor = colorProfile.textOnSurface,
                    titleContentColor = colorProfile.textOnSurface,
                    actionIconContentColor = colorProfile.textOnSurface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (isLoaded) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(animatedBackground)
            ) {
                // Color Picker panel: row of color alternatives
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NoteColors.profiles.forEach { profile ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(profile.tagColor)
                                .clickable { colorIndex = profile.colorIndex }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorIndex == profile.colorIndex) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "ใช้สี ${profile.name}",
                                    tint = profile.textOnSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Divider line dividing colors panel and text canvases
                HorizontalDivider(
                    thickness = 1.dp,
                    color = colorProfile.textSecondaryOnSurface.copy(alpha = 0.15f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Input areas wrapper
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(18.dp)
                ) {
                    // Title Text Field
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = {
                            Text(
                                "ชื่อบันทึก...",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorProfile.textSecondaryOnSurface.copy(alpha = 0.5f)
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorProfile.textOnSurface
                        ),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_title_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Content Body Text Field
                    TextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = {
                            Text(
                                "เขียนข้อความจดบันทึกของคุณที่นี่...",
                                fontSize = 16.sp,
                                color = colorProfile.textSecondaryOnSurface.copy(alpha = 0.5f)
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = colorProfile.textOnSurface,
                            lineHeight = 24.sp
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("note_content_input")
                    )
                }
            }
        } else {
            // Simple indicator while loading
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
