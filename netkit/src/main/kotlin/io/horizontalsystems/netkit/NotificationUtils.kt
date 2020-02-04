package io.horizontalsystems.netkit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class NotificationUtils {

    companion object {

        private const val NOTIFICATION_ID = 0
        private const val channelId = "vpn_notification_channel"

        fun getNotification(
            context: Context,
            pendingIntent: PendingIntent,
            title: String,
            description: String
        ): Notification {
            createNotificationChannel(context, title)

            val mBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentTitle(title)
                .setContentText(description)

            mBuilder.setContentIntent(pendingIntent)

            return mBuilder.build()
        }

        private fun createNotificationChannel(ctx: Context, appName: String) {
            val androidNotificationManager = NotificationManagerCompat.from(ctx)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, appName, importance)
            androidNotificationManager.createNotificationChannel(channel)
        }
    }
}
