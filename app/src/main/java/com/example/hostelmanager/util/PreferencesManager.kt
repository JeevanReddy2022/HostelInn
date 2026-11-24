package com.example.hostelmanager.util

import android.content.Context

object PreferencesManager {
    private const val PREFS_NAME = "hostel_manager_prefs"
    private const val KEY_LAST_HOSTEL_ID = "last_hostel_id"

    fun saveLastHostelId(context: Context, hostelId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_HOSTEL_ID, hostelId).apply()
    }

    fun getLastHostelId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_HOSTEL_ID, null)
    }

    fun clearLastHostelId(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_LAST_HOSTEL_ID).apply()
    }
}