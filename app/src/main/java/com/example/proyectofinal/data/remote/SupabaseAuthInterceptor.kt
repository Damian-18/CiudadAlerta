package com.example.proyectofinal.data.remote

import com.example.proyectofinal.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class SupabaseAuthInterceptor : Interceptor {
    var accessToken: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val builder = originalRequest.newBuilder()
            .header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        
        if (accessToken != null) {
            builder.header("Authorization", "Bearer $accessToken")
        } else {
            builder.header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        }

        // Para Supabase/PostgREST, solicitar la representación del objeto después de insertar/actualizar
        if (originalRequest.method == "POST" || originalRequest.method == "PATCH") {
            builder.header("Prefer", "return=representation")
        }

        return chain.proceed(builder.build())
    }
}
