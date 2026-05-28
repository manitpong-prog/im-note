package com.example.sync

import android.content.Context
import android.provider.Settings
import com.imnotesminimal.app.data.Note
import com.imnotesminimal.app.data.NoteRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NoteSyncRepository(
    private val context: Context,
    private val noteRepository: NoteRepository
) {
    suspend fun syncAll(
        userId: String?,
        accessToken: String?
    ): Result<SyncSummary> {
        val uploaded = uploadPendingNotes(userId, accessToken).getOrElse { return Result.failure(it) }
        val downloaded = pullRemoteNotes(userId, accessToken).getOrElse { return Result.failure(it) }
        return Result.success(SyncSummary(uploadedCount = uploaded, downloadedCount = downloaded))
    }

    suspend fun syncLoginAndReplaceLocalCache(
        userId: String?,
        accessToken: String?
    ): Result<SyncSummary> {
        val uploaded = uploadPendingNotes(userId, accessToken).getOrElse { return Result.failure(it) }
        noteRepository.deleteAllNotes()
        val downloaded = pullRemoteNotes(userId, accessToken).getOrElse { return Result.failure(it) }
        return Result.success(SyncSummary(uploadedCount = uploaded, downloadedCount = downloaded))
    }

    suspend fun uploadPendingNotes(
        userId: String?,
        accessToken: String?
    ): Result<Int> {
        if (userId.isNullOrBlank()) {
            return Result.failure(IllegalStateException("ยังไม่พบรหัสผู้ใช้สำหรับซิงค์"))
        }

        if (accessToken.isNullOrBlank()) {
            return Result.failure(IllegalStateException("ยังไม่พบ session สำหรับซิงค์ กรุณาเข้าสู่ระบบใหม่"))
        }

        if (!SupabaseConfig.isConfigured) {
            return Result.failure(IllegalStateException("ยังไม่ได้ตั้งค่า Supabase URL และ Anon Key"))
        }

        val api = SupabaseService.notesApi
            ?: return Result.failure(IllegalStateException("ไม่สามารถสร้าง Supabase Notes API ได้"))

        return try {
            val pendingNotes = noteRepository.getNotesNeedingSync()
            if (pendingNotes.isEmpty()) {
                return Result.success(0)
            }

            var syncedCount = 0
            val deviceId = getDeviceId()
            val authorization = SupabaseService.bearer(accessToken)

            for (note in pendingNotes) {
                val body = note.toSupabaseBody(
                    userId = userId,
                    deviceId = deviceId
                )

                val response = if (!note.remoteId.isNullOrBlank()) {
                    api.updateNoteByRemoteId(
                        apiKey = SupabaseConfig.anonKey,
                        authorization = authorization,
                        remoteIdFilter = "eq.${note.remoteId}",
                        note = body
                    )
                } else {
                    api.upsertNote(
                        apiKey = SupabaseConfig.anonKey,
                        authorization = authorization,
                        note = body
                    )
                }

                if (response.isSuccessful) {
                    val remoteNote = response.body()?.firstOrNull()
                    val remoteId = remoteNote?.id ?: note.remoteId
                    if (!remoteId.isNullOrBlank()) {
                        noteRepository.markNoteSynced(
                            localId = note.id,
                            remoteId = remoteId,
                            userId = userId
                        )
                        syncedCount += 1
                    }
                } else {
                    return Result.failure(
                        IllegalStateException(
                            toFriendlySyncError(response.code(), response.errorBody()?.string())
                        )
                    )
                }
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(toFriendlyNetworkError(e)))
        }
    }

    suspend fun pullRemoteNotes(
        userId: String?,
        accessToken: String?
    ): Result<Int> {
        if (userId.isNullOrBlank()) {
            return Result.failure(IllegalStateException("ยังไม่พบรหัสผู้ใช้สำหรับดึงโน้ตออนไลน์"))
        }

        if (accessToken.isNullOrBlank()) {
            return Result.failure(IllegalStateException("ยังไม่พบ session สำหรับดึงโน้ตออนไลน์ กรุณาเข้าสู่ระบบใหม่"))
        }

        if (!SupabaseConfig.isConfigured) {
            return Result.failure(IllegalStateException("ยังไม่ได้ตั้งค่า Supabase URL และ Anon Key"))
        }

        val api = SupabaseService.notesApi
            ?: return Result.failure(IllegalStateException("ไม่สามารถสร้าง Supabase Notes API ได้"))

        return try {
            val response = api.getNotes(
                apiKey = SupabaseConfig.anonKey,
                authorization = SupabaseService.bearer(accessToken)
            )

            if (!response.isSuccessful) {
                return Result.failure(
                    IllegalStateException(
                        toFriendlySyncError(response.code(), response.errorBody()?.string())
                    )
                )
            }

            val remoteNotes = response.body().orEmpty()
                .filter { !it.id.isNullOrBlank() }

            var changedCount = 0

            for (remote in remoteNotes) {
                val remoteId = remote.id ?: continue
                val local = noteRepository.getNoteByRemoteId(remoteId)
                val remoteUpdatedAt = remote.clientUpdatedAt?.toEpochMillisOrNull() ?: remote.updatedAt?.toEpochMillisOrNull() ?: System.currentTimeMillis()
                val remoteCreatedAt = remote.clientCreatedAt?.toEpochMillisOrNull() ?: remote.createdAt?.toEpochMillisOrNull() ?: remoteUpdatedAt
                val remoteDeletedAt = remote.deletedAt?.toEpochMillisOrNull()

                if (remoteDeletedAt != null) {
                    if (local != null && local.deletedAt == null && local.syncStatus != "PENDING") {
                        noteRepository.softDeleteNote(
                            localId = local.id,
                            deletedAt = remoteDeletedAt,
                            updatedAt = remoteUpdatedAt,
                            syncStatus = "SYNCED"
                        )
                        changedCount += 1
                    }
                    continue
                }

                if (local == null) {
                    noteRepository.insertNote(
                        Note(
                            title = remote.title,
                            content = remote.content,
                            colorIndex = remote.colorIndex,
                            isPinned = remote.isPinned,
                            createdAt = remoteCreatedAt,
                            updatedAt = remoteUpdatedAt,
                            userId = remote.userId,
                            remoteId = remoteId,
                            syncStatus = "SYNCED",
                            deletedAt = null
                        )
                    )
                    changedCount += 1
                } else if (local.syncStatus != "PENDING" && remoteUpdatedAt > local.updatedAt) {
                    noteRepository.updateNote(
                        local.copy(
                            title = remote.title,
                            content = remote.content,
                            colorIndex = remote.colorIndex,
                            isPinned = remote.isPinned,
                            updatedAt = remoteUpdatedAt,
                            userId = remote.userId,
                            remoteId = remoteId,
                            syncStatus = "SYNCED",
                            deletedAt = null
                        )
                    )
                    changedCount += 1
                }
            }

            Result.success(changedCount)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(toFriendlyNetworkError(e)))
        }
    }

    suspend fun permanentlyDeleteRemoteNote(
        remoteId: String?,
        accessToken: String?
    ): Result<Unit> {
        if (remoteId.isNullOrBlank()) {
            return Result.success(Unit)
        }

        if (accessToken.isNullOrBlank()) {
            return Result.failure(IllegalStateException("ยังไม่พบ session สำหรับลบโน้ตออนไลน์ กรุณาเข้าสู่ระบบใหม่"))
        }

        if (!SupabaseConfig.isConfigured) {
            return Result.failure(IllegalStateException("ยังไม่ได้ตั้งค่า Supabase URL และ Anon Key"))
        }

        val api = SupabaseService.notesApi
            ?: return Result.failure(IllegalStateException("ไม่สามารถสร้าง Supabase Notes API ได้"))

        return try {
            val response = api.deleteNoteByRemoteId(
                apiKey = SupabaseConfig.anonKey,
                authorization = SupabaseService.bearer(accessToken),
                remoteIdFilter = "eq.$remoteId"
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException(toFriendlySyncError(response.code(), response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException(toFriendlyNetworkError(e)))
        }
    }

    private fun Note.toSupabaseBody(userId: String, deviceId: String): Map<String, Any?> {
        val body = linkedMapOf<String, Any?>(
            "user_id" to userId,
            "local_id" to id,
            "title" to title,
            "content" to content,
            "color_index" to colorIndex,
            "is_pinned" to isPinned,
            "client_created_at" to createdAt.toIso8601(),
            "client_updated_at" to updatedAt.toIso8601(),
            "deleted_at" to deletedAt?.toIso8601(),
            "sync_version" to 1,
            "device_id" to deviceId
        )

        if (!remoteId.isNullOrBlank()) {
            body["id"] = remoteId
        }

        return body
    }

    private fun Long.toIso8601(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date(this))
    }

    private fun String.toEpochMillisOrNull(): Long? {
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX"
        )

        for (pattern in patterns) {
            try {
                val formatter = SimpleDateFormat(pattern, Locale.US)
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                return formatter.parse(this)?.time
            } catch (_: Exception) {
                // Try next format.
            }
        }

        return null
    }

    private fun getDeviceId(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return "android-${androidId ?: "unknown"}"
    }

    private fun toFriendlySyncError(statusCode: Int, rawBody: String?): String {
        val raw = rawBody.orEmpty().lowercase()
        return when {
            raw.contains("row-level security") || raw.contains("rls") ->
                "ซิงค์ไม่สำเร็จ เพราะสิทธิ์เข้าถึงข้อมูลไม่ถูกต้อง กรุณาเข้าสู่ระบบใหม่"

            raw.contains("jwt") || raw.contains("token") || statusCode == 401 || statusCode == 403 ->
                "session หมดอายุหรือไม่มีสิทธิ์ซิงค์ กรุณาเข้าสู่ระบบใหม่"

            statusCode >= 500 ->
                "เซิร์ฟเวอร์ซิงค์มีปัญหาชั่วคราว กรุณาลองใหม่อีกครั้ง"

            else ->
                "ซิงค์โน้ตไม่สำเร็จ กรุณาลองใหม่อีกครั้ง"
        }
    }

    private fun toFriendlyNetworkError(error: Exception): String {
        val message = error.message.orEmpty().lowercase()
        return when {
            message.contains("unable to resolve host") || message.contains("failed to connect") ->
                "เชื่อมต่ออินเทอร์เน็ตหรือ Supabase ไม่ได้ กรุณาตรวจสอบการเชื่อมต่อ"

            message.contains("timeout") ->
                "การซิงค์ใช้เวลานานเกินไป กรุณาลองใหม่อีกครั้ง"

            else ->
                "เกิดข้อผิดพลาดระหว่างซิงค์ กรุณาลองใหม่อีกครั้ง"
        }
    }
}

data class SyncSummary(
    val uploadedCount: Int,
    val downloadedCount: Int
)
