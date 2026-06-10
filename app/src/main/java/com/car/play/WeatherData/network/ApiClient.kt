package com.car.play.WeatherData.network
import com.car.play.WeatherData.BASE_URL
import com.intuit.ssp.BuildConfig

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {
    companion object {
        private val retrofit by lazy {
            // Create the HttpLoggingInterceptor
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY // Log full request and response in debug mode
                } else {
                    HttpLoggingInterceptor.Level.NONE // Disable logging in production
                }
            }

            // Build OkHttpClient with interceptors
            val httpClient = OkHttpClient.Builder()
                .addInterceptor(QueryParamInterceptor()) // Add your custom interceptor
                .addInterceptor(loggingInterceptor) // Add logging interceptor
                .build()

            // Build Retrofit instance
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build()
        }

        val api: ApiService by lazy {
            retrofit.create(ApiService::class.java)
        }
    }
}