package io.horizontalsystems.netkit

import android.content.Context
import io.horizontalsystems.netkit.network.ConnectionManager
import io.horizontalsystems.tor.Tor
import io.horizontalsystems.tor.TorManager
import io.horizontalsystems.tor.core.TorConstants
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Retrofit
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL

class NetKit(private val context: Context, private val torListener: Tor.Listener) {

    private val torNotifManager = NetNotifManager(context)
    private val torManager = TorManager(context, torListener)

    companion object {
        lateinit var instance: NetKit
    }

    init {
        instance = this
        torNotifManager.subscribeTo(torManager.torObservable)
    }

    fun getInstance(): NetKit {
        return instance
    }

    fun startTor(useBridges: Boolean): Observable<Tor.Info> {

        enableProxy()
        return torManager.start(useBridges)
    }

    fun stopTor(): Single<Boolean> {
        disableProxy()

        return torManager.stop()
    }

    fun getSocketConnection(host: String, port: Int): Socket {
        return ConnectionManager.socks4aSocketConnection(
                host,
                port,
                torManager.getTorInfo().isStarted,
                TorConstants.IP_LOCALHOST,
                TorConstants.SOCKS_PROXY_PORT_DEFAULT.toInt())

    }

    fun getHttpConnection(url: URL): HttpURLConnection {

        return ConnectionManager.httpURLConnection(
                url,
                torManager.getTorInfo().isStarted,
                TorConstants.IP_LOCALHOST,
                TorConstants.HTTP_PROXY_PORT_DEFAULT.toInt())

    }

    fun buildRetrofit(url: String, timeout: Long = 60): Retrofit {
        return ConnectionManager.retrofit(
                url,
                timeout,
                torManager.getTorInfo().isStarted,
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