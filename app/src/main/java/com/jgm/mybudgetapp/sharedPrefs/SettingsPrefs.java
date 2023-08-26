package com.jgm.mybudgetapp.sharedPrefs;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsPrefs {

    private static final String SETTINGS_PREFS = "com.jgm.mybudgetapp.settings_prefs";

    private static SharedPreferences getSettingsPrefs(Context context) {
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

    public static String getSettingsPrefsString(Context context, String key) {
        return getSettingsPrefs(context).getString(key, "");
    }

    public static void setSettingsPrefsString(Context context, String key, String value) {
        SharedPreferences.Editor editor = getSettingsPrefs(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static long getSettingsPrefsMilliseconds(Context context, String key) {
        return getSettingsPrefs(context).getLong(key, 0);
    }

    public static void setSettingsPrefsMilliseconds(Context context, String key, long value) {
        SharedPreferences.Editor editor = getSettingsPrefs(context).edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static void clearSettingsPrefs(Context context) {
        SharedPreferences.Editor editor = getSettingsPrefs(context).edit();
        editor.clear();
        editor.apply();
    }

}