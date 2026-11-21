package de.pixellock

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.color.MaterialColors
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.google.android.material.R as M

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

/** Kleine Bibliothek für einheitliche Material-3-Bausteine (Karten, Zeilen, Regler …). */
object Ui {
    private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

    fun color(ctx: Context, attr: Int): Int = MaterialColors.getColor(ctx, attr, Color.GRAY)

    fun ripple(ctx: Context): Drawable? {
        val ta = ctx.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        val d = ta.getDrawable(0)
        ta.recycle()
        return d
    }

    fun title(ctx: Context, s: CharSequence): TextView = TextView(ctx).apply {
        text = s
        setTextAppearance(M.style.TextAppearance_Material3_HeadlineMedium)
        setTextColor(color(ctx, M.attr.colorOnSurface))
    }

    fun body(ctx: Context, s: CharSequence): TextView = TextView(ctx).apply {
        text = s
        setTextAppearance(M.style.TextAppearance_Material3_BodyMedium)
        setTextColor(color(ctx, M.attr.colorOnSurfaceVariant))
    }

    fun section(ctx: Context, s: CharSequence): TextView = TextView(ctx).apply {
        text = s
        setTextAppearance(M.style.TextAppearance_Material3_LabelLarge)
        setTextColor(color(ctx, M.attr.colorPrimary))
        setPadding(ctx.dp(8), ctx.dp(16), 0, ctx.dp(8))
    }

    fun note(ctx: Context, s: CharSequence): TextView = TextView(ctx).apply {
        text = s
        setTextAppearance(M.style.TextAppearance_Material3_BodySmall)
        setTextColor(color(ctx, M.attr.colorOnSurfaceVariant))
        setPadding(ctx.dp(8), ctx.dp(4), ctx.dp(8), ctx.dp(8))
    }

