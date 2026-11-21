package de.pixellock

import android.content.Context
import android.graphics.Color
import android.os.Build

object Prefs {
    // Home Up
    const val COLUMNS = "columns"
    const val ICON_DP = "icon_dp"
    const val LABELS = "labels"
    const val SHAPE = "shape"            // 0 Original, 1 Kreis, 2 Squircle, 3 Abgerundet
    const val DOCK = "dock"
    const val DOCK_APPS = "dock_apps"
    const val SEARCH = "search"
    const val HOME_CLOCK = "home_clock"
    const val DIM = "dim"
    const val GESTURE = "gesture"
    const val HIDDEN = "hidden"

    // ClockFace
    const val CLOCK_STYLE = "clock_style"
    const val CLOCK_COLOR = "clock_color"  // 0 = Systemfarbe
    const val CLOCK_SECONDS = "clock_seconds"
    const val CLOCK_DATE = "clock_date"
    const val CLOCK_WEIGHT = "clock_weight"

    // LockStar
    const val LOCK_BRIGHT = "lock_bright"
    const val LOCK_KEEP_ON = "lock_keep_on"
    const val LOCK_SHIFT = "lock_shift"
    const val LOCK_BATTERY = "lock_battery"

    // Theme Park
    const val NIGHT = "night"            // 0 System, 1 Hell, 2 Dunkel
    const val DYNAMIC = "dynamic"

    // NotiStar
    const val NOTI_ENABLED = "noti_enabled"

    private fun sp(c: Context) = c.getSharedPreferences("pixellock", Context.MODE_PRIVATE)

    fun getInt(c: Context, key: String, def: Int): Int = sp(c).getInt(key, def)
    fun putInt(c: Context, key: String, value: Int) = sp(c).edit().putInt(key, value).apply()
    fun getBool(c: Context, key: String, def: Boolean): Boolean = sp(c).getBoolean(key, def)
    fun putBool(c: Context, key: String, value: Boolean) = sp(c).edit().putBoolean(key, value).apply()
    fun getString(c: Context, key: String, def: String): String = sp(c).getString(key, def) ?: def
    fun putString(c: Context, key: String, value: String) = sp(c).edit().putString(key, value).apply()

    fun getSet(c: Context, key: String): MutableSet<String> =
        HashSet(sp(c).getStringSet(key, emptySet()) ?: emptySet())

    fun putSet(c: Context, key: String, value: Set<String>) =
        sp(c).edit().putStringSet(key, HashSet(value)).apply()

    /** Uhrfarbe; 0 steht für die Systemfarbe (Material You) und fällt vor Android 12 auf Weiß zurück. */
    fun clockColor(c: Context): Int {
        val v = getInt(c, CLOCK_COLOR, Color.WHITE)
        if (v != 0) return v
        return if (Build.VERSION.SDK_INT >= 31) c.getColor(android.R.color.system_accent1_200) else Color.WHITE
    }
}
