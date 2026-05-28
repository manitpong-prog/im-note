package com.example.sync

import android.net.Uri
import com.imnotesminimal.app.data.User

class SupabaseAuthRepository {
    private val googleRedirectUri = "com.imnotesminimal.app://login-callback"

    fun buildGoogleOAuthUrl(): String {
        if (!SupabaseConfig.isConfigured) return ""

        return Uri.parse("${SupabaseConfig.url}/auth/v1/authorize")
            .buildUpon()
            .appendQueryParameter("provider", "google")
            .appendQueryParameter("redirect_to", googleRedirectUri)
            .build()
            .toString()
    }

    suspend fun deleteAccount(accessToken: String?): Result<Unit> {
        if (accessToken.isNullOrBlank()) {
            return Result.failure(IllegalStateException("ยังไม่พบ session สำหรับลบบัญชี กรุณาเข้าสู่ระบบใหม่"))
        }

        if (!SupabaseConfig.isConfigured) {
            return Result.failure(IllegalStateException("ยังไม่ได้ตั้งค่า Supabase URL และ Anon Key ใน local.properties"))
        }

        val api = SupabaseService.authApi
            ?: return Result.failure(IllegalStateException("ไม่สามารถสร้าง Supabase Auth API ได้"))

        return try {
            val response = api.deleteAccount(
                apiKey = SupabaseConfig.anonKey,
                authorization = SupabaseService.bearer(accessToken)
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val message = response.body()?.error
                    ?: response.errorBody()?.string()
                    ?: "ลบบัญชีไม่สำเร็จ กรุณาลองใหม่อีกครั้ง"
                Result.failure(IllegalStateException(toFriendlyDeleteAccountError(response.code(), message)))
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException(toFriendlyNetworkError(e)))
        }
    }

