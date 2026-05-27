package com.imnotesminimal.app.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sync.NoteSyncRepository
import com.example.sync.SupabaseAuthRepository
import com.imnotesminimal.app.data.AppDatabase
import com.imnotesminimal.app.data.Note
import com.imnotesminimal.app.data.NoteRepository
import com.imnotesminimal.app.data.User
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
    private val syncRepository: NoteSyncRepository
    private val authRepository = SupabaseAuthRepository()
    private val sharedPrefs: SharedPreferences = application.getSharedPreferences("im_notes_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _autoSync = MutableStateFlow(true)
    val autoSync: StateFlow<Boolean> = _autoSync.asStateFlow()

    private val _wifiOnly = MutableStateFlow(false)
    val wifiOnly: StateFlow<Boolean> = _wifiOnly.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _pinNewDefault = MutableStateFlow(false)
    val pinNewDefault: StateFlow<Boolean> = _pinNewDefault.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortMode = MutableStateFlow(SortMode.EDITED_DESC)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _selectedColorFilter = MutableStateFlow<Int?>(null)
    val selectedColorFilter: StateFlow<Int?> = _selectedColorFilter.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = NoteRepository(database.noteDao)
        syncRepository = NoteSyncRepository(application.applicationContext, repository)
        loadSavedPreferences()
        seedSampleNotes()
    }

    private fun loadSavedPreferences() {
        _autoSync.value = sharedPrefs.getBoolean("auto_sync", true)
        _wifiOnly.value = sharedPrefs.getBoolean("wifi_only", false)
        _isDarkTheme.value = sharedPrefs.getBoolean("dark_theme", false)
        _pinNewDefault.value = sharedPrefs.getBoolean("pin_default", false)
        _lastSyncTime.value = sharedPrefs.getLong("last_sync_time", 0L)

        val userId = sharedPrefs.getString("logged_user_id", null)
        val email = sharedPrefs.getString("logged_email", null)
        val name = sharedPrefs.getString("logged_name", null)
        val type = sharedPrefs.getString("logged_type", null)

        if (email != null && name != null) {
            _currentUser.value = User(
                id = userId,
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
                val currentNotes = repository.allNotesFlow.first()
                if (currentNotes.isEmpty()) {
                    val currentTime = System.currentTimeMillis()
                    val sampleNotes = listOf(
                        Note(
                            title = "ช้อปปิ้งของเข้าบ้าน 🛒",
                            content = "1. นมจืดสองกล่อง\n2. ไข่ไก่ 1 แผง\n3. ผักกาดขาวและแครอท\n4. อกไก่หมักพริกไทยดำ 1 กิโลกรัม",
                            colorIndex = 0,
                            isPinned = true,
                            createdAt = currentTime - 3600000 * 24,
                            updatedAt = currentTime - 3600000 * 12
                        ),
                        Note(
                            title = "วางแผนท่องเที่ยวเชียงใหม่ ✈️",
                            content = "วันแรก: ดอยสุเทพ, นิมมานฯ ชิลคาเฟ่ยอดนิยม\nวันที่สอง: ม่อนแจ่ม ดูทะเลหมอกยามเช้า\nวันที่สาม: ซื้อไส้อั่ว น้ำพริกหนุ่มตลาดวโรรส",
                            colorIndex = 1,
                            isPinned = false,
                            createdAt = currentTime - 3600000 * 48,
                            updatedAt = currentTime - 3600000 * 24
                        ),
                        Note(
                            title = "บันทึกไอเดียแอปพยากรณ์อากาศ ☀️",
                            content = "ดีไซน์หน้าตาแบบเรียบหรู ปล่อยอนิเมชันตามสภาพอากาศจริง\nดึงข้อมูลสภาพอากาศจาก OpenWeatherMap API\nเพิ่มกล่องแจ้งเตือนฝนล่วงหน้าและเสื้อฝนที่แนะนำ",
                            colorIndex = 2,
                            isPinned = false,
                            createdAt = currentTime - 3600000 * 10,
                            updatedAt = currentTime - 3600000 * 2
                        ),
                        Note(
                            title = "ตารางออกกำลังกายประจำสัปดาห์ 🏃‍♂️",
                            content = "จันทร์: คาร์ดิโอสลับเดินเร็ว\nพุธ: เวทเทรนนิ่งแกนกลางลำตัว\nศุกร์: โยคะเพื่อสุขภาพและหลังตึง",
                            colorIndex = 3,
                            isPinned = true,
                            createdAt = currentTime - 3600000 * 2,
                            updatedAt = currentTime
                        ),
                        Note(
                            title = "สูตรทำบราวนี่ช็อกโกแลตหน้าฟิล์ม 🍫",
                            content = "แป้งเค้กอเนกประสงค์ 100g, ผงโกโก้ 60g, เนยละลาย 120g และน้ำตาล 150g\nอบอุณหภูมิ 175 C ประมาณ 20 นาที",
                            colorIndex = 5,
                            isPinned = false,
                            createdAt = currentTime - 3600000 * 72,
                            updatedAt = currentTime - 3600000 * 48
                        )
                    )
                    for (note in sampleNotes) {
                        repository.insertNote(note)
                    }
                }
            } catch (e: Exception) {
                // Keep the app usable even if sample-note seeding fails.
            }
        }
    }

    val filteredAndSortedNotes: StateFlow<List<Note>> = combine(
        repository.allNotesFlow,
        _searchQuery,
        _sortMode,
        _selectedColorFilter
    ) { notes, query, sort, colorId ->
        var result = notes

        if (colorId != null) {
            result = result.filter { it.colorIndex == colorId }
        }

        if (query.isNotBlank()) {
            result = result.filter {
                it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
            }
        }

        val (pinned, unpinned) = result.partition { it.isPinned }
        val comparator = when (sort) {
            SortMode.EDITED_DESC -> compareByDescending<Note> { it.updatedAt }
            SortMode.EDITED_ASC -> compareBy<Note> { it.updatedAt }
            SortMode.ALPHA_ASC -> compareBy { it.title.lowercase() }
            SortMode.ALPHA_DESC -> compareByDescending { it.title.lowercase() }
        }

        pinned.sortedWith(comparator) + unpinned.sortedWith(comparator)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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

    fun registerWithEmail(emailInput: String, passwordInput: String, nameInput: String, onResult: (Boolean, String) -> Unit) {
        val email = emailInput.trim().lowercase()
        val password = passwordInput.trim()
        val displayName = nameInput.trim()

        if (email.isBlank() || password.length < 6 || displayName.isBlank()) {
            onResult(false, "กรุณากรอกข้อมูลให้ครบถ้วน และตั้งรหัสผ่านอย่างน้อย 6 ตัวอักษร")
            return
        }

        viewModelScope.launch {
            val result = authRepository.signUp(email, password, displayName)
            result.fold(
                onSuccess = { (user, session) ->
                    saveLoggedInUser(
                        user = user,
                        accessToken = session?.accessToken,
                        refreshToken = session?.refreshToken
                    )
                    onResult(true, "สมัครสมาชิกสำเร็จ")
                    triggerCloudSync()
                },
                onFailure = { error ->
                    onResult(false, error.message ?: "สมัครสมาชิกไม่สำเร็จ")
                }
            )
        }
    }

    fun authenticateUser(emailInput: String, passwordInput: String, onResult: (Boolean, String) -> Unit) {
        val email = emailInput.trim().lowercase()
        val password = passwordInput.trim()

        if (email.isBlank() || password.isBlank()) {
            onResult(false, "กรุณากรอกอีเมลและรหัสผ่าน")
            return
        }

        viewModelScope.launch {
            val result = authRepository.signIn(email, password)
            result.fold(
                onSuccess = { (user, session) ->
                    saveLoggedInUser(
                        user = user,
                        accessToken = session.accessToken,
                        refreshToken = session.refreshToken
                    )
                    onResult(true, "เข้าสู่ระบบสำเร็จ")
                    triggerCloudSync()
                },
                onFailure = { error ->
                    onResult(false, error.message ?: "เข้าสู่ระบบไม่สำเร็จ")
                }
            )
        }
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

        saveLoggedInUser(loggedUser, accessToken = null, refreshToken = null)
        triggerCloudSync()
    }

    private fun saveLoggedInUser(user: User, accessToken: String?, refreshToken: String?) {
        sharedPrefs.edit()
            .putString("logged_user_id", user.id)
            .putString("logged_email", user.email)
            .putString("logged_name", user.displayName)
            .putString("logged_type", user.accountType)
            .putString("supabase_access_token", accessToken)
            .putString("supabase_refresh_token", refreshToken)
            .apply()
        _currentUser.value = user
    }

    fun signOutUser() {
        sharedPrefs.edit()
            .remove("logged_user_id")
            .remove("logged_email")
            .remove("logged_name")
            .remove("logged_type")
            .remove("supabase_access_token")
            .remove("supabase_refresh_token")
            .remove("last_sync_time")
            .apply()
        _currentUser.value = null
        _lastSyncTime.value = 0L
    }

    fun deleteUserAccount(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteAllNotes()
                signOutUser()
                sharedPrefs.edit().clear().apply()
                loadSavedPreferences()
                onComplete()
            } catch (e: Exception) {
                // Keep the UI stable if deletion fails.
            }
        }
    }

    fun triggerSimulatedCloudSync() {
        triggerCloudSync()
    }

    private fun triggerCloudSync() {
        val user = _currentUser.value
        val accessToken = sharedPrefs.getString("supabase_access_token", null)

        if (user == null || !_autoSync.value) {
            return
        }

        viewModelScope.launch {
            try {
                _isSyncing.value = true
                val result = syncRepository.uploadPendingNotes(
                    userId = user.id,
                    accessToken = accessToken
                )

                if (result.isSuccess) {
                    val currentTime = System.currentTimeMillis()
                    _lastSyncTime.value = currentTime
                    sharedPrefs.edit().putLong("last_sync_time", currentTime).apply()
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun saveNote(noteId: Int?, title: String, content: String, colorIndex: Int, isPinned: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val titleText = title.ifBlank { "ไม่ได้ตั้งชื่อบันทึก" }
            if (noteId == null) {
                val newNote = Note(
                    title = titleText,
                    content = content,
                    colorIndex = colorIndex,
                    isPinned = isPinned || _pinNewDefault.value,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    userId = _currentUser.value?.id,
                    syncStatus = if (_currentUser.value != null) "PENDING" else "LOCAL"
                )
                repository.insertNote(newNote)
            } else {
                val existingNote = repository.getNoteById(noteId)
                if (existingNote != null) {
                    val updatedNote = existingNote.copy(
                        title = titleText,
                        content = content,
                        colorIndex = colorIndex,
                        isPinned = isPinned,
                        updatedAt = System.currentTimeMillis(),
                        userId = existingNote.userId ?: _currentUser.value?.id,
                        syncStatus = if (_currentUser.value != null) "PENDING" else existingNote.syncStatus
                    )
                    repository.updateNote(updatedNote)
                }
            }
            triggerCloudSync()
            onComplete()
        }
    }

    suspend fun getNoteById(noteId: Int): Note? {
        return repository.getNoteById(noteId)
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
            triggerCloudSync()
        }
    }

    fun togglePinState(note: Note) {
        viewModelScope.launch {
            val updated = note.copy(
                isPinned = !note.isPinned,
                updatedAt = System.currentTimeMillis(),
                syncStatus = if (_currentUser.value != null) "PENDING" else note.syncStatus
            )
            repository.updateNote(updated)
            triggerCloudSync()
        }
    }

    fun changeNoteColor(note: Note, newColorIndex: Int) {
        viewModelScope.launch {
            val updated = note.copy(
                colorIndex = newColorIndex,
                updatedAt = System.currentTimeMillis(),
                syncStatus = if (_currentUser.value != null) "PENDING" else note.syncStatus
            )
            repository.updateNote(updated)
            triggerCloudSync()
        }
    }
}
