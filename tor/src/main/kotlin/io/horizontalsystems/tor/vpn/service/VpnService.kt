package io.horizontalsystems.tor.vpn.service

import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import io.horizontalsystems.tor.R
import io.horizontalsystems.tor.utils.NotificationUtils
import io.horizontalsystems.tor.vpn.Vpn
import io.horizontalsystems.tor.vpn.VpnManager
import java.io.IOException
import java.util.concurrent.TimeoutException


class TorVpnService : VpnService() {

    val TAG = "TorVpnService"

    val ACTION_START = "start"
    val ACTION_STOP = "stop"

    var mVpnManager: VpnManager? = null


    override fun onCreate() {
        super.onCreate()

        try {
            mVpnManager = VpnManager(this, torSettingsGlobal!!)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: TimeoutException) {
            e.printStackTrace()
        }
    }

    private var builder: Builder? = null
    private var  parcelFileDescriptor: ParcelFileDescriptor? = null


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

        mVpnManager!!.handleIntent(Builder(), intent)


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
            description = "NotificationDescription",
            channelId = "io.horizontalsystem.netkit.vpn.channelId"
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
        var torSettingsGlobal: Vpn.Settings? = null


        fun start(torSettings: Vpn.Settings) {

            torSettingsGlobal = torSettings
            val intent = Intent(torSettings.context, TorVpnService::class.java)
            intent.action = ACTION_START
            torSettings.context.startForegroundService(intent)
        }

        fun stop(torSettings: Vpn.Settings) {
            torSettingsGlobal = torSettings
            val intent = Intent(torSettings.context, TorVpnService::class.java)
            intent.action = ACTION_STOP
            torSettings.context.startService(intent)
        }
    }
}
