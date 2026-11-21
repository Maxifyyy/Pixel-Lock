package de.pixellock

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast

/** Modul „LockStar“: Sperr-Uhr (über dem Sperrbildschirm) und Schnelleinstellungen-Kachel. */
class LockSettingsActivity : BasePage() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = scrollPage("LockStar")

        page.addView(Ui.body(this,
            "Die Sperr-Uhr zeigt eine große Uhr auf schwarzem Grund, auch bei gesperrtem Gerät. Ein Tippen schließt sie."))
        page.addView(Ui.button(this, "Sperr-Uhr jetzt starten") {
            startActivity(Intent(this, LockClockActivity::class.java))
        }.apply {
            (layoutParams as LinearLayout.LayoutParams).apply {
                topMargin = dp(12)
                bottomMargin = dp(4)
            }
        })

        page.addView(Ui.section(this, "Anzeige"))
        page.addView(Ui.group(this, listOf(
            Ui.sliderRow(this, "Helligkeit", 5, 100,
                Prefs.getInt(this, Prefs.LOCK_BRIGHT, 25), " %") {
                Prefs.putInt(this, Prefs.LOCK_BRIGHT, it)
            },
            Ui.switchRow(this, "Bildschirm anlassen", "Display schaltet sich nicht von selbst aus",
                Prefs.getBool(this, Prefs.LOCK_KEEP_ON, true)) {
                Prefs.putBool(this, Prefs.LOCK_KEEP_ON, it)
            },
            Ui.switchRow(this, "Einbrennschutz", "Uhr verschiebt sich jede Minute ein kleines Stück",
                Prefs.getBool(this, Prefs.LOCK_SHIFT, true)) {
                Prefs.putBool(this, Prefs.LOCK_SHIFT, it)
            },
            Ui.switchRow(this, "Akkustand anzeigen", null,
                Prefs.getBool(this, Prefs.LOCK_BATTERY, true)) {
                Prefs.putBool(this, Prefs.LOCK_BATTERY, it)
            }
        )))
        page.addView(Ui.note(this, "Die Uhr selbst stellst du unter ClockFace ein."))

        page.addView(Ui.section(this, "Schnelleinstellungen"))
        page.addView(Ui.group(this, listOf(
            Ui.actionRow(this, "Kachel „Sperr-Uhr“ hinzufügen",
                "Startet die Sperr-Uhr direkt aus den Schnelleinstellungen") { addTile() }
        )))
        page.addView(Ui.note(this,
            "Hinweis: Die Sperr-Uhr erscheint über dem Sperrbildschirm, ersetzt ihn aber nicht. Den echten Sperrbildschirm kann keine App ändern."))
    }

    private fun addTile() {
        if (Build.VERSION.SDK_INT >= 33) {
            val sbm = getSystemService(StatusBarManager::class.java)
            sbm.requestAddTileService(
                ComponentName(this, LockClockTileService::class.java),
                "Sperr-Uhr",
                Icon.createWithResource(this, R.drawable.ic_tile_lock),
                mainExecutor
            ) { }
        } else {
            Toast.makeText(
                this,
                "Schnelleinstellungen herunterziehen → Stift-Symbol → Kachel „Sperr-Uhr“ hinzufügen",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
