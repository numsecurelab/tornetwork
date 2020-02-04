package io.horizontalsystems.netkit

import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.IOException


class TorVpnService : VpnService() {

    private var builder: Builder? = null
    private var  parcelFileDescriptor: ParcelFileDescriptor? = null

    private val TAG = "TorVpnService"
//    private val binder = LocalBinder()

    override fun onCreate() {
        builder = Builder()
        builder?.addAddress("10.1.10.1", 32)
        builder?.addRoute("0.0.0.0", 0)

        val packageName = applicationContext.packageName
        Log.e(TAG, "packageName: $packageName")
        builder?.addAllowedApplication(packageName)

        parcelFileDescriptor = builder?.establish()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "Received $intent")

        if (intent == null) {
            return START_NOT_STICKY
        }

        if (ACTION_START == intent.action) {
            start()
            startForeground(NOTIFICATION_ID, getNotification())
        } else if (ACTION_STOP == intent.action) {
            stop()
        }

        return super.onStartCommand(intent, flags, startId)
    }

//    override fun onBind(intent: Intent): IBinder {
//        return binder
//    }

    private fun start() {
        Log.e(TAG, "onStart called")
        //start TOR

    }

    private fun stop(){
        //stop TOR

        Log.e(TAG, "stop called")

        try {
            parcelFileDescriptor?.close()
        } catch (ex: IOException) {
            Log.e(TAG,"parcelFileDescriptor?.close()", ex)
        }

        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy called")
        super.onDestroy()
    }

    private fun getNotification(): Notification {
        return NotificationUtils.getNotification(
            context = this,
            pendingIntent = getPendingIntent(),
            title = "Foreground Service",
            description = "NotificationDescription"
        )
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent()
        val packageName: String = applicationContext.packageName
        val intentClass: String =
            applicationContext.resources.getString(R.string.foreground_notification_class_name)
        intent.component = ComponentName(packageName, packageName + intentClass)
        return PendingIntent.getActivity(this, 0, intent, 0)
    }

//    inner class LocalBinder : Binder() {
//        // Return this instance of LocalService so clients can call public methods
//        fun getService(): TorVpnService = this@TorVpnService
//    }

    companion object {
        private const val ACTION_START = "start"
        private const val ACTION_STOP = "stop"
        private const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            val intent = Intent(context, TorVpnService::class.java)
            intent.action = ACTION_START
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, TorVpnService::class.java)
            intent.action = ACTION_STOP
            context.startService(intent)
        }
    }
}
