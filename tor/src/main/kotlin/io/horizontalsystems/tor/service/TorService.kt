package io.horizontalsystems.tor.service

import android.app.*
import android.app.Notification.GROUP_ALERT_SUMMARY
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.horizontalsystems.tor.ConnectionStatus
import io.horizontalsystems.tor.R
import io.horizontalsystems.tor.Tor
import io.horizontalsystems.tor.TorManager
import io.horizontalsystems.tor.utils.NotificationUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


class TorService : Service(), Tor.Listener {

    companion object {
        const val TAG = "HSTorService"
        const val TOR_CHANNEL_ID: String = "io.horizontalsystems.netkit."
        const val TOR_SERVICE_NOTIFICATION_ID = 1
        var title = "HS-TOR"

        const val START_SERVICE = "START_SERVICE"
        const val STOP_SERVICE = "STOP_SERVICE"
        const val RESTART_SERVICE = "RESTART_SERVICE"
        const val RENEW_IDENTITY = "RENEW_IDENTITY"
        private var torSettings: Tor.Settings? = null

        fun start(torSettings: Tor.Settings) {

            this.torSettings = torSettings
            val intent = Intent(torSettings.context, TorService::class.java)
            intent.action = START_SERVICE

            torSettings.context.startForegroundService(intent)
        }

        fun stop(torSettings: Tor.Settings) {

            val intent = Intent(torSettings.context, TorService::class.java)
            intent.action = STOP_SERVICE

            torSettings.context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()

        //----------------------------------------------------
        TorManager.getInstance().torInternalListener = this
        //----------------------------------------------------

//        val notification: Notification = Notification.Builder(this, TOR_CHANNEL_ID)
//                .setContentTitle(title)
//                .setContentText("Waiting...")
//                .setOngoing(true)
//                .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
//                .setGroup("Tor")
//                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
//                .setGroupSummary(false)
//                .setSmallIcon(R.drawable.ic_tor)
//                .build()
//
//        this.startForeground(TOR_SERVICE_NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.e(TAG, "Received $intent")

        if (intent == null) {
            return START_NOT_STICKY
        }

        if (START_SERVICE == intent.action) {
            startForeground(TOR_SERVICE_NOTIFICATION_ID, getNotification())
        } else if (START_SERVICE == intent.action) {
            stopForeground(flags)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onProcessStatusUpdate(torInfo: Tor.Info?, message: String) {
        torInfo?.let {
            updateNotification(torInfo, message)
        }
    }

    override fun onConnStatusUpdate(torConnInfo: Tor.ConnectionInfo?, message: String) {
        torConnInfo?.let {
            updateNotification(Tor.Info(torConnInfo), message)
        }
    }




    private fun updateNotification(torInfo: Tor.Info, contentParam: String) {

        var content = contentParam

        if (content.isEmpty()) {
            content = "Bootstrapping..."
        }

        if (!TorManager.getInstance().torInfo.isStarted) {
            title = "Tor: Starting"
        } else {
            if (torInfo.connectionInfo.getConnectionStatus() == ConnectionStatus.CONNECTING) {
                title = "Tor: Connecting"
            } else if (torInfo.connectionInfo.getConnectionStatus() == ConnectionStatus.CONNECTED) {
                title = "Tor: Connected"
            } else
                title = "Tor: Disconnected"
        }

        val notification: NotificationCompat.Builder = NotificationCompat.Builder(this, TOR_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setOngoing(true)
                //.setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
                .setGroup("Tor")
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setGroupSummary(false)
                .setSmallIcon(R.drawable.ic_tor_running)

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager?.notify(TOR_SERVICE_NOTIFICATION_ID, notification.build())
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
}