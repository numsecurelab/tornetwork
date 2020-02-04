package io.horizontalsystems.tor.service

import android.app.Notification
import android.app.Notification.GROUP_ALERT_SUMMARY
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.horizontalsystems.tor.R
import io.horizontalsystems.tor.Tor
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


class TorService : Service(), Tor.Listener {

    companion object {
        const val TAG = "HSTorService"
        const val title = "HS-TOR"
        const val TOR_CHANNEL_ID: String = "io.horizontalsystems.netkit.demo"
        const val TOR_SERVICE_NOTIFICATION_ID = 95

        const val START_SERVICE = "START_SERVICE"
        const val STOP_SERVICE = "STOP_SERVICE"
        const val RESTART_SERVICE = "RESTART_SERVICE"
        const val RENEW_IDENTITY = "RENEW_IDENTITY"
    }

    private val mNotificationManager: NotificationManager? = null
    private val mNotifyBuilder: NotificationCompat.Builder? = null
    private val mNotification: Notification? = null
    private val mNotificationShowing = false


    private val compositeDisposable = CompositeDisposable()
    private val torDisposable: Disposable? = null

    override fun onCreate() {
        super.onCreate()

        val notification: Notification = Notification.Builder(this, TOR_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText("Waiting...")
                .setOngoing(true)
                .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
                .setGroup("Tor")
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setGroupSummary(false)
                .setSmallIcon(R.drawable.ic_stat_tor_err)
                .build()

        this.startForeground(TOR_SERVICE_NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onProcessStatusUpdate(torInfo: Tor.Info?, message: String) {
    }

    override fun onConnStatusUpdate(torConnInfo: Tor.ConnectionInfo?, message: String) {
    }
}