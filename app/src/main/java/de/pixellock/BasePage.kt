package de.pixellock

import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

/** Gemeinsame Basis aller Einstellungsseiten: Toolbar mit Zurück-Pfeil + scrollbarer Inhalt. */
abstract class BasePage : AppCompatActivity() {

    protected lateinit var toolbar: MaterialToolbar

    protected fun pageRoot(title: String): LinearLayout {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        toolbar = MaterialToolbar(this).apply {
            this.title = title
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener { finish() }
        }
        root.addView(
            toolbar,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64))
        )
        setContentView(root)
        return root
    }

    protected fun scrollPage(title: String): LinearLayout {
        val root = pageRoot(title)
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(32))
        }
        val scroll = ScrollView(this).apply { addView(col) }
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        return col
    }
}
