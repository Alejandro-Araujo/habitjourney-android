package com.alejandro.habitjourney.core.data.remote.network

import com.alejandro.habitjourney.BuildConfig
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import jakarta.inject.Inject
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Singleton
class RetrofitClient @Inject constructor(
    private val okHttpClient: OkHttpClient
){
    private val BASE_URL = BuildConfig.API_BASE_URL

    fun create(): Retrofit {
        val gson = GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}