package com.car.play.WeatherData

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

fun showToast(context:Context,message:String,length:Int) {
    Toast.makeText(context,message,length).show()
}

fun Int.unixTimestampToTimeString() : String {
    try {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this*1000.toLong()
        val outputDateFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        outputDateFormat.timeZone = TimeZone.getDefault()
        return outputDateFormat.format(calendar.time)

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return this.toString()
}
