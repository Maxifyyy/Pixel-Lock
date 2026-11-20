package de.pixellock

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class AppEntry(val label: String, val pkg: String, val icon: Drawable, val intent: Intent)

/** Modul „Home Up“: eigener Startbildschirm mit App-Raster und Uhr. */
class HomeActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var clock: ClockFaceView
    private val appAdapter = AppAdapter(
        onClick = { startActivity(it.intent) },
        onLong = { showAppMenu(it) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Zurück-Taste soll auf dem Startbildschirm nichts tun
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        clock = ClockFaceView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(200)
            )
            // Langer Druck auf die Uhr öffnet die Einstellungen
            setOnLongClickListener {
                startActivity(Intent(this@HomeActivity, MainActivity::class.java))
                true
            }
        }

        recycler = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
            adapter = appAdapter
            clipToPadding = false
        }

        root.addView(clock)
        root.addView(recycler)
        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, b.top, 0, b.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        clock.style = Prefs.getInt(this, Prefs.CLOCK_STYLE, 0)
        clock.clockColor = Prefs.getInt(this, Prefs.CLOCK_COLOR, Color.WHITE)
        clock.showSeconds = Prefs.getBool(this, Prefs.CLOCK_SECONDS, false)

        recycler.layoutManager = GridLayoutManager(this, Prefs.getInt(this, Prefs.COLUMNS, 5))
        appAdapter.iconDp = Prefs.getInt(this, Prefs.ICON_DP, 56)
        appAdapter.showLabels = Prefs.getBool(this, Prefs.LABELS, true)
        loadApps()
    }

    private fun loadApps() {
        Thread {
            val pm = packageManager
            val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val list = pm.queryIntentActivities(query, 0).map { ri ->
                AppEntry(
                    label = ri.loadLabel(pm).toString(),
                    pkg = ri.activityInfo.packageName,
                    icon = ri.loadIcon(pm),
                    intent = Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setClassName(ri.activityInfo.packageName, ri.activityInfo.name)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }.sortedBy { it.label.lowercase() }
            runOnUiThread { appAdapter.submit(list) }
        }.start()
    }

    private fun showAppMenu(app: AppEntry) {
        AlertDialog.Builder(this)
            .setTitle(app.label)
            .setItems(arrayOf("App-Info", "Deinstallieren")) { _, which ->
                val uri = Uri.parse("package:${app.pkg}")
                if (which == 0) {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))
                } else {
                    startActivity(Intent(Intent.ACTION_DELETE, uri))
                }
            }
            .show()
    }
}

class AppAdapter(
    private val onClick: (AppEntry) -> Unit,
    private val onLong: (AppEntry) -> Unit
) : RecyclerView.Adapter<AppAdapter.VH>() {

    private var items: List<AppEntry> = emptyList()
    var iconDp = 56
    var showLabels = true

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<AppEntry>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(val root: LinearLayout, val icon: ImageView, val label: TextView) :
        RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val c = parent.context
        val root = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(c.dp(4), c.dp(10), c.dp(4), c.dp(10))
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val icon = ImageView(c)
        val label = TextView(c).apply {
            setTextColor(Color.WHITE)
            textSize = 12f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.CENTER
            setShadowLayer(4f, 0f, 1f, Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = c.dp(4) }
        }
        root.addView(icon)
        root.addView(label)
        return VH(root, icon, label)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val e = items[position]
        val c = h.root.context
        h.icon.setImageDrawable(e.icon)
        h.icon.layoutParams = LinearLayout.LayoutParams(c.dp(iconDp), c.dp(iconDp))
        h.label.text = e.label
        h.label.visibility = if (showLabels) View.VISIBLE else View.GONE
        h.root.setOnClickListener { onClick(e) }
        h.root.setOnLongClickListener { onLong(e); true }
    }

    override fun getItemCount() = items.size
}
