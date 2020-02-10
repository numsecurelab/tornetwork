package io.horizontalsystems.tor.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket

class NetworkUtils {

    companion object{

        fun isPortOpen(ip: String?, port: Int, timeout: Int): Boolean {
            return try {
                val socket = Socket()
                socket.connect(InetSocketAddress(ip, port), timeout)
                socket.close()
                true
            } catch (ce: ConnectException) { //ce.printStackTrace();
                false
            } catch (ex: Exception) { //ex.printStackTrace();
                false
            }
        }

        private fun createNotificationChannel(ctx: Context, appName: String, channelId: String) {

            val androidNotificationManager = NotificationManagerCompat.from(ctx)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, appName, importance)

            androidNotificationManager.createNotificationChannel(channel)
        }


    }
}