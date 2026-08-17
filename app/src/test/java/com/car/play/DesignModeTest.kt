package com.car.play.android.app.design

import com.car.play.android.app.R
import org.junit.Assert.assertEquals
import org.junit.Test

class DesignModeTest {
    @Test
    fun legacyPreferenceMapsToLegacyTheme() {
        val mode = DesignMode.fromPreference("legacy")

        assertEquals(DesignMode.LEGACY, mode)
        assertEquals(R.style.Theme_NewCarplay, mode.themeResId)
    }

    @Test
    fun previousExecutivePreferenceMigratesToMidnightTheme() {
        val mode = DesignMode.fromPreference("executive")

        assertEquals(DesignMode.MIDNIGHT, mode)
        assertEquals(R.style.Theme_NewCarplay_Midnight, mode.themeResId)
    }

    @Test
    fun allNewDesignPreferencesMapToDistinctThemes() {
        assertEquals(DesignMode.PEARL, DesignMode.fromPreference("pearl"))
        assertEquals(R.style.Theme_NewCarplay_Pearl, DesignMode.PEARL.themeResId)
        assertEquals(DesignMode.SAGE, DesignMode.fromPreference("sage"))
        assertEquals(R.style.Theme_NewCarplay_Sage, DesignMode.SAGE.themeResId)
        assertEquals(DesignMode.CARBON, DesignMode.fromPreference("carbon"))
        assertEquals(R.style.Theme_NewCarplay_Carbon, DesignMode.CARBON.themeResId)
    }

    @Test
    fun unknownPreferenceFallsBackToLegacy() {
        assertEquals(DesignMode.LEGACY, DesignMode.fromPreference("unknown"))
        assertEquals(DesignMode.LEGACY, DesignMode.fromPreference(null))
    }
}
