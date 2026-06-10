package com.car.play.WeatherData

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    var preference: SharedPreferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE)
    var editor: SharedPreferences.Editor = preference.edit()

    var tempUnit:String get() = preference.getString(KEY_UNITS,"metric")!!
    set(value) {editor.putString(KEY_UNITS,value).commit()}

}