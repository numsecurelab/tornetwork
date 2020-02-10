package io.horizontalsystems.netkit

import android.app.NotificationManager
import android.content.Context
import android.content.Entity
import androidx.core.app.NotificationCompat
import io.horizontalsystems.tor.ConnectionStatus
import io.horizontalsystems.tor.R
import io.horizontalsystems.tor.Tor
import io.horizontalsystems.netkit.utils.NotificationUtils
import io.horizontalsystems.tor.EntityState

class NetNotifManager(private val context: Context) : Tor.Listener {

    private val TOR_CHANNEL_ID = "io.horizontalsystems.netkit.tor.channelId"
    private val TOR_SERVICE_NOTIFICATION_ID = 1
    private var lastConnectionStatus = ConnectionStatus.UNDEFINED
    private val iconConnecting = R.drawable.ic_tor
    private val iconConnected = R.drawable.ic_tor_running
    private val iconError = R.drawable.ic_tor_error

    init {
        NotificationUtils.createNotificationChannel(context, "io.horizontalsystems", TOR_CHANNEL_ID)
    }

    private fun updateNotification(torInfo: Tor.Info, contentParam: String) {

        var content = contentParam
        var title: String = ""
        var icon: Int = iconConnecting

        if (torInfo.state == EntityState.STARTING) {
            title = "Tor: Starting"
        } else if (torInfo.state == EntityState.RUNNING) {
            title = "Tor: Running"
        } else {
            title = "Tor: Stopped"
        }

        if (torInfo.connectionInfo.getConnectionStatus() == ConnectionStatus.CONNECTING) {
            content = "Connecting ... "
        } else if (torInfo.connectionInfo.getConnectionStatus() == ConnectionStatus.CONNECTED) {
            content = "Successfully Connected."
            icon = iconConnected
        } else {
            content = "Disconnected"
        }

        val notification: NotificationCompat.Builder = NotificationCompat.Builder(context, TOR_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setOngoing(true)
                //.setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
                .setGroup("Tor")
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setGroupSummary(false)
                .setSmallIcon(icon)

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotificationManager.notify(TOR_SERVICE_NOTIFICATION_ID, notification.build())
    }

    override fun onProcessStatusUpdate(torInfo: Tor.Info?, message: String) {
        torInfo?.let {
            updateNotification(torInfo, message)
        }
    }

    override fun onConnStatusUpdate(torConnInfo: Tor.ConnectionInfo?, message: String) {
        torConnInfo?.let {
            if (torConnInfo.getConnectionStatus() != lastConnectionStatus) {
                updateNotification(Tor.Info(torConnInfo), message)
            }

            lastConnectionStatus = torConnInfo.getConnectionStatus()
        }
    }
}