package com.car.play.android.app.sharepref

import android.content.Context
import android.content.SharedPreferences


class MyPref(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE)
    private val sharpref: SharedPreferences =
        context.getSharedPreferences("mypref", Context.MODE_PRIVATE)

    fun setIntroValue() {
        sharedPreferences.edit().putBoolean("introScreen", true).apply()
    }

    fun getIntroValue(): Boolean {
        return sharedPreferences.getBoolean("introScreen", false)
    }

    fun setLanguage(value: Boolean) {
        sharpref.edit().putBoolean("language", value).apply()
    }

    fun getLanguage(): Boolean {
        return sharpref.getBoolean("language", false)
    }

    fun setLatitude(value: String) {
        sharpref.edit().putString("Latitude", value).apply()
    }

    fun getLatitude(): String {
        return sharpref.getString("Latitude", "").toString()
    }
    fun setLongitude(value: String) {
        sharpref.edit().putString("Longitude", value).apply()
    }

    fun getLongitude(): String {
        return sharpref.getString("Longitude", "").toString()
    }

    fun setEmail(value: String) {
        sharpref.edit().putString("email", value).apply()
    }

    fun getEmail(): String {
        return sharpref.getString("email", "").toString()
    }

     fun saveSubscriptionState(isSubscribed: Boolean) {
        sharpref.edit().putBoolean("is_subscribed", isSubscribed).apply()
    }

     fun checkSubscriptionState(): Boolean {
        return sharpref.getBoolean("is_subscribed", false)
    }

}
