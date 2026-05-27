package com.example.sync

import com.imnotesminimal.app.data.User

class SupabaseAuthRepository {
    suspend fun signUp(email: String, password: String, displayName: String): Result<User> {
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
                    )
                )
            } else {
                Result.failure(IllegalStateException(response.errorBody()?.string()?.ifBlank { null } ?: "สมัครสมาชิกไม่สำเร็จ"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
                Result.failure(IllegalStateException(response.errorBody()?.string()?.ifBlank { null } ?: "เข้าสู่ระบบไม่สำเร็จ"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
