package com.car.play.android.app.design

import android.app.Activity

object DesignTheme {
    fun apply(activity: Activity): DesignMode {
        val mode = DesignPreferences(activity).getDesignMode()
        activity.setTheme(mode.themeResId)
        return mode
    }
}
