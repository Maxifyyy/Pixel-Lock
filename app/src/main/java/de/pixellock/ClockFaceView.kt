package de.pixellock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

/**
 * Zeichnet die Uhr in drei Stilen: 0 = Digital, 1 = Analog, 2 = Gestapelt.
 * Schriftstärke: 0 = Dünn, 1 = Normal, 2 = Fett.
 */
class ClockFaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var style: Int = 0
        set(value) { field = value; invalidate() }
    var clockColor: Int = Color.WHITE
        set(value) { field = value; invalidate() }
    var showSeconds: Boolean = false
        set(value) { field = value; invalidate() }
    var showDate: Boolean = true
        set(value) { field = value; invalidate() }
    var weight: Int = 0
        set(value) { field = value; invalidate() }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dateFormat = SimpleDateFormat("EEEE, d. MMMM", Locale.getDefault())

    private val ticker = object : Runnable {
        override fun run() {
            invalidate()
            postDelayed(this, 1000)
        }
    }

    /** Übernimmt alle gespeicherten Uhr-Einstellungen. */
    fun loadPrefs() {
        style = Prefs.getInt(context, Prefs.CLOCK_STYLE, 0)
        clockColor = Prefs.clockColor(context)
        showSeconds = Prefs.getBool(context, Prefs.CLOCK_SECONDS, false)
        showDate = Prefs.getBool(context, Prefs.CLOCK_DATE, true)
        weight = Prefs.getInt(context, Prefs.CLOCK_WEIGHT, 0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(ticker)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(ticker)
        super.onDetachedFromWindow()
    }

    private fun face(): Typeface = when (weight) {
        0 -> Typeface.create("sans-serif-light", Typeface.NORMAL)
        2 -> Typeface.create("sans-serif", Typeface.BOLD)
        else -> Typeface.create("sans-serif", Typeface.NORMAL)
    }

    override fun onDraw(canvas: Canvas) {
        val cal = Calendar.getInstance()
        val is24 = DateFormat.is24HourFormat(context)
        val hh = if (is24) cal.get(Calendar.HOUR_OF_DAY)
        else cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val mm = cal.get(Calendar.MINUTE)
        val ss = cal.get(Calendar.SECOND)

        paint.reset()
        paint.isAntiAlias = true
        paint.color = clockColor
        paint.setShadowLayer(8f, 0f, 2f, 0x99000000.toInt())

        when (style) {
            1 -> drawAnalog(canvas, cal, mm, ss)
            2 -> drawStacked(canvas, hh, mm)
            else -> drawDigital(canvas, cal, hh, mm, ss)
        }
    }

    private fun drawCentered(canvas: Canvas, text: String, cx: Float, cy: Float) {
        val y = cy - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(text, cx, y, paint)
    }

    private fun drawDigital(canvas: Canvas, cal: Calendar, hh: Int, mm: Int, ss: Int) {
        val txt = if (showSeconds) "%02d:%02d:%02d".format(hh, mm, ss) else "%02d:%02d".format(hh, mm)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = face()
        paint.textSize = 100f
        val base = paint.measureText(txt)
        val size = min(100f * width * 0.9f / base, height * 0.5f)
        paint.textSize = size
        val cy = if (showDate) height * 0.42f else height * 0.5f
        drawCentered(canvas, txt, width / 2f, cy)

        if (showDate) {
            paint.textSize = size * 0.22f
            drawCentered(canvas, dateFormat.format(cal.time), width / 2f, cy + size * 0.7f)
        }
    }

    private fun drawStacked(canvas: Canvas, hh: Int, mm: Int) {
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = face()
        paint.textSize = 100f
        val base = paint.measureText("00")
        val size = min(100f * width * 0.7f / base, height * 0.42f)
        paint.textSize = size
        drawCentered(canvas, "%02d".format(hh), width / 2f, height * 0.32f)
        paint.alpha = 190
        drawCentered(canvas, "%02d".format(mm), width / 2f, height * 0.70f)
    }

    private fun drawAnalog(canvas: Canvas, cal: Calendar, mm: Int, ss: Int) {
        val cx = width / 2f
        val cy = height / 2f
        val r = min(width, height) / 2f * 0.9f
        val thick = when (weight) { 0 -> 0.7f; 2 -> 1.5f; else -> 1f }

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = r * 0.025f * thick
        canvas.drawCircle(cx, cy, r, paint)

        for (i in 0 until 12) {
            canvas.save()
            canvas.rotate(i * 30f, cx, cy)
            val len = if (i % 3 == 0) r * 0.12f else r * 0.06f
            paint.strokeWidth = (if (i % 3 == 0) r * 0.035f else r * 0.02f) * thick
            canvas.drawLine(cx, cy - r + r * 0.06f, cx, cy - r + r * 0.06f + len, paint)
            canvas.restore()
        }

        val hourAngle = ((cal.get(Calendar.HOUR) % 12) + mm / 60f) * 30f
        val minAngle = (mm + ss / 60f) * 6f
        hand(canvas, cx, cy, hourAngle, r * 0.5f, r * 0.06f * thick)
        hand(canvas, cx, cy, minAngle, r * 0.75f, r * 0.04f * thick)

        if (showSeconds) {
            paint.color = 0xFFFF5252.toInt()
            hand(canvas, cx, cy, ss * 6f, r * 0.85f, r * 0.015f)
        }
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, r * 0.05f, paint)
    }

    private fun hand(canvas: Canvas, cx: Float, cy: Float, angle: Float, len: Float, w: Float) {
        paint.strokeWidth = w
        canvas.save()
        canvas.rotate(angle, cx, cy)
        canvas.drawLine(cx, cy, cx, cy - len, paint)
        canvas.restore()
    }
}
