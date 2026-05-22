package com.powergrid.exemployee.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.powergrid.exemployee.BuildConfig
import com.powergrid.exemployee.data.remote.AuthApi
import com.powergrid.exemployee.data.remote.EmployeeApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
object NetworkModule {

    // ── MODIFY THIS: Set your actual API base URL ──────────────────────────
    private const val BASE_URL = "https://api.powergrid.in/v1/"
    // ──────────────────────────────────────────────────────────────────────

    @Provides @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = false; coerceInputValues = true }

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .also {
            if (BuildConfig.DEBUG) it.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        }
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder().baseUrl(BASE_URL).client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()

    @Provides @Singleton fun provideAuthApi(r: Retrofit): AuthApi = r.create(AuthApi::class.java)
    @Provides @Singleton fun provideEmployeeApi(r: Retrofit): EmployeeApi = r.create(EmployeeApi::class.java)
}
