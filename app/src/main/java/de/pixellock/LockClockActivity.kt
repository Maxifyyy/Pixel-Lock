package de.pixellock

import android.graphics.Color
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.random.Random

/**
 * Vollbild-Uhr (wie ein Always-on-Display). Wird auch bei gesperrtem Gerät angezeigt;
 * ein Tippen schließt sie wieder. Ersetzt NICHT den echten Android-Sperrbildschirm.
 */
class LockClockActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clock: ClockFaceView
    private var battery: TextView? = null

    private val shifter = object : Runnable {
        override fun run() {
            // Pixel-Shift gegen Einbrennen: Uhr wandert jede Minute ein kleines Stück
            clock.translationX = Random.nextInt(-dp(24), dp(24) + 1).toFloat()
            clock.translationY = Random.nextInt(-dp(32), dp(32) + 1).toFloat()
            updateBattery()
            handler.postDelayed(this, 60_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        if (Prefs.getBool(this, Prefs.LOCK_KEEP_ON, true)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        window.attributes = window.attributes.apply {
            screenBrightness = Prefs.getInt(this@LockClockActivity, Prefs.LOCK_BRIGHT, 25) / 100f
        }

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            setOnClickListener { finish() }
        }
        clock = ClockFaceView(this).apply { loadPrefs() }
        root.addView(
            clock,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        if (Prefs.getBool(this, Prefs.LOCK_BATTERY, true)) {
            battery = TextView(this).apply {
                setTextColor(0x99FFFFFF.toInt())
                textSize = 16f
            }
            root.addView(
                battery,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    bottomMargin = dp(48)
                }
            )
            updateBattery()
        }
        setContentView(root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onResume() {
        super.onResume()
        if (Prefs.getBool(this, Prefs.LOCK_SHIFT, true)) handler.post(shifter)
        else handler.post { updateBattery() }
    }

    override fun onPause() {
        handler.removeCallbacksAndMessages(null)
        super.onPause()
    }

    private fun updateBattery() {
        val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        battery?.text = "Akku $level %"
    }
}
