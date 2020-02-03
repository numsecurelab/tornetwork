package io.horizontalsystems.tor.service

import android.app.Service
import android.content.Intent
import android.os.IBinder


class TorService : Service() {

    companion object{
        private val CHANNEL_ID = "HSTorServiceChannel"
        private const val TAG_FOREGROUND_SERVICE = "HSTorService"
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Return the communication channel to the service.")
    }

}