package de.pixellock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/** Uhr-Widget für den Startbildschirm; Tippen öffnet PixelLock. */
class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pending = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val color = Prefs.clockColor(context)
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_clock)
            views.setOnClickPendingIntent(R.id.widget_root, pending)
            views.setTextColor(R.id.widget_time, color)
            views.setTextColor(R.id.widget_date, color)
            manager.updateAppWidget(id, views)
        }
    }

    companion object {
        /** Aktualisiert alle platzierten Widgets (z. B. nach einer Farbänderung). */
        fun refresh(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, ClockWidgetProvider::class.java))
            if (ids.isNotEmpty()) ClockWidgetProvider().onUpdate(context, mgr, ids)
        }
    }
}
