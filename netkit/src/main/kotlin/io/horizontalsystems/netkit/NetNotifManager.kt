package io.horizontalsystems.netkit

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import io.horizontalsystems.tor.ConnectionStatus
import io.horizontalsystems.tor.R
import io.horizontalsystems.tor.Tor
import io.horizontalsystems.netkit.utils.NotificationUtils
import io.horizontalsystems.tor.EntityState
import io.reactivex.subjects.Subject

class NetNotifManager(private val context: Context) {

    private val TOR_CHANNEL_ID = "io.horizontalsystems.netkit.tor.channelId"
    private val TOR_SERVICE_NOTIFICATION_ID = 100
    private val iconConnecting = R.drawable.ic_tor
    private val iconConnected = R.drawable.ic_tor_running
    private val iconError = R.drawable.ic_tor_error

    init {
        NotificationUtils.createNotificationChannel(context, "io.horizontalsystems", TOR_CHANNEL_ID)
    }

    private fun updateNotification(torInfo: Tor.Info?, contentParam: String) {

        var content = contentParam
        var title: String
        var icon: Int

        torInfo?.let {

            if (it.state == EntityState.STARTING) {
                title = "Tor: Starting"
            } else if (it.state == EntityState.RUNNING) {
                title = "Tor: Running"
            } else {
                title = "Tor: Stopped"
            }

            if (it.connection.getState() == ConnectionStatus.CONNECTING) {
                icon = iconConnecting
                content = "Connecting ... "
            } else if (it.connection.getState() == ConnectionStatus.CONNECTED) {
                content = "Successfully Connected !"
                icon = iconConnected
            } else {
                icon = iconError
                content = "Disconnected"
            }

            val notification: NotificationCompat.Builder = NotificationCompat.Builder(context, TOR_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setOngoing(true)
                .setGroup("Tor")
                .setContentIntent(getPendingIntent())
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setGroupSummary(false)
                .setSmallIcon(icon)

            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            mNotificationManager.notify(TOR_SERVICE_NOTIFICATION_ID, notification.build())

        }
    }

    private fun getPendingIntent(): PendingIntent?{
        val launchIntent: Intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return PendingIntent.getActivity(context, 0, launchIntent, 0)
    }

    @SuppressLint("CheckResult")
    fun subscribeTo(torObserver: Subject<Tor.Info>){

        torObserver.subscribe(
            { torInfo -> updateNotification(torInfo, "")
            },
            { error -> updateNotification(null, error.toString())
            })
    }
}