    suspend fun createGoogleSessionFromCallback(callbackUri: Uri): Result<Pair<User, SupabaseAuthSession>> {
        val fragmentUri = Uri.parse("scheme://host?${callbackUri.fragment.orEmpty()}")
        val accessToken = callbackUri.getQueryParameter("access_token")
            ?: fragmentUri.getQueryParameter("access_token")
        val refreshToken = callbackUri.getQueryParameter("refresh_token")
            ?: fragmentUri.getQueryParameter("refresh_token")
        val expiresIn = callbackUri.getQueryParameter("expires_in")?.toLongOrNull()
            ?: fragmentUri.getQueryParameter("expires_in")?.toLongOrNull()
        val error = callbackUri.getQueryParameter("error_description")
            ?: callbackUri.getQueryParameter("error")
            ?: fragmentUri.getQueryParameter("error_description")
            ?: fragmentUri.getQueryParameter("error")

        if (!error.isNullOrBlank()) {
            return Result.failure(IllegalStateException(error))
        }

        if (accessToken.isNullOrBlank()) {
            return Result.failure(IllegalStateException("ไม่ได้รับ access token จาก Google Login"))
        }

        if (!SupabaseConfig.isConfigured) {
            return Result.failure(IllegalStateException("ยังไม่ได้ตั้งค่า Supabase URL และ Anon Key ใน local.properties"))
        }

        val api = SupabaseService.authApi
            ?: return Result.failure(IllegalStateException("ไม่สามารถสร้าง Supabase Auth API ได้"))

        return try {
            val response = api.getUser(
                apiKey = SupabaseConfig.anonKey,
                authorization = SupabaseService.bearer(accessToken)
            )

            if (response.isSuccessful) {
                val authUser = response.body()
                    ?: return Result.failure(IllegalStateException("Google Login สำเร็จแต่ไม่ได้รับข้อมูลผู้ใช้"))

                val email = authUser.email ?: "google-user@imnotesminimal.local"
                val displayName = authUser.userMetadata?.get("full_name") as? String
                    ?: authUser.userMetadata?.get("name") as? String
                    ?: email.substringBefore('@').ifBlank { "Google User" }

                val session = SupabaseAuthSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresIn = expiresIn,
                    tokenType = "bearer",
                    user = authUser
                )

                Result.success(
                    User(
                        id = authUser.id,
                        email = email,
                        displayName = displayName,
                        imageUrl = "G",
                        accountType = "GOOGLE"
                    ) to session
                )
            } else {
                Result.failure(IllegalStateException(toFriendlyAuthError(response.code(), response.errorBody()?.string(), isRegister = false)))
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException(toFriendlyNetworkError(e)))
        }
    }

    suspend fun signUp(email: String, password: String, displayName: String): Result<Pair<User, SupabaseAuthSession?>> {
        if (!SupabaseConfig.isConfigured) {
            return Result.failure(IllegalStateException("ยังไม่ได้ตั้งค่า Supabase URL และ Anon Key ใน local.properties"))
        }

        val api = SupabaseService.authApi
            ?: return Result.failure(IllegalStateException("ไม่สามารถสร้าง Supabase Auth API ได้"))

        return try {
            val response = api.signUp(
                apiKey = SupabaseConfig.anonKey,
                body = SupabaseAuthRequest(
                    email = email,
                    password = password,
                    data = mapOf("display_name" to displayName)
                )
            )

            if (response.isSuccessful) {
                val session = response.body()
                val authUser = session?.user
                Result.success(
                    User(
                        id = authUser?.id,
                        email = authUser?.email ?: email,
                        displayName = displayName,
                        accountType = "EMAIL"
                    ) to session
                )
            } else {
                Result.failure(IllegalStateException(toFriendlyAuthError(response.code(), response.errorBody()?.string(), isRegister = true)))
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException(toFriendlyNetworkError(e)))
        }
    }

    suspend fun signIn(email: String, password: String): Result<Pair<User, SupabaseAuthSession>> {
        if (!SupabaseConfig.isConfigured) {
            return Result.failure(IllegalStateException("ยังไม่ได้ตั้งค่า Supabase URL และ Anon Key ใน local.properties"))
        }

        val api = SupabaseService.authApi
            ?: return Result.failure(IllegalStateException("ไม่สามารถสร้าง Supabase Auth API ได้"))

        return try {
            val response = api.signInWithPassword(
                apiKey = SupabaseConfig.anonKey,
                body = SupabaseAuthRequest(email = email, password = password)
            )

            if (response.isSuccessful) {
                val session = response.body()
                    ?: return Result.failure(IllegalStateException("เข้าสู่ระบบสำเร็จแต่ไม่ได้รับ session จาก Supabase"))

                val authUser = session.user
                val userEmail = authUser?.email ?: email
                val displayName = userEmail.substringBefore('@').ifBlank { "iM Notes User" }

                Result.success(
                    User(
                        id = authUser?.id,
                        email = userEmail,
                        displayName = displayName,
                        accountType = "EMAIL"
                    ) to session
                )
            } else {
                Result.failure(IllegalStateException(toFriendlyAuthError(response.code(), response.errorBody()?.string(), isRegister = false)))
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException(toFriendlyNetworkError(e)))
        }
    }

    private fun toFriendlyDeleteAccountError(statusCode: Int, rawBody: String?): String {
        val raw = rawBody.orEmpty().lowercase()
        return when {
            raw.contains("invalid user session") || statusCode == 401 || statusCode == 403 ->
                "session หมดอายุหรือไม่มีสิทธิ์ลบบัญชี กรุณาเข้าสู่ระบบใหม่"

            raw.contains("missing supabase_service_role_key") || raw.contains("missing supabase_secret_keys") || raw.contains("missing") ->
                "ยังไม่ได้ตั้งค่า secret key สำหรับ Edge Function delete-account"

            statusCode == 404 ->
                "ยังไม่ได้ deploy Edge Function delete-account ใน Supabase"

            statusCode >= 500 ->
                "ระบบลบบัญชีมีปัญหาชั่วคราว กรุณาลองใหม่อีกครั้ง"

            else -> "ลบบัญชีไม่สำเร็จ กรุณาลองใหม่อีกครั้ง"
        }
    }

    private fun toFriendlyAuthError(statusCode: Int, rawBody: String?, isRegister: Boolean): String {
        val raw = rawBody.orEmpty().lowercase()

        return when {
            raw.contains("user_already_exists") || raw.contains("already registered") ->
                "อีเมลนี้สมัครไว้แล้ว กรุณาเข้าสู่ระบบแทน"

            raw.contains("invalid_credentials") || raw.contains("invalid login credentials") ->
                "อีเมลหรือรหัสผ่านไม่ถูกต้อง"

            raw.contains("email not confirmed") || raw.contains("email_not_confirmed") ->
                "กรุณายืนยันอีเมลก่อนเข้าสู่ระบบ"

            raw.contains("signup_disabled") || raw.contains("signups not allowed") ->
                "ระบบยังไม่เปิดให้สมัครสมาชิกใหม่"

            raw.contains("password") && raw.contains("weak") ->
                "รหัสผ่านง่ายเกินไป กรุณาตั้งรหัสผ่านใหม่ให้ปลอดภัยขึ้น"

            raw.contains("email") && raw.contains("invalid") ->
                "รูปแบบอีเมลไม่ถูกต้อง"

            statusCode == 400 && isRegister ->
                "สมัครสมาชิกไม่สำเร็จ กรุณาตรวจสอบอีเมลและรหัสผ่าน"

            statusCode == 400 ->
                "เข้าสู่ระบบไม่สำเร็จ กรุณาตรวจสอบข้อมูลอีกครั้ง"

            statusCode == 401 || statusCode == 403 ->
                "ไม่มีสิทธิ์เข้าใช้งาน กรุณาเข้าสู่ระบบใหม่"

            statusCode >= 500 ->
                "เซิร์ฟเวอร์มีปัญหาชั่วคราว กรุณาลองใหม่อีกครั้ง"

            else ->
                if (isRegister) "สมัครสมาชิกไม่สำเร็จ กรุณาลองใหม่อีกครั้ง" else "เข้าสู่ระบบไม่สำเร็จ กรุณาลองใหม่อีกครั้ง"
        }
    }

    private fun toFriendlyNetworkError(error: Exception): String {
        val message = error.message.orEmpty().lowercase()
        return when {
            message.contains("unable to resolve host") || message.contains("failed to connect") ->
                "เชื่อมต่ออินเทอร์เน็ตหรือ Supabase ไม่ได้ กรุณาตรวจสอบการเชื่อมต่อ"

            message.contains("timeout") ->
                "การเชื่อมต่อนานเกินไป กรุณาลองใหม่อีกครั้ง"

            else ->
                "เกิดข้อผิดพลาดในการเชื่อมต่อ กรุณาลองใหม่อีกครั้ง"
        }
    }
}
