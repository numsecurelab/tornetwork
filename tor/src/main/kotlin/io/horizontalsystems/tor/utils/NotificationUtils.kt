package io.horizontalsystems.tor.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.horizontalsystems.tor.R


class NotificationUtils {

    companion object {

        fun getNotification(context: Context, pendingIntent: PendingIntent, title: String, description: String, channelId: String): Notification {

            createNotificationChannel(context, title, channelId)

            val mBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_stat_tor)
                .setContentTitle(title)
                .setContentText(description)

            mBuilder.setContentIntent(pendingIntent)

            return mBuilder.build()
        }

        private fun createNotificationChannel(ctx: Context, appName: String, channelId: String) {

            val androidNotificationManager = NotificationManagerCompat.from(ctx)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, appName, importance)

            androidNotificationManager.createNotificationChannel(channel)
        }
    }
}
