package de.pixellock

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Vollbild-Uhr (wie ein Always-on-Display). Wird auch bei gesperrtem Gerät angezeigt;
 * ein Tippen schließt sie wieder. Ersetzt NICHT den echten Android-Sperrbildschirm.
 */
class LockClockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.attributes = window.attributes.apply { screenBrightness = 0.25f }

        val clock = ClockFaceView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.BLACK)
            style = Prefs.getInt(this@LockClockActivity, Prefs.CLOCK_STYLE, 0)
            clockColor = Prefs.getInt(this@LockClockActivity, Prefs.CLOCK_COLOR, Color.WHITE)
            showSeconds = Prefs.getBool(this@LockClockActivity, Prefs.CLOCK_SECONDS, false)
            setOnClickListener { finish() }
        }
        setContentView(clock)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
