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
    private val torManager = TorManager(context, torNotifManager, torListener)

    fun startTor(useBridges: Boolean): Observable<Tor.Info> {

        return torManager.start(useBridges).let {
            enableProxy()
            it
        }
    }

    fun stopTor(): Single<Boolean> {
        return torManager.stop().let {
            disableProxy()
            it
        }
    }

    fun getSocketConnection(host: String, port: Int): Socket {
        return ConnectionManager.getSocketConnection(host, port)
    }

    fun getHttpConnection(url: URL): HttpURLConnection {

        if (torManager.torInfo.isStarted) {
            return ConnectionManager.httpURLConnection(
                    url,
                    true,
                    TorConstants.IP_LOCALHOST,
                    TorConstants.HTTP_PROXY_PORT_DEFAULT.toInt())

        } else
            return ConnectionManager.httpURLConnection(url, false)
    }

    fun buildRetrofit(url: String, timeout: Long = 60): Retrofit {
        return ConnectionManager.retrofit(
                url,
                timeout, torManager.torInfo.isStarted,
                TorConstants.IP_LOCALHOST,
                TorConstants.SOCKS_PROXY_PORT_DEFAULT.toInt())
    }


    fun enableProxy() {
        ConnectionManager.setSystemProxy(
                true,
                "127.1.1.1",
                TorConstants.HTTP_PROXY_PORT_DEFAULT,
                TorConstants.SOCKS_PROXY_PORT_DEFAULT
        )
    }

    fun disableProxy() {
        ConnectionManager.disableSystemProxy()
    }
}