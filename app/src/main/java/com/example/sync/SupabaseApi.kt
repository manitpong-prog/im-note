package com.example.sync

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Query

data class SupabaseErrorBody(
    val message: String? = null,
    val error: String? = null,
    val code: String? = null
)

interface SupabaseAuthApi {
    @Headers("Content-Type: application/json")
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Header("apikey") apiKey: String,
        @Body body: SupabaseAuthRequest
    ): Response<SupabaseAuthSession>

    @Headers("Content-Type: application/json")
    @POST("auth/v1/token?grant_type=password")
    suspend fun signInWithPassword(
        @Header("apikey") apiKey: String,
        @Body body: SupabaseAuthRequest
    ): Response<SupabaseAuthSession>
}

interface SupabaseNotesApi {
    @Headers(
        "Content-Type: application/json",
        "Prefer: return=representation"
    )
    @POST("rest/v1/notes")
    suspend fun createNote(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body note: SupabaseNoteDto
    ): Response<List<SupabaseNoteDto>>

    @Headers(
        "Content-Type: application/json",
        "Prefer: return=representation,resolution=merge-duplicates"
    )
    @POST("rest/v1/notes?on_conflict=user_id,device_id,local_id")
    suspend fun upsertNote(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body note: SupabaseNoteDto
    ): Response<List<SupabaseNoteDto>>

    @GET("rest/v1/notes")
    suspend fun getNotes(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "updated_at.desc"
    ): Response<List<SupabaseNoteDto>>

    @Headers(
        "Content-Type: application/json",
        "Prefer: return=representation"
    )
    @PATCH("rest/v1/notes")
    suspend fun updateNoteByRemoteId(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("id") remoteIdFilter: String,
        @Body note: SupabaseNoteDto
    ): Response<List<SupabaseNoteDto>>
}
