package de.pixellock

import android.os.Bundle

/** Modul „Theme Park“: Hell/Dunkel und Material-You-Farben für PixelLock. */
class ThemeParkActivity : BasePage() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = scrollPage("Theme Park")

        page.addView(Ui.section(this, "Darstellung"))
        page.addView(Ui.group(this, listOf(
            Ui.chipRow(this, "Design", listOf("System", "Hell", "Dunkel"),
                Prefs.getInt(this, Prefs.NIGHT, 0)) {
                Prefs.putInt(this, Prefs.NIGHT, it)
                App.applyNightMode(application)
            },
            Ui.switchRow(this, "Material You",
                "Farben aus deinem Hintergrundbild übernehmen (Android 12+)",
                Prefs.getBool(this, Prefs.DYNAMIC, true)) {
                Prefs.putBool(this, Prefs.DYNAMIC, it)
                recreate()
            }
        )))
        page.addView(Ui.note(this,
            "Das gilt für alle Seiten von PixelLock. Die Farben der Uhr stellst du unter ClockFace ein."))

        page.addView(Ui.section(this, "Info"))
        val version = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "?"
        } catch (e: Exception) {
            "?"
        }
        page.addView(Ui.group(this, listOf(
            Ui.actionRow(this, "PixelLock", "Version $version") { }
        )))
    }
}
