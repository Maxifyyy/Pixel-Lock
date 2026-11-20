package de.pixellock

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch

/** Modul „ClockFace“: Stil und Farbe der Uhr wählen, mit Live-Vorschau. */
class ClockSettingsActivity : AppCompatActivity() {

    private val colors = intArrayOf(
        Color.WHITE,
        0xFFFFD54F.toInt(), // Gelb
        0xFFFF8A65.toInt(), // Orange
        0xFFF06292.toInt(), // Pink
        0xFFBA68C8.toInt(), // Lila
        0xFF64B5F6.toInt(), // Blau
        0xFF4DD0E1.toInt(), // Türkis
        0xFF81C784.toInt()  // Grün
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = scrollPage("Uhr-Stile")

        val preview = ClockFaceView(this).apply {
            setBackgroundColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(220)
            ).apply { bottomMargin = dp(16) }
            style = Prefs.getInt(this@ClockSettingsActivity, Prefs.CLOCK_STYLE, 0)
            clockColor = Prefs.getInt(this@ClockSettingsActivity, Prefs.CLOCK_COLOR, Color.WHITE)
            showSeconds = Prefs.getBool(this@ClockSettingsActivity, Prefs.CLOCK_SECONDS, false)
        }
        page.addView(preview)

        // Stil
        val group = RadioGroup(this)
        listOf("Digital", "Analog", "Gestapelt").forEachIndexed { i, name ->
            group.addView(RadioButton(this).apply {
                id = View.generateViewId()
                text = name
                isChecked = preview.style == i
                setOnClickListener {
                    preview.style = i
                    Prefs.putInt(this@ClockSettingsActivity, Prefs.CLOCK_STYLE, i)
                }
            })
        }
        page.addView(group)

        // Sekunden
        page.addView(MaterialSwitch(this).apply {
            text = "Sekunden anzeigen"
            isChecked = preview.showSeconds
            setPadding(0, dp(8), 0, dp(8))
            setOnCheckedChangeListener { _, checked ->
                preview.showSeconds = checked
                Prefs.putBool(this@ClockSettingsActivity, Prefs.CLOCK_SECONDS, checked)
            }
        })

        // Farben
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, dp(16))
        }
        for (col in colors) {
            row.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(32), dp(32)).apply {
                    marginEnd = dp(8)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(col)
                    setStroke(dp(1), 0x55888888)
                }
                setOnClickListener {
                    preview.clockColor = col
                    Prefs.putInt(this@ClockSettingsActivity, Prefs.CLOCK_COLOR, col)
                }
            })
        }
        page.addView(row)

        page.addView(MaterialButton(this).apply {
            text = "Sperr-Uhr starten"
            setOnClickListener {
                startActivity(Intent(this@ClockSettingsActivity, LockClockActivity::class.java))
            }
        })
    }
}
