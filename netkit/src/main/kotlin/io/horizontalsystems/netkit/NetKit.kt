package io.horizontalsystems.netkit

import android.content.Context
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

class NetKit(context: Context) {

    private val torNotificationManager = NetNotificationManager(context)
    private val torManager = TorManager(context)
    private var disposable: Disposable? = null


    fun startTor(useBridges: Boolean): Observable<Tor.Info> {
        enableProxy()
        disposable = torManager.torObservable.subscribe({
            torNotificationManager.updateNotification(it)
        },{

        })
        return torManager.start(useBridges)
    }

    fun stopTor(): Single<Boolean> {
        disableProxy()
        disposable?.dispose()
        torNotificationManager.closeNotification()
        return torManager.stop()
    }

    fun getSocketConnection(host: String, port: Int): Socket {
        return ConnectionManager.socks4aSocketConnection(
                host,
                port,
                false,//torManager.getTorInfo().isStarted,
                TorConstants.IP_LOCALHOST,
                TorConstants.SOCKS_PROXY_PORT_DEFAULT.toInt())

    }

    fun getHttpConnection(url: URL): HttpURLConnection {

        return ConnectionManager.httpURLConnection(
                url,
                false,//torManager.getTorInfo().isStarted,
                TorConstants.IP_LOCALHOST,
                TorConstants.HTTP_PROXY_PORT_DEFAULT.toInt())

    }

    fun buildRetrofit(url: String, timeout: Long = 60): Retrofit {
        return ConnectionManager.retrofit(
                url,
                timeout,
                false,//torManager.getTorInfo().isStarted,
                TorConstants.IP_LOCALHOST,
                TorConstants.SOCKS_PROXY_PORT_DEFAULT.toInt())
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
}