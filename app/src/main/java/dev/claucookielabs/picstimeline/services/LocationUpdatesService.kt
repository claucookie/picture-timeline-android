package dev.claucookielabs.picstimeline.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder


class LocationUpdatesService : Service() {

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        val service: LocationUpdatesService
            get() {
                return this@LocationUpdatesService
            }
    }
}
