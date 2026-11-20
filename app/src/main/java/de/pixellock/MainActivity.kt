package de.pixellock

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

/** Hub: Einstieg in alle Module. */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = scrollPage("PixelLock")

        page.addView(card(
            "Launcher (Home Up)",
            "Raster, Icon-Größe und Beschriftung des Startbildschirms anpassen"
        ) { startActivity(Intent(this, LauncherSettingsActivity::class.java)) })

        page.addView(card(
            "Uhr-Stile (ClockFace)",
            "Digital, Analog oder gestapelt – mit eigener Farbe"
        ) { startActivity(Intent(this, ClockSettingsActivity::class.java)) })

        page.addView(card(
            "Sperr-Uhr",
            "Vollbild-Uhr, die auch bei gesperrtem Gerät angezeigt wird"
        ) { startActivity(Intent(this, LockClockActivity::class.java)) })

        page.addView(card(
            "PixelLock als Startbildschirm festlegen",
            "Öffnet die Android-Einstellung für die Standard-Home-App"
        ) { startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) })

        page.addView(MaterialTextView(this).apply {
            text = "Tipp: Das Uhr-Widget findest du beim Hinzufügen von Widgets unter „PixelLock“."
            textSize = 14f
            setPadding(0, dp(16), 0, 0)
        })
    }

    private fun card(title: String, desc: String, onClick: () -> Unit): MaterialCardView {
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
            radius = dp(16).toFloat()
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
        }
        col.addView(MaterialTextView(this).apply { text = title; textSize = 18f })
        col.addView(MaterialTextView(this).apply {
            text = desc
            textSize = 14f
            alpha = 0.7f
            setPadding(0, dp(4), 0, 0)
        })
        card.addView(col)
        return card
    }
}