    private fun rowBox(ctx: Context): LinearLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(ctx.dp(16), ctx.dp(14), ctx.dp(16), ctx.dp(14))
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
    }

    private fun textCol(ctx: Context, title: String, sub: String?): LinearLayout {
        val col = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, WRAP, 1f)
        }
        col.addView(TextView(ctx).apply {
            text = title
            setTextAppearance(M.style.TextAppearance_Material3_TitleMedium)
            setTextColor(color(ctx, M.attr.colorOnSurface))
        })
        if (sub != null) {
            col.addView(TextView(ctx).apply {
                text = sub
                setTextAppearance(M.style.TextAppearance_Material3_BodyMedium)
                setTextColor(color(ctx, M.attr.colorOnSurfaceVariant))
            })
        }
        return col
    }

    fun iconBadge(ctx: Context, res: Int): View {
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color(ctx, M.attr.colorSecondaryContainer))
        }
        return ImageView(ctx).apply {
            setImageResource(res)
            imageTintList = ColorStateList.valueOf(color(ctx, M.attr.colorOnSecondaryContainer))
            setPadding(ctx.dp(10), ctx.dp(10), ctx.dp(10), ctx.dp(10))
            background = bg
            layoutParams = LinearLayout.LayoutParams(ctx.dp(44), ctx.dp(44)).apply {
                marginEnd = ctx.dp(16)
            }
        }
    }

    /** Karte, die mehrere Zeilen gruppiert (wie bei Good Lock / Android-Einstellungen). */
    fun group(ctx: Context, rows: List<View>): MaterialCardView {
        val card = MaterialCardView(ctx, null, M.attr.materialCardViewFilledStyle).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH, WRAP).apply { bottomMargin = ctx.dp(12) }
            radius = ctx.dp(24).toFloat()
        }
        val col = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        val line = ColorUtils.setAlphaComponent(color(ctx, M.attr.colorOnSurfaceVariant), 40)
        rows.forEachIndexed { i, v ->
            if (i > 0) {
                col.addView(View(ctx).apply {
                    setBackgroundColor(line)
                    layoutParams = LinearLayout.LayoutParams(MATCH, 1).apply {
                        marginStart = ctx.dp(16)
                        marginEnd = ctx.dp(16)
                    }
                })
            }
            col.addView(v)
        }
        card.addView(col)
        return card
    }

    fun moduleRow(
        ctx: Context, icon: Int, title: String, desc: String,
        enabled: Boolean, onClick: () -> Unit
    ): View {
        val row = rowBox(ctx)
        row.addView(iconBadge(ctx, icon))
        row.addView(textCol(ctx, title, desc))
        row.addView(ImageView(ctx).apply {
            setImageResource(if (enabled) R.drawable.ic_chevron else R.drawable.ic_info)
            imageTintList = ColorStateList.valueOf(color(ctx, M.attr.colorOnSurfaceVariant))
        })
        row.alpha = if (enabled) 1f else 0.6f
        row.background = ripple(ctx)
        row.setOnClickListener { onClick() }
        return row
    }

    fun actionRow(ctx: Context, title: String, sub: String?, onClick: () -> Unit): View {
        val row = rowBox(ctx)
        row.addView(textCol(ctx, title, sub))
        row.addView(ImageView(ctx).apply {
            setImageResource(R.drawable.ic_chevron)
            imageTintList = ColorStateList.valueOf(color(ctx, M.attr.colorOnSurfaceVariant))
        })
        row.background = ripple(ctx)
        row.setOnClickListener { onClick() }
        return row
    }

    fun switchRow(
        ctx: Context, title: String, sub: String?, checked: Boolean, onChange: (Boolean) -> Unit
    ): View {
        val row = rowBox(ctx)
        val sw = MaterialSwitch(ctx)
        sw.isChecked = checked
        row.addView(textCol(ctx, title, sub))
        row.addView(sw)
        sw.setOnCheckedChangeListener { _, c -> onChange(c) }
        row.background = ripple(ctx)
        row.setOnClickListener { sw.toggle() }
        return row
    }

    fun sliderRow(
        ctx: Context, title: String, lo: Int, hi: Int, current: Int, unit: String,
        onChange: (Int) -> Unit
    ): View {
        val box = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dp(16), ctx.dp(14), ctx.dp(16), ctx.dp(4))
            layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        }
        val head = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
        head.addView(textCol(ctx, title, null))
        val valueLabel = TextView(ctx).apply {
            text = "$current$unit"
            setTextAppearance(M.style.TextAppearance_Material3_LabelLarge)
            setTextColor(color(ctx, M.attr.colorPrimary))
        }
        head.addView(valueLabel)
        val slider = Slider(ctx)
        slider.valueFrom = lo.toFloat()
        slider.valueTo = hi.toFloat()
        slider.stepSize = 1f
        slider.value = current.toFloat()
        slider.addOnChangeListener { _, x, fromUser ->
            valueLabel.text = "${x.toInt()}$unit"
            if (fromUser) onChange(x.toInt())
        }
        box.addView(head)
        box.addView(slider)
        return box
    }

    fun chipRow(
        ctx: Context, title: String, options: List<String>, selected: Int,
        onSelect: (Int) -> Unit
    ): View {
        val box = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dp(16), ctx.dp(14), ctx.dp(16), ctx.dp(10))
            layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        }
        box.addView(textCol(ctx, title, null))
        val scroll = HorizontalScrollView(ctx).apply { isHorizontalScrollBarEnabled = false }
        val chips = ChipGroup(ctx)
        chips.isSingleLine = true
        chips.isSingleSelection = true
        chips.isSelectionRequired = true
        options.forEachIndexed { i, name ->
            val chip = Chip(ctx)
            chip.text = name
            chip.isCheckable = true
            chips.addView(chip)
            chip.isChecked = i == selected
            chip.setOnCheckedChangeListener { _, c -> if (c) onSelect(i) }
        }
        scroll.addView(chips)
        box.addView(scroll)
        return box
    }

    fun colorRow(
        ctx: Context, title: String, sub: String?, colors: IntArray, selected: Int,
        resolve: (Int) -> Int, onSelect: (Int) -> Unit
    ): View {
        val box = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dp(16), ctx.dp(14), ctx.dp(16), ctx.dp(14))
            layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        }
        box.addView(textCol(ctx, title, sub))
        val scroll = HorizontalScrollView(ctx).apply { isHorizontalScrollBarEnabled = false }
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, ctx.dp(10), 0, 0)
        }
        val dots = ArrayList<GradientDrawable>()
        var current = selected
        val primary = color(ctx, M.attr.colorPrimary)
        fun refresh() {
            dots.forEachIndexed { i, d ->
                if (colors[i] == current) d.setStroke(ctx.dp(3), primary)
                else d.setStroke(ctx.dp(1), 0x55888888)
            }
        }
        colors.forEach { col ->
            val d = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(resolve(col))
            }
            dots.add(d)
            row.addView(View(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(ctx.dp(36), ctx.dp(36)).apply {
                    marginEnd = ctx.dp(12)
                }
                background = d
                setOnClickListener {
                    current = col
                    refresh()
                    onSelect(col)
                }
            })
        }
        refresh()
        scroll.addView(row)
        box.addView(scroll)
        return box
    }

    fun button(ctx: Context, label: String, outlined: Boolean = false, onClick: () -> Unit): MaterialButton {
        val b = if (outlined) MaterialButton(ctx, null, M.attr.materialButtonOutlinedStyle)
        else MaterialButton(ctx)
        b.text = label
        b.layoutParams = LinearLayout.LayoutParams(WRAP, WRAP)
        b.setOnClickListener { onClick() }
        return b
    }
}
