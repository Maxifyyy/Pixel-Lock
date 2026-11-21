package de.pixellock

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateUtils
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.R as M

/** Modul „NotiStar“: durchsuchbarer Verlauf aller Benachrichtigungen. */
class NotiStarActivity : BasePage() {

    private lateinit var top: LinearLayout
    private lateinit var empty: TextView
    private val adapter = NotiAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = pageRoot("NotiStar")

        toolbar.menu.add(0, 1, 0, "Verlauf löschen")
            .setIcon(R.drawable.ic_delete)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        toolbar.setOnMenuItemClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Verlauf löschen?")
                .setMessage("Alle gespeicherten Benachrichtigungen werden entfernt.")
                .setPositiveButton("Löschen") { _, _ ->
                    NotiStore.clear(this)
                    refresh()
                }
                .setNegativeButton("Abbrechen", null)
                .show()
            true
        }

        top = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), 0)
        }
        root.addView(top)

        empty = Ui.body(this, "Noch keine Benachrichtigungen gespeichert.").apply {
            setPadding(dp(24), dp(32), dp(24), 0)
            gravity = android.view.Gravity.CENTER
        }
        root.addView(empty)

        val recycler = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@NotiStarActivity)
            adapter = this@NotiStarActivity.adapter
        }
        root.addView(recycler, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun hasAccess(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)

    private fun refresh() {
        top.removeAllViews()
        if (!hasAccess()) {
            top.addView(Ui.group(this, listOf(
                Ui.actionRow(this, "Zugriff erlauben",
                    "NotiStar braucht Zugriff auf Benachrichtigungen, um sie zu speichern.") {
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            )))
        } else {
            top.addView(Ui.group(this, listOf(
                Ui.switchRow(this, "Verlauf aufzeichnen", "Neue Benachrichtigungen speichern",
                    Prefs.getBool(this, Prefs.NOTI_ENABLED, true)) {
                    Prefs.putBool(this, Prefs.NOTI_ENABLED, it)
                }
            )))
        }
        val list = NotiStore.load(this)
        adapter.submit(list)
        empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private class NotiAdapter : RecyclerView.Adapter<NotiAdapter.VH>() {
        private var items: List<NotiEntry> = emptyList()
        private val labels = HashMap<String, String>()
        private val icons = HashMap<String, Drawable?>()

        fun submit(list: List<NotiEntry>) {
            items = list
            notifyDataSetChanged()
        }

        class VH(
            val root: LinearLayout, val icon: ImageView,
            val head: TextView, val title: TextView, val text: TextView
        ) : RecyclerView.ViewHolder(root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val c = parent.context
            val row = LinearLayout(c).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(c.dp(20), c.dp(12), c.dp(20), c.dp(12))
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            val icon = ImageView(c).apply {
                layoutParams = LinearLayout.LayoutParams(c.dp(40), c.dp(40)).apply {
                    marginEnd = c.dp(16)
                }
            }
            val col = LinearLayout(c).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val head = TextView(c).apply {
                setTextAppearance(M.style.TextAppearance_Material3_LabelMedium)
                setTextColor(Ui.color(c, M.attr.colorPrimary))
            }
            val title = TextView(c).apply {
                setTextAppearance(M.style.TextAppearance_Material3_TitleSmall)
                setTextColor(Ui.color(c, M.attr.colorOnSurface))
            }
            val text = TextView(c).apply {
                setTextAppearance(M.style.TextAppearance_Material3_BodyMedium)
                setTextColor(Ui.color(c, M.attr.colorOnSurfaceVariant))
                maxLines = 4
                ellipsize = android.text.TextUtils.TruncateAt.END
            }
            col.addView(head)
            col.addView(title)
            col.addView(text)
            row.addView(icon)
            row.addView(col)
            return VH(row, icon, head, title, text)
        }

        override fun onBindViewHolder(h: VH, position: Int) {
            val e = items[position]
            val pm = h.root.context.packageManager
            val label = labels.getOrPut(e.pkg) {
                try {
                    pm.getApplicationLabel(pm.getApplicationInfo(e.pkg, 0)).toString()
                } catch (ex: Exception) {
                    e.pkg
                }
            }
            if (!icons.containsKey(e.pkg)) {
                icons[e.pkg] = try {
                    pm.getApplicationIcon(e.pkg)
                } catch (ex: Exception) {
                    null
                }
            }
            h.icon.setImageDrawable(icons[e.pkg])
            val whenText = DateUtils.getRelativeTimeSpanString(
                e.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
            )
            h.head.text = "$label · $whenText"
            h.title.text = e.title
            h.title.visibility = if (e.title.isBlank()) View.GONE else View.VISIBLE
            h.text.text = e.text
            h.text.visibility = if (e.text.isBlank()) View.GONE else View.VISIBLE
        }

        override fun getItemCount() = items.size
    }
}
