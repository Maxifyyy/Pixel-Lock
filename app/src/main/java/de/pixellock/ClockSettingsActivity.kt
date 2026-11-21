package de.pixellock

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast

/** Modul „ClockFace“: Stil, Schrift und Farbe der Uhr, mit Live-Vorschau. */
class ClockSettingsActivity : BasePage() {

    private val colors = intArrayOf(
        0, // Systemfarbe
        Color.WHITE,
        0xFFFFD54F.toInt(),
        0xFFFF8A65.toInt(),
        0xFFF06292.toInt(),
        0xFFBA68C8.toInt(),
        0xFF64B5F6.toInt(),
        0xFF4DD0E1.toInt(),
        0xFF81C784.toInt()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = scrollPage("ClockFace")

        val preview = ClockFaceView(this).apply {
            background = GradientDrawable().apply {
                setColor(Color.BLACK)
                cornerRadius = dp(28).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(220)
            ).apply { bottomMargin = dp(12) }
            loadPrefs()
        }
        page.addView(preview)

        page.addView(Ui.section(this, "Aussehen"))
        page.addView(Ui.group(this, listOf(
            Ui.chipRow(this, "Stil", listOf("Digital", "Analog", "Gestapelt"), preview.style) {
                preview.style = it
                Prefs.putInt(this, Prefs.CLOCK_STYLE, it)
                refreshWidgets()
            },
            Ui.chipRow(this, "Schrift", listOf("Dünn", "Normal", "Fett"), preview.weight) {
                preview.weight = it
                Prefs.putInt(this, Prefs.CLOCK_WEIGHT, it)
            },
            Ui.colorRow(
                this, "Farbe", "Der erste Punkt folgt deinen Systemfarben (Android 12+).",
                colors, Prefs.getInt(this, Prefs.CLOCK_COLOR, Color.WHITE),
                resolve = { c ->
                    if (c == 0) {
                        if (android.os.Build.VERSION.SDK_INT >= 31)
                            getColor(android.R.color.system_accent1_200) else Color.WHITE
                    } else c
                }
            ) {
                Prefs.putInt(this, Prefs.CLOCK_COLOR, it)
                preview.clockColor = Prefs.clockColor(this)
                refreshWidgets()
            }
        )))

        page.addView(Ui.section(this, "Anzeige"))
        page.addView(Ui.group(this, listOf(
            Ui.switchRow(this, "Sekunden", "Auch auf dem Startbildschirm und der Sperr-Uhr",
                preview.showSeconds) {
                preview.showSeconds = it
                Prefs.putBool(this, Prefs.CLOCK_SECONDS, it)
            },
            Ui.switchRow(this, "Datum", "Unter der digitalen Uhr anzeigen", preview.showDate) {
                preview.showDate = it
                Prefs.putBool(this, Prefs.CLOCK_DATE, it)
            }
        )))

        page.addView(Ui.section(this, "Widget"))
        page.addView(Ui.group(this, listOf(
            Ui.actionRow(this, "Uhr-Widget hinzufügen", "Auf dem Startbildschirm platzieren") {
                addWidget()
            }
        )))
    }

    private fun refreshWidgets() = ClockWidgetProvider.refresh(this)

    private fun addWidget() {
        val mgr = AppWidgetManager.getInstance(this)
        if (mgr.isRequestPinAppWidgetSupported) {
            mgr.requestPinAppWidget(ComponentName(this, ClockWidgetProvider::class.java), null, null)
        } else {
            Toast.makeText(
                this, "Halte den Startbildschirm gedrückt → Widgets → PixelLock", Toast.LENGTH_LONG
            ).show()
        }
    }
}
