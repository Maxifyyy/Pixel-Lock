package de.pixellock

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch

/** Einstellungen für den Launcher (Home Up). Änderungen gelten sofort beim nächsten Öffnen des Home-Screens. */
class LauncherSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = scrollPage("Launcher")

        page.addView(seekRow(
            this, "Spalten", 3, 7, Prefs.getInt(this, Prefs.COLUMNS, 5), ""
        ) { Prefs.putInt(this, Prefs.COLUMNS, it) })

        page.addView(seekRow(
            this, "Icon-Größe", 40, 96, Prefs.getInt(this, Prefs.ICON_DP, 56), " dp"
        ) { Prefs.putInt(this, Prefs.ICON_DP, it) })

        page.addView(MaterialSwitch(this).apply {
            text = "App-Namen anzeigen"
            isChecked = Prefs.getBool(this@LauncherSettingsActivity, Prefs.LABELS, true)
            setOnCheckedChangeListener { _, checked ->
                Prefs.putBool(this@LauncherSettingsActivity, Prefs.LABELS, checked)
            }
            setPadding(0, dp(16), 0, 0)
        })
    }
}
