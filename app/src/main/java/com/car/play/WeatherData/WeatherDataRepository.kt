package com.car.play.WeatherData

import com.car.play.WeatherData.network.ApiClient


class WeatherDataRepository {
    suspend fun getWeatherByLocation(lat:String,lon:String) = ApiClient.api.getWeatherByLocation(lat,lon)
}