package de.pixellock

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.ShapeAppearanceModel

data class AppEntry(
    val label: String,
    val pkg: String,
    val original: Drawable,
    val square: Bitmap?,
    val intent: Intent
)

/** Erzeugt ein Icon in der gewählten Form (0 Original, 1 Kreis, 2 Squircle, 3 Abgerundet). */
fun makeIcon(ctx: Context, e: AppEntry, shape: Int, sizeDp: Int): ImageView {
    val lp = LinearLayout.LayoutParams(ctx.dp(sizeDp), ctx.dp(sizeDp))
    if (shape == 0 || e.square == null) {
        return ImageView(ctx).apply {
            setImageDrawable(e.original)
            layoutParams = lp
        }
    }
    val corner = when (shape) {
        1 -> 0.5f
        2 -> 0.34f
        else -> 0.2f
    }
    return ShapeableImageView(ctx).apply {
        shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes(RelativeCornerSize(corner))
            .build()
        setImageBitmap(e.square)
        layoutParams = lp
    }
}

/** Modul „Home Up“: eigener Startbildschirm mit Uhr, Suche, App-Raster und Dock. */
class HomeActivity : AppCompatActivity() {

    private lateinit var root: LinearLayout
    private lateinit var clock: ClockFaceView
    private lateinit var searchBox: View
    private lateinit var search: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var dockBox: LinearLayout

    private var allApps: List<AppEntry> = emptyList()
    private var dirty = true
    private var loading = false

    private val adapter = AppAdapter(
        onClick = { launch(it) },
        onLong = { showMenu(it) }
    )

