package com.quaterfoldvendorapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPrefsHelper {
    private lateinit var prefs: SharedPreferences
    private const val PREFS_NAME = "quaterfoldapp"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun read(key: String, defValue: String): String? {
        return prefs.getString(key, defValue)
    }

    fun read(key: String, defValue: Boolean): Boolean {
        return prefs.getBoolean(key, defValue)
    }

    fun read(key: String, defValue: Int): Int {
        return prefs.getInt(key, defValue)
    }

    fun read(key: String): String? {
        return prefs.getString(key, null)
    }

    fun readBoolean(key: String): Boolean {
        return prefs.getBoolean(key, false)
    }

    fun readInt(key: String): Int {
        return prefs.getInt(key, 0)
    }

    fun write(key: String, value: String) {
        val prefsEditor: SharedPreferences.Editor = prefs.edit()
        with(prefsEditor) {
            putString(key, value)
            commit()
        }
    }

    fun write(key: String, value: Boolean) {
        val prefsEditor: SharedPreferences.Editor = prefs.edit()
        with(prefsEditor) {
            putBoolean(key, value)
            commit()
        }
    }

    fun write(key: String, value: Int) {
        val prefsEditor: SharedPreferences.Editor = prefs.edit()
        with(prefsEditor) {
            putInt(key, value)
            commit()
        }
    }


    fun clearAllPreferences() {
        prefs.edit { clear() }
    }
}