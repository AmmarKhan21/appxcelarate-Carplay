package com.car.play.WeatherData

data class  DataResource<T> (val status: RequestStatus, val data:T?, val message:String?){
    companion object {
        fun <T> success(data:T?):DataResource<T>{
            return DataResource(RequestStatus.SUCCESS,data,null)
        }
        fun <T> error(data:T?,message: String?):DataResource<T>{
            return DataResource(RequestStatus.ERROR,data,message)
        }
        fun <T> loading(data:T?):DataResource<T>{
            return DataResource(RequestStatus.LOADING,data,null)
        }
    }
}