    private val pkgReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            dirty = true
            loadApps()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Zurück-Taste soll auf dem Startbildschirm nichts tun
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })

        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        // Uhr (Wischen nach unten = Benachrichtigungen, langer Druck = PixelLock)
        clock = ClockFaceView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(170))
        }
        val detector = GestureDetectorCompat(this, object : android.view.GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {
                if (velocityY > 1200 && Prefs.getBool(this@HomeActivity, Prefs.GESTURE, true)) {
                    expandNotifications()
                    return true
                }
                return false
            }

            override fun onLongPress(e: MotionEvent) {
                startActivity(Intent(this@HomeActivity, MainActivity::class.java))
            }
        })
        clock.setOnTouchListener { _, ev -> detector.onTouchEvent(ev) }

        // Suchleiste
        search = EditText(this).apply {
            hint = "Apps suchen"
            setHintTextColor(0xAAFFFFFF.toInt())
            setTextColor(Color.WHITE)
            textSize = 16f
            maxLines = 1
            isSingleLine = true
            background = null
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) = updateList()
            })
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    adapter.first()?.let { launch(it) }
                    true
                } else false
            }
        }
        searchBox = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(4), dp(18), dp(4))
            background = GradientDrawable().apply {
                setColor(0x44000000)
                cornerRadius = dp(28).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dp(16)
                marginEnd = dp(16)
                bottomMargin = dp(8)
            }
            addView(ImageView(this@HomeActivity).apply {
                setImageResource(R.drawable.ic_search)
                imageTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(dp(20), dp(20)).apply { marginEnd = dp(12) }
            })
            addView(search)
        }

        recycler = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            adapter = this@HomeActivity.adapter
            clipToPadding = false
        }

        // Dock
        dockBox = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(10), dp(8), dp(10))
            background = GradientDrawable().apply {
                setColor(0x55000000)
                cornerRadius = dp(32).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dp(12)
                marginEnd = dp(12)
                topMargin = dp(8)
                bottomMargin = dp(8)
            }
        }

        root.addView(clock)
        root.addView(searchBox)
        root.addView(recycler)
        root.addView(dockBox)
        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, b.top, 0, b.bottom)
            insets
        }

        val f = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        registerReceiver(pkgReceiver, f)
    }

    override fun onDestroy() {
        unregisterReceiver(pkgReceiver)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        root.setBackgroundColor(Color.argb(Prefs.getInt(this, Prefs.DIM, 25) * 255 / 100, 0, 0, 0))

        clock.loadPrefs()
        clock.visibility = if (Prefs.getBool(this, Prefs.HOME_CLOCK, true)) View.VISIBLE else View.GONE
        searchBox.visibility = if (Prefs.getBool(this, Prefs.SEARCH, true)) View.VISIBLE else View.GONE

        recycler.layoutManager = GridLayoutManager(this, Prefs.getInt(this, Prefs.COLUMNS, 5))
        adapter.iconDp = Prefs.getInt(this, Prefs.ICON_DP, 56)
        adapter.showLabels = Prefs.getBool(this, Prefs.LABELS, true)
        adapter.shape = Prefs.getInt(this, Prefs.SHAPE, 1)

        if (dirty || allApps.isEmpty()) loadApps() else {
            updateList()
            refreshDock()
        }
    }

    override fun onStop() {
        search.setText("")
        super.onStop()
    }

    private fun loadApps() {
        if (loading) return
        loading = true
        Thread {
            val pm = packageManager
            val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val list = pm.queryIntentActivities(query, 0).map { ri ->
                val icon = ri.loadIcon(pm)
                AppEntry(
                    label = ri.loadLabel(pm).toString(),
                    pkg = ri.activityInfo.packageName,
                    original = icon,
                    square = toSquare(icon, 192),
                    intent = Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setClassName(ri.activityInfo.packageName, ri.activityInfo.name)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }.filter { it.pkg != packageName }.sortedBy { it.label.lowercase() }
            runOnUiThread {
                allApps = list
                dirty = false
                loading = false
                updateList()
                refreshDock()
            }
        }.start()
    }

    /** Rendert ein Icon als quadratisches Bild, damit die gewählte Form darauf angewendet werden kann. */
    private fun toSquare(d: Drawable, px: Int): Bitmap? {
        return try {
            val bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp)
            if (d is AdaptiveIconDrawable) {
                // sichtbarer Bereich = mittlere 72 von 108 Einheiten
                val full = px * 108 / 72
                val off = (px - full) / 2
                d.background?.let {
                    it.setBounds(off, off, off + full, off + full)
                    it.draw(c)
                }
                d.foreground?.let {
                    it.setBounds(off, off, off + full, off + full)
                    it.draw(c)
                }
            } else {
                c.drawColor(0xFFECECEC.toInt())
                val inset = px * 15 / 100
                d.setBounds(inset, inset, px - inset, px - inset)
                d.draw(c)
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }

    private fun updateList() {
        val hidden = Prefs.getSet(this, Prefs.HIDDEN)
        val q = search.text.toString().trim().lowercase()
        adapter.submit(allApps.filter {
            it.pkg !in hidden && (q.isEmpty() || it.label.lowercase().contains(q))
        })
    }

    private fun dockPackages(): List<String> =
        Prefs.getString(this, Prefs.DOCK_APPS, "").split(",").filter { it.isNotBlank() }

    private fun refreshDock() {
        dockBox.removeAllViews()
        if (!Prefs.getBool(this, Prefs.DOCK, true)) {
            dockBox.visibility = View.GONE
            return
        }
        dockBox.visibility = View.VISIBLE
        val entries = dockPackages().mapNotNull { p -> allApps.firstOrNull { it.pkg == p } }.take(5)
        if (entries.isEmpty()) {
            dockBox.addView(TextView(this).apply {
                text = "App lange gedrückt halten → „Zum Dock hinzufügen“"
                setTextColor(0xCCFFFFFF.toInt())
                textSize = 13f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            })
            return
        }
        val shape = Prefs.getInt(this, Prefs.SHAPE, 1)
        val size = Prefs.getInt(this, Prefs.ICON_DP, 56)
        entries.forEach { e ->
            val cell = LinearLayout(this).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { launch(e) }
                setOnLongClickListener { showMenu(e); true }
            }
            cell.addView(makeIcon(this, e, shape, size))
            dockBox.addView(cell)
        }
    }

    private fun launch(e: AppEntry) {
        try {
            startActivity(e.intent)
        } catch (ex: Exception) {
            Toast.makeText(this, "App konnte nicht gestartet werden", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMenu(app: AppEntry) {
        val inDock = app.pkg in dockPackages()
        val items = arrayOf(
            "App-Info",
            "Deinstallieren",
            if (inDock) "Aus Dock entfernen" else "Zum Dock hinzufügen",
            "Ausblenden",
            "PixelLock-Einstellungen"
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(app.label)
            .setItems(items) { _, which ->
                val uri = Uri.parse("package:${app.pkg}")
                when (which) {
                    0 -> startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))
                    1 -> startActivity(Intent(Intent.ACTION_DELETE, uri))
                    2 -> toggleDock(app, inDock)
                    3 -> {
                        val h = Prefs.getSet(this, Prefs.HIDDEN)
                        h.add(app.pkg)
                        Prefs.putSet(this, Prefs.HIDDEN, h)
                        updateList()
                        Toast.makeText(
                            this, "Ausgeblendet. Zurückholen unter Home Up → Einstellungen.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> startActivity(Intent(this, MainActivity::class.java))
                }
            }
            .show()
    }

    private fun toggleDock(app: AppEntry, inDock: Boolean) {
        val list = dockPackages().toMutableList()
        if (inDock) {
            list.remove(app.pkg)
        } else {
            if (list.size >= 5) {
                Toast.makeText(this, "Das Dock ist voll (max. 5 Apps)", Toast.LENGTH_SHORT).show()
                return
            }
            list.add(app.pkg)
        }
        Prefs.putString(this, Prefs.DOCK_APPS, list.joinToString(","))
        refreshDock()
    }

    private fun expandNotifications() {
        try {
            val sb = getSystemService("statusbar")
            Class.forName("android.app.StatusBarManager")
                .getMethod("expandNotificationsPanel")
                .invoke(sb)
        } catch (t: Throwable) {
            // wird vom System ggf. nicht erlaubt – dann passiert nichts
        }
    }
}

class AppAdapter(
    private val onClick: (AppEntry) -> Unit,
    private val onLong: (AppEntry) -> Unit
) : RecyclerView.Adapter<AppAdapter.VH>() {

    private var items: List<AppEntry> = emptyList()
    var iconDp = 56
    var showLabels = true
    var shape = 1

    fun first(): AppEntry? = items.firstOrNull()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<AppEntry>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(val root: LinearLayout) : RecyclerView.ViewHolder(root)

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
        return VH(root)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val e = items[position]
        val c = h.root.context
        h.root.removeAllViews()
        h.root.addView(makeIcon(c, e, shape, iconDp))
        if (showLabels) {
            h.root.addView(TextView(c).apply {
                text = e.label
                setTextColor(Color.WHITE)
                textSize = 12f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                gravity = Gravity.CENTER
                setShadowLayer(4f, 0f, 1f, Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = c.dp(4) }
            })
        }
        h.root.setOnClickListener { onClick(e) }
        h.root.setOnLongClickListener { onLong(e); true }
    }

    override fun getItemCount() = items.size
}
