package com.jgm.mybudgetapp.sharedPrefs;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsPrefs {

    private static final String SETTINGS_PREFS = "com.jgm.mybudgetapp.settings_prefs";

    private static final SharedPreferences getSettingsPrefs(Context context) {
        return context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
    }

    public static boolean getSettingsPrefsBoolean(Context context, String key) {
        return getSettingsPrefs(context).getBoolean(key, false);
    }

    public static void setSettingsPrefsBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getSettingsPrefs(context).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void clearSettingsPrefs(Context context) {
        SharedPreferences.Editor editor = getSettingsPrefs(context).edit();
        editor.clear();
        editor.apply();
    }

}