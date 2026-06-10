package com.car.play.WeatherData

interface RequestCallback<T> {
    fun onRequestCompleted(data:T)
    fun onRequestFailed(errorMessage:String?)
}