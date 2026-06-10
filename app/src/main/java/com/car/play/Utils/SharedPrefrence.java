package com.car.play.Utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.car.play.android.app.BuildConfig;

public class SharedPrefrence {
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    private static final String PREF_NAME = BuildConfig.APPLICATION_ID + "appname";
    public static void saveSubscriptionState(Context context,boolean isSubscribed) {
        pref = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        editor = pref.edit();
        editor.putBoolean("removeads", isSubscribed);
        editor.apply();
    }
    public static boolean checkSubscriptionState(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return pref.getBoolean("removeads", false);
    }


}
