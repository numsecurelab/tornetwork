package io.horizontalsystems.netkit

import io.horizontalsystems.netkit.network.ConnectionManager
import io.horizontalsystems.tor.Tor
import io.horizontalsystems.tor.TorManager
import io.horizontalsystems.tor.core.TorConstants
import io.reactivex.Observable
import io.reactivex.Single
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL

class NetKit(private val torListener: Tor.Listener) {


    private val connectionManager = ConnectionManager()

    fun startTor(torSettings: Tor.Settings): Observable<Tor.Info> {

        val torManager = TorManager(true, torSettings, torListener)

        return torManager.start().map {
            connectionManager.setSystemProxy(
                TorConstants.IP_LOCALHOST,
                TorConstants.HTTP_PROXY_PORT_DEFAULT,
                TorConstants.SOCKS_PROXY_PORT_DEFAULT
            )

            it
        }
    }

    fun stopVpn(): Boolean {
        return true
    }

    fun getSocketConnection(host: String, port: Int): Socket {
        return connectionManager.getSocketConnection(host, port)
    }

    fun getHttpConnection(url: URL): HttpURLConnection {

        return connectionManager.httpURLConnection(url, false)
    }

}