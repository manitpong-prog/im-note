package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import com.example.ui.NoteViewModel
import com.example.ui.SortMode
import com.example.ui.theme.NoteColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel,
    onNoteClick: (Int?) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.filteredAndSortedNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentSortMode by viewModel.sortMode.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val activeColorFilter by viewModel.selectedColorFilter.collectAsState()

    val currentUser by viewModel.currentUser.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var dismissPromoBanner by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "iM Notes Minimal",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleGridView() },
                        modifier = Modifier.testTag("layout_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                            contentDescription = "เปลี่ยนมุมมองแสดงผล"
                        )
                    }

                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.testTag("sort_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "เรียงลำดับบันทึก"
                        )
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null) },
                                text = { Text(SortMode.EDITED_DESC.displayNameTh) },
                                onClick = {
                                    viewModel.setSortMode(SortMode.EDITED_DESC)
                                    showSortMenu = false
                                },
                                modifier = Modifier.testTag("sort_newest_edited")
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null) },
                                text = { Text(SortMode.EDITED_ASC.displayNameTh) },
                                onClick = {
                                    viewModel.setSortMode(SortMode.EDITED_ASC)
                                    showSortMenu = false
                                },
                                modifier = Modifier.testTag("sort_oldest_edited")
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) },
                                text = { Text(SortMode.ALPHA_ASC.displayNameTh) },
                                onClick = {
                                    viewModel.setSortMode(SortMode.ALPHA_ASC)
                                    showSortMenu = false
                                },
                                modifier = Modifier.testTag("sort_alpha_asc")
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) },
                                text = { Text(SortMode.ALPHA_DESC.displayNameTh) },
                                onClick = {
                                    viewModel.setSortMode(SortMode.ALPHA_DESC)
                                    showSortMenu = false
                                },
                                modifier = Modifier.testTag("sort_alpha_desc")
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_menu_button")
                    ) {
                        if (currentUser != null) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.displayName?.take(1)?.uppercase() ?: "U",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "การตั้งค่าใช้งาน"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNoteClick(null) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .testTag("add_note_fab")
                    .padding(8.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "เพิ่มบันทึกใหม่",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("ค้นหาจากชื่อเรื่อง หรือเนื้อหา...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "ค้นหา") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "ล้างการค้นหา")
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .testTag("search_input_field")
            )

            AnimatedVisibility(
                visible = isSyncing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "กำลังอัปเดตสถานะสำรองข้อมูลในเครื่อง...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            AnimatedVisibility(
                visible = currentUser == null && !dismissPromoBanner,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    onClick = onNavigateToSettings,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("auth_promo_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "โหมดใช้งานในเครื่อง",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "โน้ตถูกบันทึกไว้ในเครื่องนี้ก่อน ระบบบัญชีเป็นโหมดทดลองสำหรับทดสอบ UI",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(
                            onClick = { dismissPromoBanner = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "ปิดแบนเนอร์",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            ColorFilterRow(
                selectedColor = activeColorFilter,
                onColorSelected = { viewModel.setColorFilter(it) }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "จัดเรียง: ${currentSortMode.displayNameTh}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (activeColorFilter != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(NoteColors.getProfile(activeColorFilter!!).tagColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "สี: ${NoteColors.getProfile(activeColorFilter!!).name}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (notes.isEmpty()) {
                EmptyNotesState(
                    hasFilter = searchQuery.isNotEmpty() || activeColorFilter != null,
                    onClearFilters = {
                        viewModel.setSearchQuery("")
                        viewModel.setColorFilter(null)
                    }
                )
            } else {
                if (isGridView) {
                    NotesGrid(
                        notes = notes,
                        onNoteClick = onNoteClick,
                        onNotePinToggle = { viewModel.togglePinState(it) },
                        onNoteDelete = { showDeleteConfirmDialog = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    )
                } else {
                    NotesList(
                        notes = notes,
                        onNoteClick = onNoteClick,
                        onNotePinToggle = { viewModel.togglePinState(it) },
                        onNoteDelete = { showDeleteConfirmDialog = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    showDeleteConfirmDialog?.let { note ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("ลบบันทึก?") },
            text = { Text("ต้องการลบบันทึก '${note.title}' ใช่ไหม? เมื่อลบแล้วจะไม่สามารถย้อนกลับได้") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNote(note)
                        showDeleteConfirmDialog = null
                    },
                    modifier = Modifier.testTag("dialog_confirm_delete")
                ) {
                    Text("ลบ", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@Composable
fun ColorFilterRow(
    selectedColor: Int?,
    onColorSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selectedColor == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onColorSelected(null) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ทั้งหมด",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (selectedColor == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        NoteColors.profiles.forEach { profile ->
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(profile.tagColor)
                    .clickable { onColorSelected(profile.colorIndex) }
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedColor == profile.colorIndex) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "เลือก ${profile.name}",
                        tint = profile.textOnSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NotesList(
    notes: List<Note>,
    onNoteClick: (Int) -> Unit,
    onNotePinToggle: (Note) -> Unit,
    onNoteDelete: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.testTag("notes_list_view"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteCardItem(
                note = note,
                onClick = { onNoteClick(note.id) },
                onPinToggle = { onNotePinToggle(note) },
                onDelete = { onNoteDelete(note) }
            )
        }
    }
}

@Composable
fun NotesGrid(
    notes: List<Note>,
    onNoteClick: (Int) -> Unit,
    onNotePinToggle: (Note) -> Unit,
    onNoteDelete: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.testTag("notes_grid_view"),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteCardItem(
                note = note,
                onClick = { onNoteClick(note.id) },
                onPinToggle = { onNotePinToggle(note) },
                onDelete = { onNoteDelete(note) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCardItem(
    note: Note,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorProfile = NoteColors.getProfile(note.colorIndex)
    val formattedDate = remember(note.updatedAt) {
        val sdf = SimpleDateFormat("dd MMM yy, HH:mm", Locale("th", "TH"))
        sdf.format(Date(note.updatedAt))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorProfile.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onDelete
            )
            .testTag("note_item_${note.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorProfile.textOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onPinToggle,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("pin_button_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = if (note.isPinned) "ถอนหมุด" else "ปักหมุด",
                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary else colorProfile.textSecondaryOnSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.content.ifBlank { "บันทึกนี้ยังไม่มีเนื้อหา" },
                fontSize = 13.sp,
                color = colorProfile.textSecondaryOnSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colorProfile.tagColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = colorProfile.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorProfile.textOnSurface
                    )
                }

                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
                    color = colorProfile.textSecondaryOnSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun EmptyNotesState(
    hasFilter: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (hasFilter) Icons.Default.SearchOff else Icons.Default.NoteAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasFilter) "ไม่พบผลลัพธ์การค้นหา" else "ยังไม่มีโน้ต",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasFilter) "ลองปรับคำค้นหาหรือตัวกรองสีใหม่อีกครั้ง" else "เริ่มจดไอเดีย งาน หรือรายการที่ต้องทำได้เลย",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (hasFilter) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("ล้างเงื่อนไขทั้งหมด", fontSize = 13.sp)
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "กดปุ่ม + มุมขวาล่างเพื่อเพิ่มโน้ตใหม่",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
