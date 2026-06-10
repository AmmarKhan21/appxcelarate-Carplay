package com.car.play.WeatherData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class WeatherViewModel:ViewModel() {

    //location live data
    val locationLiveData = MutableLiveData<GeoLocation>()
    val locationLiveDataFailure = MutableLiveData<String?>()
    //weatherByLocation live data
    val weatherByLocation = MutableLiveData<DataResource<ResponseWeather>>()

    fun getCurrentLocation(model: GeoLocationService){
        model.getUserCurrentLocation(object :RequestCallback<GeoLocation> {
            override fun onRequestCompleted(data: GeoLocation) {
                locationLiveData.postValue(data)
            }

            override fun onRequestFailed(errorMessage: String?) {
                locationLiveDataFailure.postValue(errorMessage)
            }
        })
    }

    /**
     * Weather by Location call
     */
    fun getWeatherByLocation(model:WeatherDataRepository, lat:String, lon:String) {
        viewModelScope.launch {  safeWeatherByLocationFetch(model,lat,lon) }
    }

    private suspend fun safeWeatherByLocationFetch(model: WeatherDataRepository, lat: String, lon: String) {
        weatherByLocation.postValue(DataResource.loading(null))
        try {
            val response = model.getWeatherByLocation(lat,lon)
            weatherByLocation.postValue(handleWeatherResponse(response))
        } catch (t:Throwable){
            when(t){
                is IOException -> weatherByLocation.postValue(DataResource.error(null,"Network Failure"))
                else -> weatherByLocation.postValue(DataResource.error(null,t.localizedMessage))
            }
        }
    }
    private fun handleWeatherResponse(response: Response<ResponseWeather>): DataResource<ResponseWeather> {
        return if (response.isSuccessful) DataResource.success(response.body()) else DataResource.error(null,"Error: ${response.errorBody()}")
    }


}