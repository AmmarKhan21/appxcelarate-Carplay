package com.car.play.android.app.design

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import com.car.play.android.app.R

enum class DesignMode(
    val preferenceValue: String,
    @StyleRes val themeResId: Int,
    @StringRes val displayNameResId: Int,
    @StringRes val descriptionResId: Int,
    @DrawableRes val homeHeaderResId: Int,
    @StringRes val homeTitleResId: Int,
    @StringRes val homeSubtitleResId: Int
) {
    LEGACY(
        "legacy",
        R.style.Theme_NewCarplay,
        R.string.design_legacy,
        R.string.design_legacy_description,
        R.drawable.img_main,
        R.string.design_legacy_home_title,
        R.string.design_preview_subtitle
    ),
    STEALTH(
        "stealth",
        R.style.Theme_NewCarplay_Stealth,
        R.string.design_stealth,
        R.string.design_stealth_description,
        R.drawable.stealth_home_header,
        R.string.stealth_home_title,
        R.string.stealth_home_subtitle
    ),
    FROST(
        "frost",
        R.style.Theme_NewCarplay_Frost,
        R.string.design_frost,
        R.string.design_frost_description,
        R.drawable.frost_home_header,
        R.string.frost_home_title,
        R.string.frost_home_subtitle
    ),
    COCKPIT(
        "cockpit",
        R.style.Theme_NewCarplay_Cockpit,
        R.string.design_cockpit,
        R.string.design_cockpit_description,
        R.drawable.cockpit_home_header,
        R.string.cockpit_home_title,
        R.string.cockpit_home_subtitle
    ),
    DAYLIGHT(
        "daylight",
        R.style.Theme_NewCarplay_Daylight,
        R.string.design_daylight,
        R.string.design_daylight_description,
        R.drawable.daylight_home_header,
        R.string.daylight_home_title,
        R.string.daylight_home_subtitle
    ),
    APPLE_GLASS(
        "apple_glass",
        R.style.Theme_NewCarplay_AppleGlass,
        R.string.design_apple_glass,
        R.string.design_apple_glass_description,
        R.drawable.apple_home_header,
        R.string.apple_home_title,
        R.string.apple_home_subtitle
    ),
    CARBON_RALLY(
        "carbon_rally",
        R.style.Theme_NewCarplay_CarbonRally,
        R.string.design_carbon_rally,
        R.string.design_carbon_rally_description,
        R.drawable.rally_home_header,
        R.string.rally_home_title,
        R.string.rally_home_subtitle
    );

    companion object {
        private val legacyPreferenceMap = mapOf(
            "pearl" to DAYLIGHT,
            "sage" to FROST,
            "executive" to STEALTH,
            "carbon" to CARBON_RALLY,
            "midnight" to STEALTH
        )

        fun fromPreference(value: String?): DesignMode {
            if (value == null) return LEGACY
            entries.firstOrNull { it.preferenceValue == value }?.let { return it }
            legacyPreferenceMap[value]?.let { return it }
            return LEGACY
        }
    }
}
