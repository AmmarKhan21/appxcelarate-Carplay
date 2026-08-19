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
    fun figmaVariantsMapToDistinctThemes() {
        assertEquals(DesignMode.STEALTH, DesignMode.fromPreference("stealth"))
        assertEquals(R.style.Theme_NewCarplay_Stealth, DesignMode.STEALTH.themeResId)
        assertEquals(DesignMode.FROST, DesignMode.fromPreference("frost"))
        assertEquals(DesignMode.COCKPIT, DesignMode.fromPreference("cockpit"))
        assertEquals(DesignMode.DAYLIGHT, DesignMode.fromPreference("daylight"))
        assertEquals(DesignMode.APPLE_GLASS, DesignMode.fromPreference("apple_glass"))
        assertEquals(DesignMode.CARBON_RALLY, DesignMode.fromPreference("carbon_rally"))
    }

    @Test
    fun previousPreferencesMigrateToClosestFigmaVariant() {
        assertEquals(DesignMode.STEALTH, DesignMode.fromPreference("executive"))
        assertEquals(DesignMode.DAYLIGHT, DesignMode.fromPreference("pearl"))
        assertEquals(DesignMode.FROST, DesignMode.fromPreference("sage"))
        assertEquals(DesignMode.CARBON_RALLY, DesignMode.fromPreference("carbon"))
    }

    @Test
    fun unknownPreferenceFallsBackToLegacy() {
        assertEquals(DesignMode.LEGACY, DesignMode.fromPreference("unknown"))
        assertEquals(DesignMode.LEGACY, DesignMode.fromPreference(null))
    }
}
