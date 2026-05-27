package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
    var isEditing by remember(noteId) { mutableStateOf(noteId == null) }

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
    val animatedBackground by animateColorAsState(targetValue = colorProfile.surface)

    fun handleSaveAndBack() {
        if (!isEditing) {
            onNavigateBack()
            return
        }

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
            onNavigateBack()
        }
    }

    fun handleSaveOnly() {
        if (title.isNotBlank() || content.isNotBlank()) {
            viewModel.saveNote(
                noteId = noteId,
                title = title.trim(),
                content = content,
                colorIndex = colorIndex,
                isPinned = isPinned,
                onComplete = { isEditing = false }
            )
        } else {
            isEditing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            noteId == null -> "สร้างบันทึก"
                            isEditing -> "แก้ไขบันทึก"
                            else -> "อ่านบันทึก"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { handleSaveAndBack() },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isEditing) "ย้อนกลับและบันทึก" else "ย้อนกลับ"
                        )
                    }
                },
                actions = {
                    if (isEditing) {
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
                    }

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

                    if (isEditing) {
                        IconButton(
                            onClick = { handleSaveOnly() },
                            modifier = Modifier.testTag("editor_save_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "บันทึก"
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { isEditing = true },
                            modifier = Modifier.testTag("editor_edit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "แก้ไขบันทึก"
                            )
                        }
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
                if (isEditing) {
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

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = colorProfile.textSecondaryOnSurface.copy(alpha = 0.15f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(18.dp)
                ) {
                    if (isEditing) {
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
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = title.ifBlank { "ไม่ได้ตั้งชื่อบันทึก" },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorProfile.textOnSurface,
                                lineHeight = 30.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            if (content.isBlank()) {
                                Text(
                                    text = "บันทึกนี้ยังไม่มีเนื้อหา",
                                    fontSize = 16.sp,
                                    color = colorProfile.textSecondaryOnSurface.copy(alpha = 0.7f)
                                )
                            } else {
                                LinkifiedText(
                                    text = content,
                                    textColor = colorProfile.textOnSurface,
                                    linkColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedButton(
                                onClick = { isEditing = true },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("แก้ไขบันทึก")
                            }
                        }
                    }
                }
            }
        } else {
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

@Composable
private fun LinkifiedText(
    text: String,
    textColor: Color,
    linkColor: Color,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val annotatedText = remember(text, textColor, linkColor) {
        buildLinkAnnotatedString(
            text = text,
            textColor = textColor,
            linkColor = linkColor
        )
    }

    ClickableText(
        text = annotatedText,
        style = TextStyle(
            fontSize = 16.sp,
            color = textColor,
            lineHeight = 24.sp
        ),
        modifier = modifier,
        onClick = { offset ->
            annotatedText
                .getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()
                ?.let { annotation ->
                    runCatching { uriHandler.openUri(annotation.item) }
                }
        }
    )
}

private fun buildLinkAnnotatedString(
    text: String,
    textColor: Color,
    linkColor: Color
) = buildAnnotatedString {
    append(text)
    addStyle(
        style = SpanStyle(color = textColor),
        start = 0,
        end = text.length
    )

    val urlRegex = Regex(
        pattern = "(?i)\\b((?:https?://|www\\.)[^\\s<>()]+|[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,})"
    )

    urlRegex.findAll(text).forEach { match ->
        val rawValue = match.value.trimEnd('.', ',', ';', ':', ')', ']', '}')
        val start = match.range.first
        val end = start + rawValue.length
        val normalizedUrl = normalizeLink(rawValue)

        addStyle(
            style = SpanStyle(
                color = linkColor,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.SemiBold
            ),
            start = start,
            end = end
        )
        addStringAnnotation(
            tag = "URL",
            annotation = normalizedUrl,
            start = start,
            end = end
        )
    }
}

private fun normalizeLink(value: String): String {
    return when {
        value.contains("@") && !value.startsWith("http", ignoreCase = true) && !value.startsWith("www.", ignoreCase = true) -> "mailto:$value"
        value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true) -> value
        value.startsWith("www.", ignoreCase = true) -> "https://$value"
        else -> value
    }
}
