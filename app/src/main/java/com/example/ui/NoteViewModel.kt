package com.example.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Note
import com.example.data.NoteRepository
import com.example.data.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortMode(val displayNameTh: String) {
    EDITED_DESC("แก้ไขล่าสุด (ใหม่ -> เก่า)"),
    EDITED_ASC("แก้ไขล่าสุด (เก่า -> ใหม่)"),
    ALPHA_ASC("ตามตัวอักษร (A-Z / ก-ฮ)"),
    ALPHA_DESC("ตามตัวอักษร (Z-A / ฮ-ก)")
}

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    private val sharedPrefs: SharedPreferences = application.getSharedPreferences("im_notes_prefs", Context.MODE_PRIVATE)

    // Auth flows
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    // Settings flows
    private val _autoSync = MutableStateFlow(true)
    val autoSync: StateFlow<Boolean> = _autoSync.asStateFlow()

    private val _wifiOnly = MutableStateFlow(false)
    val wifiOnly: StateFlow<Boolean> = _wifiOnly.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _pinNewDefault = MutableStateFlow(false)
    val pinNewDefault: StateFlow<Boolean> = _pinNewDefault.asStateFlow()

    // In-memory UI control states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortMode = MutableStateFlow(SortMode.EDITED_DESC)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _selectedColorFilter = MutableStateFlow<Int?>(null) // null = all colors
    val selectedColorFilter: StateFlow<Int?> = _selectedColorFilter.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = NoteRepository(database.noteDao)
        loadSavedPreferences()
        seedSampleNotes()
    }

    private fun loadSavedPreferences() {
        // Load settings values
        _autoSync.value = sharedPrefs.getBoolean("auto_sync", true)
        _wifiOnly.value = sharedPrefs.getBoolean("wifi_only", false)
        _isDarkTheme.value = sharedPrefs.getBoolean("dark_theme", false)
        _pinNewDefault.value = sharedPrefs.getBoolean("pin_default", false)
        _lastSyncTime.value = sharedPrefs.getLong("last_sync_time", 0L)

        // Load logged in user if any
        val email = sharedPrefs.getString("logged_email", null)
        val name = sharedPrefs.getString("logged_name", null)
        val type = sharedPrefs.getString("logged_type", null)
        
        if (email != null && name != null) {
            _currentUser.value = User(
                email = email,
                displayName = name,
                imageUrl = if (type == "GOOGLE") "G" else null,
                accountType = type ?: "EMAIL"
            )
        }
    }

    private fun seedSampleNotes() {
        viewModelScope.launch {
            try {
                // Room Flow emits current db content immediately
                val currentNotes = repository.allNotesFlow.first()
                if (currentNotes.isEmpty()) {
                    val currentTime = System.currentTimeMillis()
                    val sampleNotes = listOf(
                        Note(
                            title = "ช้อปปิ้งของเข้าบ้าน 🛒",
                            content = "1. นมจืดสองกล่อง\n2. ไข่ไก่ 1 แผง\n3. ผักกาดขาวและแครอท\n4. อกไก่หมักพริกไทยดำ 1 กิโลกรัม",
                            colorIndex = 0, // Yellow
                            isPinned = true,
                            createdAt = currentTime - 3600000 * 24, // 1 day ago
                            updatedAt = currentTime - 3600000 * 12  // 12 hours ago
                        ),
                        Note(
                            title = "วางแผนท่องเที่ยวเชียงใหม่ ✈️",
                            content = "วันแรก: ดอยสุเทพ, นิมมานฯ ชิลคาเฟ่ยอดนิยม\nวันที่สอง: ม่อนแจ่ม ดูทะเลหมอกยามเช้า\nวันที่สาม: ซื้อไส้อั่ว น้ำพริกหนุ่มตลาดวโรรส",
                            colorIndex = 1, // Blue
                            isPinned = false,
                            createdAt = currentTime - 3600000 * 48, // 2 days ago
                            updatedAt = currentTime - 3600000 * 24  // 1 day ago
                        ),
                        Note(
                            title = "บันทึกไอเดียแอปพยากรณ์อากาศ ☀️",
                            content = "ดีไซน์หน้าตาแบบเรียบหรู ปล่อยอนิเมชันตามสภาพอากาศจริง\nดึงข้อมูลสภาพอากาศจาก OpenWeatherMap API\nเพิ่มกล่องแจ้งเตือนฝนถล่มล่วงหน้าและเสื้อฝนที่แนะนำ",
                            colorIndex = 2, // Green
                            isPinned = false,
                            createdAt = currentTime - 3600000 * 10, // 10 hours ago
                            updatedAt = currentTime - 3600000 * 2   // 2 hours ago
                        ),
                        Note(
                            title = "ตารางออกกำลังกายประจำสัปดาห์ 🏃‍♂️",
                            content = "จันทร์: คาร์ดิโอสลับเดินเร็ว\nพุธ: เวทเทรนนิ่งแกนกลางลำตัว\nศุกร์: โยคะเพื่อสุขภาพและหลังตึงตึง",
                            colorIndex = 3, // Red
                            isPinned = true,
                            createdAt = currentTime - 3600000 * 2, // 2 hours ago
                            updatedAt = currentTime                 // Just now
                        ),
                        Note(
                            title = "สูตรทำบราวนี่ช็อกโกแลตหน้าฟิล์ม 🍫",
                            content = "แป้งเค้กอเนกประสงค์ 100g, ผงโกโก้เกรดพรีเมียม 60g, เนยละลาย 120g และน้ำตาลพาสเทล 150g\nอบอุณหภูมิ 175 C ควบคุมเวลาเป๊ะๆ 20 นาทีถ้วนเด็ดขาด",
                            colorIndex = 5, // Orange
                            isPinned = false,
                            createdAt = currentTime - 3600000 * 72, // 3 days ago
                            updatedAt = currentTime - 3600000 * 48  // 2 days ago
                        )
                    )
                    for (note in sampleNotes) {
                        repository.insertNote(note)
                    }
                }
            } catch (e: Exception) {
                // Fail silently
            }
        }
    }

    // Combined Flow: filter search, filter color, and sort dynamically
    // Pinned notes are ALWAYS positioned on the top layout, sorted within their group.
    val filteredAndSortedNotes: StateFlow<List<Note>> = combine(
        repository.allNotesFlow,
        _searchQuery,
        _sortMode,
        _selectedColorFilter
    ) { notes, query, sort, colorId ->
        var result = notes

        // 1. Filter by color index if any is chosen
        if (colorId != null) {
            result = result.filter { it.colorIndex == colorId }
        }

        // 2. Dynamic text search (case-insensitive) on title or contents
        if (query.isNotBlank()) {
            result = result.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }

        // 3. Separate into Pinned VS Unpinned groups
        val (pinned, unpinned) = result.partition { it.isPinned }

        // 4. Common comparator based on SortMode
        val comparator = when (sort) {
            SortMode.EDITED_DESC -> compareByDescending<Note> { it.updatedAt }
            SortMode.EDITED_ASC -> compareBy<Note> { it.updatedAt }
            SortMode.ALPHA_ASC -> compareBy { it.title.lowercase() }
            SortMode.ALPHA_DESC -> compareByDescending { it.title.lowercase() }
        }

        val sortedPinned = pinned.sortedWith(comparator)
        val sortedUnpinned = unpinned.sortedWith(comparator)

        // Concatenate pinned entries before unpinned entries
        sortedPinned + sortedUnpinned
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Setters for UI controls
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun setColorFilter(colorId: Int?) {
        _selectedColorFilter.value = colorId
    }

    // Settings modifiers
    fun setAutoSync(enabled: Boolean) {
        _autoSync.value = enabled
        sharedPrefs.edit().putBoolean("auto_sync", enabled).apply()
    }

    fun setWifiOnly(enabled: Boolean) {
        _wifiOnly.value = enabled
        sharedPrefs.edit().putBoolean("wifi_only", enabled).apply()
    }

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        sharedPrefs.edit().putBoolean("dark_theme", enabled).apply()
    }

    fun setPinNewDefault(enabled: Boolean) {
        _pinNewDefault.value = enabled
        sharedPrefs.edit().putBoolean("pin_default", enabled).apply()
    }

    // Simulated Authentication Mechanics (Local database inside Preferences)
    fun registerWithEmail(emailInput: String, passwordInput: String, nameInput: String, onResult: (Boolean, String) -> Unit) {
        val email = emailInput.trim().lowercase()
        val password = passwordInput.trim()
        val displayName = nameInput.trim()

        if (email.isBlank() || password.length < 6 || displayName.isBlank()) {
            onResult(false, "กรุณากรอกข้อมูลให้ครบถ้วน ความยาวรหัสผ่าน 6 ตัวอักษรขึ้นไป")
            return
        }

        // Check if existing user exists
        val existsKey = "user_pwd_$email"
        if (sharedPrefs.contains(existsKey)) {
            onResult(false, "อีเมลนี้ผ่านการสมัครใช้งานไปแล้ว กรุณาเข้าสู่ระบบ")
            return
        }

        // Store credentials
        sharedPrefs.edit()
            .putString("user_pwd_$email", password)
            .putString("user_name_$email", displayName)
            .apply()

        // Auto authenticate on registration complete
        authenticateUser(email, password) { success, msg ->
            onResult(success, if (success) "สมัครสมาชิกและเข้าสู่ระบบสำเร็จ!" else msg)
        }
    }

    fun authenticateUser(emailInput: String, passwordInput: String, onResult: (Boolean, String) -> Unit) {
        val email = emailInput.trim().lowercase()
        val password = passwordInput.trim()

        val savedPassword = sharedPrefs.getString("user_pwd_$email", null)
        val savedName = sharedPrefs.getString("user_name_$email", null)

        if (savedPassword == null || savedName == null) {
            onResult(false, "ไม่พบบัญชีผู้ใช้ ตรวจสอบอีเมลหรือสมัครใช้งานใหม่")
            return
        }

        if (savedPassword != password) {
            onResult(false, "รหัสผ่านไม่ถูกต้อง กรุมลั่นลองใหม่อีกครั้ง")
            return
        }

        // Establish session
        val loggedUser = User(
            email = email,
            displayName = savedName,
            imageUrl = null,
            accountType = "EMAIL"
        )
        
        sharedPrefs.edit()
            .putString("logged_email", email)
            .putString("logged_name", savedName)
            .putString("logged_type", "EMAIL")
            .apply()

        _currentUser.value = loggedUser
        onResult(true, "เข้าสู่ระบบเสร็จสิ้น!")
        triggerSimulatedCloudSync()
    }

    fun authenticateWithGoogle(emailInput: String, nameInput: String) {
        val email = emailInput.trim().lowercase()
        val displayName = nameInput.trim()

        val loggedUser = User(
            email = email,
            displayName = displayName,
            imageUrl = "G",
            accountType = "GOOGLE"
        )

        sharedPrefs.edit()
            .putString("logged_email", email)
            .putString("logged_name", displayName)
            .putString("logged_type", "GOOGLE")
            .apply()

        _currentUser.value = loggedUser
        triggerSimulatedCloudSync()
    }

    fun signOutUser() {
        sharedPrefs.edit()
            .remove("logged_email")
            .remove("logged_name")
            .remove("logged_type")
            .apply()
        _currentUser.value = null
        _lastSyncTime.value = 0L
        sharedPrefs.edit().remove("last_sync_time").apply()
    }

    fun deleteUserAccount(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                // Delete all notes from DB
                repository.deleteAllNotes()
                
                // Clear active logged credentials
                signOutUser()
                
                // Nuke all registered profile info & preferences
                sharedPrefs.edit().clear().apply()
                
                // Reload clean variables default values
                loadSavedPreferences()
                
                onComplete()
            } catch (e: Exception) {
                // Fail silently
            }
        }
    }

    // Cloud Synchronization Service Simulation
    // When Note modifications occur, trigger automatic sync if user profile matches
    fun triggerSimulatedCloudSync() {
        val user = _currentUser.value
        if (user != null && _autoSync.value) {
            viewModelScope.launch {
                try {
                    _isSyncing.value = true
                    delay(1500) // Simulated network handshaking
                    val currentTime = System.currentTimeMillis()
                    _lastSyncTime.value = currentTime
                    sharedPrefs.edit().putLong("last_sync_time", currentTime).apply()
                } catch (e: Exception) {
                    // Safe network failure failover
                } finally {
                    _isSyncing.value = false
                }
            }
        }
    }

    // Database CRUD actions executed in background routines
    fun saveNote(noteId: Int?, title: String, content: String, colorIndex: Int, isPinned: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val titleText = title.ifBlank { "ไม่ได้ตั้งชื่อบันทึก" }
            if (noteId == null) {
                // Insert a fresh new Note
                val newNote = Note(
                    title = titleText,
                    content = content,
                    colorIndex = colorIndex,
                    isPinned = isPinned,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                repository.insertNote(newNote)
            } else {
                // Update an existing Note
                val existingNote = repository.getNoteById(noteId)
                if (existingNote != null) {
                    val updatedNote = existingNote.copy(
                        title = titleText,
                        content = content,
                        colorIndex = colorIndex,
                        isPinned = isPinned,
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateNote(updatedNote)
                }
            }
            triggerSimulatedCloudSync()
            onComplete()
        }
    }

    suspend fun getNoteById(noteId: Int): Note? {
        return repository.getNoteById(noteId)
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
            triggerSimulatedCloudSync()
        }
    }

    fun togglePinState(note: Note) {
        viewModelScope.launch {
            val updated = note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis())
            repository.updateNote(updated)
            triggerSimulatedCloudSync()
        }
    }

    fun changeNoteColor(note: Note, newColorIndex: Int) {
        viewModelScope.launch {
            val updated = note.copy(colorIndex = newColorIndex, updatedAt = System.currentTimeMillis())
            repository.updateNote(updated)
            triggerSimulatedCloudSync()
        }
    }
}
