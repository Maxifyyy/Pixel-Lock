package de.pixellock

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class NotiEntry(val pkg: String, val title: String, val text: String, val time: Long)

/** Speichert den Benachrichtigungsverlauf (max. 500 Einträge) als JSON-Datei. */
object NotiStore {
    private const val FILE = "noti_history.json"
    private const val MAX = 500

    private fun file(c: Context) = File(c.filesDir, FILE)

    @Synchronized
    fun load(c: Context): MutableList<NotiEntry> {
        val out = ArrayList<NotiEntry>()
        try {
            val f = file(c)
            if (!f.exists()) return out
            val arr = JSONArray(f.readText())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(
                    NotiEntry(
                        o.optString("p"), o.optString("t"), o.optString("x"), o.optLong("w")
                    )
                )
            }
        } catch (e: Exception) {
            // defekte Datei: leere Liste zurückgeben
        }
        return out
    }

    @Synchronized
    fun add(c: Context, entry: NotiEntry) {
        val list = load(c)
        val last = list.firstOrNull()
        if (last != null && last.pkg == entry.pkg && last.title == entry.title && last.text == entry.text) return
        list.add(0, entry)
        while (list.size > MAX) list.removeAt(list.size - 1)
        save(c, list)
    }

    @Synchronized
    fun clear(c: Context) {
        file(c).delete()
    }

    private fun save(c: Context, list: List<NotiEntry>) {
        try {
            val arr = JSONArray()
            list.forEach {
                arr.put(
                    JSONObject()
                        .put("p", it.pkg).put("t", it.title)
                        .put("x", it.text).put("w", it.time)
                )
            }
            file(c).writeText(arr.toString())
        } catch (e: Exception) {
            // Speichern ist Best-Effort
        }
    }
}
