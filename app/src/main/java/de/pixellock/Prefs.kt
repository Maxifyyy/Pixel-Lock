package de.pixellock

import android.content.Context

object Prefs {
    const val COLUMNS = "columns"
    const val ICON_DP = "icon_dp"
    const val LABELS = "labels"
    const val CLOCK_STYLE = "clock_style"
    const val CLOCK_COLOR = "clock_color"
    const val CLOCK_SECONDS = "clock_seconds"

    private fun sp(c: Context) = c.getSharedPreferences("pixellock", Context.MODE_PRIVATE)

    fun getInt(c: Context, key: String, def: Int): Int = sp(c).getInt(key, def)
    fun putInt(c: Context, key: String, value: Int) = sp(c).edit().putInt(key, value).apply()
    fun getBool(c: Context, key: String, def: Boolean): Boolean = sp(c).getBoolean(key, def)
    fun putBool(c: Context, key: String, value: Boolean) = sp(c).edit().putBoolean(key, value).apply()
}
