package de.pixellock

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.R as M

/** Hub: zwei Reiter („Make up“ / „Life up“) mit allen Modulen, ähnlich wie Good Lock. */
class MainActivity : AppCompatActivity() {

    private lateinit var content: LinearLayout
    private var tab = 0

    private class Mod(
        val title: String, val desc: String, val icon: Int,
        val target: Class<*>? = null, val reason: String? = null
    )

    private val makeUp = listOf(
        Mod("Home Up", "Raster, Icon-Form, Dock, Suche und Gesten des Startbildschirms",
            R.drawable.ic_home, HomeSettingsActivity::class.java),
        Mod("ClockFace", "Uhr-Stile, Farben und Widget",
            R.drawable.ic_clock, ClockSettingsActivity::class.java),
        Mod("LockStar", "Sperr-Uhr über dem Sperrbildschirm und Schnelleinstellungen-Kachel",
            R.drawable.ic_lock, LockSettingsActivity::class.java),
        Mod("Theme Park", "Hell/Dunkel und Material-You-Farben",
            R.drawable.ic_palette, ThemeParkActivity::class.java)
    )

    private val makeUpOff = listOf(
        Mod("NavStar", "Navigationsleiste anpassen", R.drawable.ic_info, null,
            "Die Navigationsleiste und Gesten gehören dem System. Normale Apps können sie auf dem Pixel nicht verändern."),
        Mod("QuickStar", "Schnelleinstellungen umgestalten", R.drawable.ic_info, null,
            "Das Aussehen der Schnelleinstellungen kann keine App ändern. Eine eigene Kachel gibt es stattdessen unter LockStar."),
        Mod("Keys Cafe", "Tasten umbelegen", R.drawable.ic_info, null,
            "Hardware-Tasten wie die Power-Taste lassen sich ohne Root oder besondere Rechte nicht umbelegen."),
        Mod("MultiStar", "Multi-Window anpassen", R.drawable.ic_info, null,
            "Das Verhalten von Mehrfenster und Pop-ups steuert Android selbst.")
    )

    private val lifeUp = listOf(
        Mod("NotiStar", "Verlauf aller Benachrichtigungen, auch gelöschter",
            R.drawable.ic_bell, NotiStarActivity::class.java)
    )

    private val lifeUpOff = listOf(
        Mod("Routines+", "Automatisierungen", R.drawable.ic_info, null,
            "Systemweite Automatisierungen brauchen Rechte, die Apps auf dem Pixel nicht haben. Alternativen: Pixel-Routinen oder Tasker."),
        Mod("Sound Assistant", "Lautstärke pro App", R.drawable.ic_info, null,
            "Die Lautstärke einzelner Apps kann eine normale App unter Android nicht steuern.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(24))
        }
        root.addView(
            ScrollView(this).apply { addView(content) },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        )
        val nav = BottomNavigationView(this)
        nav.inflateMenu(R.menu.hub_nav)
        nav.setOnItemSelectedListener {
            tab = if (it.itemId == R.id.nav_make) 0 else 1
            render()
            true
        }
        root.addView(
            nav,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        content.removeAllViews()
        content.addView(Ui.title(this, if (tab == 0) "Make up" else "Life up"))
        content.addView(Ui.body(this,
            if (tab == 0) "Gestalte dein Pixel nach deinen Vorstellungen."
            else "Praktische Helfer für den Alltag."))

        if (tab == 0) content.addView(statusCard())

        content.addView(Ui.section(this, "Module"))
        content.addView(Ui.group(this, (if (tab == 0) makeUp else lifeUp).map { moduleRow(it) }))

        content.addView(Ui.section(this, "Auf dem Pixel nicht verfügbar"))
        content.addView(Ui.group(this, (if (tab == 0) makeUpOff else lifeUpOff).map { moduleRow(it) }))
        content.addView(Ui.note(this,
            "Diese Good-Lock-Module nutzen Samsung-eigene Schnittstellen. Tippe auf einen Eintrag für Details."))
    }

    private fun moduleRow(m: Mod) = Ui.moduleRow(this, m.icon, m.title, m.desc, m.target != null) {
        if (m.target != null) {
            startActivity(Intent(this, m.target))
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle(m.title)
                .setMessage(m.reason)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun isDefaultHome(): Boolean {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val ri = packageManager.resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY)
        return ri?.activityInfo?.packageName == packageName
    }

    private fun statusCard(): MaterialCardView {
        val active = isDefaultHome()
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(16) }
            radius = dp(24).toFloat()
            cardElevation = 0f
            setCardBackgroundColor(Ui.color(this@MainActivity, M.attr.colorPrimaryContainer))
        }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(18))
        }
        val onColor = Ui.color(this, M.attr.colorOnPrimaryContainer)
        col.addView(android.widget.TextView(this).apply {
            text = if (active) "Home Up ist aktiv" else "Home Up ist nicht aktiv"
            setTextAppearance(M.style.TextAppearance_Material3_TitleMedium)
            setTextColor(onColor)
        })
        col.addView(android.widget.TextView(this).apply {
            text = if (active) "PixelLock ist dein Standard-Startbildschirm."
            else "Lege PixelLock als Standard-Startbildschirm fest, damit Raster, Dock und Suche genutzt werden."
            setTextAppearance(M.style.TextAppearance_Material3_BodyMedium)
            setTextColor(onColor)
            setPadding(0, dp(4), 0, dp(8))
        })
        if (!active) {
            col.addView(Ui.button(this, "Jetzt festlegen") {
                startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            })
        }
        card.addView(col)
        return card
    }
}
