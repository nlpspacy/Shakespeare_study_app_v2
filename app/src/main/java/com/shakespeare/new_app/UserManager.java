package com.shakespeare.new_app;

import android.content.Context;
import android.content.SharedPreferences;

public class UserManager {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USERNAME = "username";

    // Save username
    public static void saveUsername(Context context, String username) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USERNAME, username).apply();
    }

    // Retrieve username
    public static String getUsername(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        return prefs.getString(KEY_USERNAME, "dan");  // Default = "dan" if not set
        return prefs.getString(KEY_USERNAME, "");
    }
    public static void setUsername(Context context, String username) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USERNAME, username).apply();
    }

    public static boolean isUsernameSet(Context context) {
        return !getUsername(context).trim().isEmpty();
    }
}
