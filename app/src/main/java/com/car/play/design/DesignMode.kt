package com.car.play.android.app.design

import androidx.annotation.StyleRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.car.play.android.app.R

enum class DesignMode(
    val preferenceValue: String,
    @StyleRes val themeResId: Int,
    @StringRes val displayNameResId: Int,
    @StringRes val descriptionResId: Int,
    @DrawableRes val homeHeaderResId: Int?,
    @StringRes val homeTitleResId: Int,
    @StringRes val homeSubtitleResId: Int
) {
    LEGACY(
        "legacy",
        R.style.Theme_NewCarplay,
        R.string.design_legacy,
        R.string.design_legacy_description,
        null,
        R.string.design_legacy_home_title,
        R.string.design_preview_subtitle
    ),
    PEARL(
        "pearl",
        R.style.Theme_NewCarplay_Pearl,
        R.string.design_pearl,
        R.string.design_pearl_description,
        R.drawable.pearl_home_header,
        R.string.pearl_home_title,
        R.string.pearl_home_subtitle
    ),
    SAGE(
        "sage",
        R.style.Theme_NewCarplay_Sage,
        R.string.design_sage,
        R.string.design_sage_description,
        R.drawable.sage_home_header,
        R.string.sage_home_title,
        R.string.sage_home_subtitle
    ),
    MIDNIGHT(
        "executive",
        R.style.Theme_NewCarplay_Midnight,
        R.string.design_midnight,
        R.string.design_midnight_description,
        R.drawable.executive_home_header,
        R.string.executive_home_title,
        R.string.executive_home_subtitle
    ),
    CARBON(
        "carbon",
        R.style.Theme_NewCarplay_Carbon,
        R.string.design_carbon,
        R.string.design_carbon_description,
        R.drawable.carbon_home_header,
        R.string.carbon_home_title,
        R.string.carbon_home_subtitle
    );

    /** Header artwork used for the in-picker mockup; legacy has no dedicated header. */
    @DrawableRes
    fun previewHeaderResId(): Int = homeHeaderResId ?: R.drawable.app_bg

    companion object {
        fun fromPreference(value: String?): DesignMode =
            entries.firstOrNull { it.preferenceValue == value } ?: LEGACY
    }
}
