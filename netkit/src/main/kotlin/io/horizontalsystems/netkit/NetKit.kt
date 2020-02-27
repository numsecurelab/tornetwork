package io.horizontalsystems.netkit

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import io.horizontalsystems.netkit.network.ConnectionManager
import io.horizontalsystems.tor.Tor
import io.horizontalsystems.tor.TorManager
import io.horizontalsystems.tor.core.TorConstants
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import retrofit2.Retrofit
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL

class NetKit(private val context: Context) {

    private val torManager = TorManager(context)
    private var disposable: Disposable? = null
    private var netService: NetService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as NetService.LocalBinder
            netService = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            netService = null
        }
    }

    fun startTor(useBridges: Boolean): Observable<Tor.Info> {
        enableProxy()
        disposable = torManager.torObservable.subscribe({
            netService?.updateNotification(it)
        }, {
            Log.e("NetKit", "error", it)
        })
        startService(context)
        return torManager.start(useBridges)
    }

    fun stopTor(): Single<Boolean> {
        disableProxy()
        disposable?.dispose()
        torManager.stop()
        netService?.stop()
        return torManager.stop()
    }

    fun getSocketConnection(host: String, port: Int): Socket {
        return ConnectionManager.socks4aSocketConnection(
            host,
            port,
            false,//torManager.getTorInfo().isStarted,
            TorConstants.IP_LOCALHOST,
            TorConstants.SOCKS_PROXY_PORT_DEFAULT.toInt()
        )

    }

    fun getHttpConnection(url: URL): HttpURLConnection {

        return ConnectionManager.httpURLConnection(
            url,
            false,//torManager.getTorInfo().isStarted,
            TorConstants.IP_LOCALHOST,
            TorConstants.HTTP_PROXY_PORT_DEFAULT.toInt()
        )

    }

    fun buildRetrofit(url: String, timeout: Long = 60): Retrofit {
        return ConnectionManager.retrofit(
            url,
            timeout,
            false,//torManager.getTorInfo().isStarted,
            TorConstants.IP_LOCALHOST,
            TorConstants.SOCKS_PROXY_PORT_DEFAULT.toInt()
        )
    }

    fun enableProxy() {
        ConnectionManager.setSystemProxy(
            true,
            TorConstants.IP_LOCALHOST,
            TorConstants.HTTP_PROXY_PORT_DEFAULT,
            TorConstants.SOCKS_PROXY_PORT_DEFAULT
        )
    }

    fun disableProxy() {
        ConnectionManager.disableSystemProxy()
    }

    private fun startService(context: Context) {
        val serviceIntent = Intent(context, NetService::class.java)
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android")
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        ContextCompat.startForegroundService(context, serviceIntent)
    }

}
