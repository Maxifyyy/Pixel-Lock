package de.pixellock

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        applyNightMode(this)
        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions.Builder()
                .setPrecondition { _, _ -> Prefs.getBool(this@App, Prefs.DYNAMIC, true) }
                .build()
        )
    }

    companion object {
        fun applyNightMode(app: Application) {
            AppCompatDelegate.setDefaultNightMode(
                when (Prefs.getInt(app, Prefs.NIGHT, 0)) {
                    1 -> AppCompatDelegate.MODE_NIGHT_NO
                    2 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            )
        }
    }
}
