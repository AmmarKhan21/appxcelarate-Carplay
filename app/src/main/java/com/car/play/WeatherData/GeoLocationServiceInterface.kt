package com.car.play.WeatherData

interface GeoLocationServiceInterface {
    fun getUserCurrentLocation(callback: RequestCallback<GeoLocation>)
}