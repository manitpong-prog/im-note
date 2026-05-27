package com.example.sync

import com.imnotesminimal.app.data.User

class SupabaseAuthRepository {
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
