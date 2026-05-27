package com.example.sync

import com.squareup.moshi.Json

data class SupabaseAuthRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>? = null
)

data class SupabaseAuthUser(
    val id: String,
    val email: String? = null
)

data class SupabaseAuthSession(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "expires_in") val expiresIn: Long? = null,
    @Json(name = "token_type") val tokenType: String? = null,
    val user: SupabaseAuthUser? = null
)

data class SupabaseNoteDto(
    val id: String? = null,
    @Json(name = "user_id") val userId: String,
    @Json(name = "local_id") val localId: Int? = null,
    val title: String,
    val content: String,
    @Json(name = "color_index") val colorIndex: Int,
    @Json(name = "is_pinned") val isPinned: Boolean,
    @Json(name = "client_created_at") val clientCreatedAt: String? = null,
    @Json(name = "client_updated_at") val clientUpdatedAt: String? = null,
    @Json(name = "deleted_at") val deletedAt: String? = null,
    @Json(name = "sync_version") val syncVersion: Int = 1,
    @Json(name = "device_id") val deviceId: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)
