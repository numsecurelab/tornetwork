package io.horizontalsystems.netkit

import io.horizontalsystems.netkit.network.ConnectionManager
import io.horizontalsystems.tor.Tor
import io.horizontalsystems.tor.TorManager
import io.horizontalsystems.tor.vpn.Vpn
import io.horizontalsystems.tor.vpn.service.TorVpnService
import io.reactivex.Single
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL

class NetKit(private val torListener: Tor.Listener) {

    private val connectionManager = ConnectionManager()
    lateinit var torInfo: Tor.Info

    fun startTor(torSettings: Tor.Settings): Single<Tor.Info> {

        val torManager = TorManager(false, torSettings, torListener)

        return torManager.start()
    }

    fun startVpn(vpnSettings: Vpn.Settings): Vpn.Info {

        TorVpnService.start(vpnSettings)

        return Vpn.Info(Vpn.ConnectionInfo())
    }

    fun stopVpn(): Boolean {
        return true
    }

    fun getSocketConnection(host: String, port: Int): Socket {
        return connectionManager.getSocketConnection(host,port)
    }

    fun getHttpConnection(url: URL): HttpURLConnection {

//        return connectionManager.httpURLConnection(url,
//                                                   enableTOR.let { if(!it) false else !enableVPN },
//                                                   torInfo.connectionInfo.proxyHost,
//                                                   torInfo.connectionInfo.proxyHttpPort.toInt())

        return connectionManager.httpURLConnection(url, false)

    }
}