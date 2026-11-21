package de.pixellock

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Einstellungen für Home Up. Änderungen gelten sofort beim nächsten Öffnen des Startbildschirms. */
class HomeSettingsActivity : BasePage() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = scrollPage("Home Up")

        page.addView(Ui.section(this, "Raster"))
        page.addView(Ui.group(this, listOf(
            Ui.sliderRow(this, "Spalten", 3, 7, Prefs.getInt(this, Prefs.COLUMNS, 5), "") {
                Prefs.putInt(this, Prefs.COLUMNS, it)
            },
            Ui.sliderRow(this, "Icon-Größe", 40, 96, Prefs.getInt(this, Prefs.ICON_DP, 56), " dp") {
                Prefs.putInt(this, Prefs.ICON_DP, it)
            },
            Ui.switchRow(this, "App-Namen anzeigen", null, Prefs.getBool(this, Prefs.LABELS, true)) {
                Prefs.putBool(this, Prefs.LABELS, it)
            },
            Ui.chipRow(this, "Icon-Form", listOf("Original", "Kreis", "Squircle", "Abgerundet"),
                Prefs.getInt(this, Prefs.SHAPE, 1)) {
                Prefs.putInt(this, Prefs.SHAPE, it)
            }
        )))

        page.addView(Ui.section(this, "Startseite"))
        page.addView(Ui.group(this, listOf(
            Ui.switchRow(this, "Uhr anzeigen", "Stil und Farbe stellst du unter ClockFace ein",
                Prefs.getBool(this, Prefs.HOME_CLOCK, true)) {
                Prefs.putBool(this, Prefs.HOME_CLOCK, it)
            },
            Ui.switchRow(this, "Suchleiste", "Apps nach Namen suchen",
                Prefs.getBool(this, Prefs.SEARCH, true)) {
                Prefs.putBool(this, Prefs.SEARCH, it)
            },
            Ui.switchRow(this, "Dock", "Bis zu 5 Lieblings-Apps unten",
                Prefs.getBool(this, Prefs.DOCK, true)) {
                Prefs.putBool(this, Prefs.DOCK, it)
            },
            Ui.sliderRow(this, "Hintergrund abdunkeln", 0, 80,
                Prefs.getInt(this, Prefs.DIM, 25), " %") {
                Prefs.putInt(this, Prefs.DIM, it)
            }
        )))

        page.addView(Ui.section(this, "Gesten"))
        page.addView(Ui.group(this, listOf(
            Ui.switchRow(this, "Auf der Uhr nach unten wischen",
                "Öffnet die Benachrichtigungen (wird nicht von jedem Android-Stand erlaubt)",
                Prefs.getBool(this, Prefs.GESTURE, true)) {
                Prefs.putBool(this, Prefs.GESTURE, it)
            }
        )))
        page.addView(Ui.note(this,
            "Tipp: Langer Druck auf die Uhr öffnet PixelLock, langer Druck auf eine App das App-Menü."))

        page.addView(Ui.section(this, "Apps"))
        val hiddenCount = Prefs.getSet(this, Prefs.HIDDEN).size
        page.addView(Ui.group(this, listOf(
            Ui.actionRow(this, "Ausgeblendete Apps", "$hiddenCount ausgeblendet") { manageHidden() },
            Ui.actionRow(this, "Dock leeren", null) {
                Prefs.putString(this, Prefs.DOCK_APPS, "")
                Toast.makeText(this, "Dock geleert", Toast.LENGTH_SHORT).show()
            },
            Ui.actionRow(this, "Als Standard-Startbildschirm festlegen", null) {
                startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            }
        )))
    }

    private fun appLabel(pkg: String): String = try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    } catch (e: Exception) {
        pkg
    }

    private fun manageHidden() {
        val hidden = Prefs.getSet(this, Prefs.HIDDEN).toList().sortedBy { appLabel(it).lowercase() }
        if (hidden.isEmpty()) {
            Toast.makeText(this, "Keine Apps ausgeblendet", Toast.LENGTH_SHORT).show()
            return
        }
        val names = hidden.map { appLabel(it) }.toTypedArray()
        val checked = BooleanArray(hidden.size) { true }
        MaterialAlertDialogBuilder(this)
            .setTitle("Haken entfernen = wieder anzeigen")
            .setMultiChoiceItems(names, checked) { _, i, c -> checked[i] = c }
            .setPositiveButton("Speichern") { _, _ ->
                val keep = hidden.filterIndexed { i, _ -> checked[i] }.toSet()
                Prefs.putSet(this, Prefs.HIDDEN, keep)
                recreate()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }
}
