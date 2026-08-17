package com.car.play.android.app.design

import android.content.Context

class DesignPreferences(context: Context) {
    private val appContext = context.applicationContext
    private val preferences =
        appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getDesignMode(): DesignMode {
        val storedValue = preferences.getString(KEY_DESIGN_MODE, null)
        if (storedValue != null) return DesignMode.fromPreference(storedValue)

        val initialMode = if (isUpdatedInstall()) {
            DesignMode.LEGACY
        } else {
            DesignMode.PEARL
        }
        setDesignMode(initialMode)
        return initialMode
    }

    fun setDesignMode(mode: DesignMode) {
        preferences.edit().putString(KEY_DESIGN_MODE, mode.preferenceValue).apply()
    }

    private fun isUpdatedInstall(): Boolean {
        return runCatching {
            val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            packageInfo.lastUpdateTime - packageInfo.firstInstallTime > UPDATE_TIME_TOLERANCE_MS
        }.getOrDefault(false)
    }

    companion object {
        internal const val PREFERENCES_NAME = "design_preferences"
        internal const val KEY_DESIGN_MODE = "design_mode"
        private const val UPDATE_TIME_TOLERANCE_MS = 1_000L
    }
}
