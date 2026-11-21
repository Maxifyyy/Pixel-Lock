package de.pixellock

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/** Schnelleinstellungen-Kachel, die die Sperr-Uhr startet. */
class LockClockTileService : TileService() {

    override fun onStartListening() {
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            label = "Sperr-Uhr"
            icon = Icon.createWithResource(this@LockClockTileService, R.drawable.ic_tile_lock)
            updateTile()
        }
    }

    override fun onClick() {
        val intent = Intent(this, LockClockActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 34) {
            startActivityAndCollapse(
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
