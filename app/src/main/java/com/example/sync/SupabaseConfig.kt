package com.example.sync

import com.imnotesminimal.app.BuildConfig

object SupabaseConfig {
    val url: String = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY.trim()

    val isConfigured: Boolean
        get() = url.startsWith("https://") && anonKey.isNotBlank()

    fun requireConfigured() {
        check(isConfigured) {
            "Supabase is not configured. Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties."
        }
    }
}
