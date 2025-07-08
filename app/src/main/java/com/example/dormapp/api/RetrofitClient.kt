package com.example.dormapp.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    const val BASE_URL = "http://43.202.118.147:8000/"

    private fun authInterceptor(context: Context): Interceptor = Interceptor { chain ->
        val original = chain.request()
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)

        if (original.url.encodedPath.contains("/api/signup/") ||
            original.url.encodedPath.contains("/api/login/")
        ) {
            return@Interceptor chain.proceed(original)
        }

        val builder = original.newBuilder()
        if (!token.isNullOrEmpty()) {
            builder.addHeader("Authorization", "Token $token")
        }
        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun create(context: Context): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
