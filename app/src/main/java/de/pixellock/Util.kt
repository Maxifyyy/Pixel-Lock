package de.pixellock

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textview.MaterialTextView

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

/** Scrollbare Seite mit Titel; gibt die Spalte zurück, in die Inhalte eingefügt werden. */
fun AppCompatActivity.scrollPage(title: String): LinearLayout {
    val col = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(24), dp(20), dp(32))
    }
    col.addView(MaterialTextView(this).apply {
        text = title
        textSize = 26f
        setPadding(0, 0, 0, dp(16))
    })
    setContentView(ScrollView(this).apply { addView(col) })
    return col
}

/** Zeile mit Beschriftung und Schieberegler. */
fun seekRow(
    ctx: Context,
    title: String,
    lo: Int,
    hi: Int,
    value: Int,
    unit: String,
    onChange: (Int) -> Unit
): View {
    val box = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, ctx.dp(8), 0, ctx.dp(8))
    }
    val label = MaterialTextView(ctx).apply {
        text = "$title: $value$unit"
        textSize = 16f
    }
    val bar = SeekBar(ctx)
    bar.max = hi - lo
    bar.progress = value - lo
    bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
            val v = p + lo
            label.text = "$title: $v$unit"
            if (fromUser) onChange(v)
        }

        override fun onStartTrackingTouch(s: SeekBar?) {}
        override fun onStopTrackingTouch(s: SeekBar?) {}
    })
    box.addView(label)
    box.addView(bar)
    return box
}
