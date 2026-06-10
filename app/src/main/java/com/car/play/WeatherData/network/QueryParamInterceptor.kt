package com.car.play.WeatherData.network

import com.car.play.WeatherData.APP_ID
import com.car.play.WeatherData.MyApp
import com.car.play.WeatherData.PreferencesManager
import okhttp3.Interceptor
import okhttp3.Response



class QueryParamInterceptor:Interceptor {

    val context = MyApp.context
    private val prefManager = PreferencesManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.newBuilder()
            .addQueryParameter("appid", APP_ID)
            .addQueryParameter("units",prefManager.tempUnit)
            .build()

        val request = chain.request().newBuilder()
            .url(url)
            .build()

        return chain.proceed(request)
    }
}