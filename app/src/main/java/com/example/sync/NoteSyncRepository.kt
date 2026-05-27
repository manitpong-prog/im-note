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
                val dto = note.toSupabaseDto(
                    userId = userId,
                    deviceId = deviceId
                )

                val response = api.upsertNote(
                    apiKey = SupabaseConfig.anonKey,
                    authorization = authorization,
                    note = dto
                )

                if (response.isSuccessful) {
                    val remoteNote = response.body()?.firstOrNull()
                    val remoteId = remoteNote?.id
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

    private fun Note.toSupabaseDto(userId: String, deviceId: String): SupabaseNoteDto {
        return SupabaseNoteDto(
            id = remoteId,
            userId = userId,
            localId = id,
            title = title,
            content = content,
            colorIndex = colorIndex,
            isPinned = isPinned,
            clientCreatedAt = createdAt.toIso8601(),
            clientUpdatedAt = updatedAt.toIso8601(),
            deletedAt = deletedAt?.toIso8601(),
            syncVersion = 1,
            deviceId = deviceId
        )
    }

    private fun Long.toIso8601(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date(this))
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
