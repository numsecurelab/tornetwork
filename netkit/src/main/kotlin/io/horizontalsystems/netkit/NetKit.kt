package io.horizontalsystems.netkit

import io.horizontalsystems.netkit.network.ConnectionManager
import io.horizontalsystems.tor.Tor
import io.horizontalsystems.tor.TorManager
import io.reactivex.Single
import java.net.HttpURLConnection
import java.net.URL

class NetKit(private var enableVPN: Boolean,
             private var enableTOR: Boolean,
             private val torSettings: Tor.Settings,
             private val torListener: Tor.Listener) {

    private val connectionManager = ConnectionManager()
    lateinit var torInfo: Tor.Info

    fun initNetworkRouter(): Single<Tor.Info> {

        if (enableTOR) {
            val torManager = TorManager(false, torSettings, torListener)

            return torManager.start().map {
                torInfo = it
                it
            }
        } else {
            torInfo = Tor.Info(Tor.ConnectionInfo(-1))
        }

        return Single.just(torInfo)
    }

    fun getHttpConnection(url: URL): HttpURLConnection {

        return connectionManager.httpURLConnection(url,
                                                   enableTOR,
                                                   torInfo.connectionInfo.proxyHost,
                                                   torInfo.connectionInfo.proxyHttpPort.toInt())
    }
}