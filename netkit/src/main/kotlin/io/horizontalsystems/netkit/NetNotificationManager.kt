package io.horizontalsystems.netkit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.horizontalsystems.tor.ConnectionStatus
import io.horizontalsystems.tor.EntityState
import io.horizontalsystems.tor.R
import io.horizontalsystems.tor.Tor

class NetNotificationManager(private val context: Context) {

    private val TOR_CHANNEL_ID = "io.netkit.tor.channelId"
    private val TOR_SERVICE_NOTIFICATION_ID = 100
    private val iconConnecting = R.drawable.ic_tor
    private val iconConnected = R.drawable.ic_tor_running
    private val iconError = R.drawable.ic_tor_error

    init {
        createNotificationChannel(context, "Tor Connection Channel", TOR_CHANNEL_ID)
    }

    fun closeNotification() {
        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(TOR_SERVICE_NOTIFICATION_ID)
    }

    fun updateNotification(torInfo: Tor.Info?) {

        var content: String
        var title: String
        var icon: Int

        torInfo?.let {

            title = when (it.state) {
                EntityState.STARTING -> "Tor: Starting"
                EntityState.RUNNING -> "Tor: Running"
                else -> "Tor: Stopped"
            }

            icon = when (it.connection.status) {
                ConnectionStatus.CONNECTING -> iconConnecting
                ConnectionStatus.CONNECTED -> iconConnected
                else -> iconError
            }

            content = when (it.connection.status) {
                ConnectionStatus.CONNECTING -> "Connecting ... "
                ConnectionStatus.CONNECTED -> "Successfully Connected !"
                else -> "Disconnected"
            }

            val notification: NotificationCompat.Builder =
                NotificationCompat.Builder(context, TOR_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setOngoing(true)
                    .setContentIntent(getPendingIntent())
                    .setSmallIcon(icon)

            val mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            mNotificationManager.notify(TOR_SERVICE_NOTIFICATION_ID, notification.build())
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        val launchIntent: Intent =
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return PendingIntent.getActivity(context, 0, launchIntent, 0)
    }

    private fun createNotificationChannel(ctx: Context, appName: String, channelId: String) {

        val androidNotificationManager = NotificationManagerCompat.from(ctx)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(channelId, appName, importance)

        androidNotificationManager.createNotificationChannel(channel)
    }

}