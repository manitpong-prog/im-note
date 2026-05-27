package com.example.sync

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object SupabaseService {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit? by lazy {
        if (!SupabaseConfig.isConfigured) {
            null
        } else {
            Retrofit.Builder()
                .baseUrl("${SupabaseConfig.url}/")
                .client(okHttpClient)
                .addConverterFactory(
                    MoshiConverterFactory
                        .create(moshi)
                        .withNullSerialization()
                )
                .build()
        }
    }

    val authApi: SupabaseAuthApi?
        get() = retrofit?.create(SupabaseAuthApi::class.java)

    val notesApi: SupabaseNotesApi?
        get() = retrofit?.create(SupabaseNotesApi::class.java)

    fun bearer(accessToken: String): String = "Bearer $accessToken"
}
