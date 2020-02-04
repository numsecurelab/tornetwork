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

    private var torVpnService: TorVpnService? = null
    private var mBound: Boolean = false

//    private val connection = object : ServiceConnection {
//
//        override fun onServiceConnected(className: ComponentName, service: IBinder) {
//            val binder = service as TorVpnService.LocalBinder
//            torVpnService = binder.getService()
//            mBound = true
//        }
//
//        override fun onServiceDisconnected(arg0: ComponentName) {
//            mBound = false
//        }
//    }

    fun initNetworkRouter(): Single<Tor.Info> {

        if (enableTOR) {
            val torManager = TorManager(torSettings, torListener)

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

    fun startVpn() {
//        Intent(routerSettings.context, TorVpnService::class.java).also { intent ->
//            routerSettings.context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
//        }

        TorVpnService.start(torSettings.context)
    }

    fun stopVpn() {
        TorVpnService.stop(torSettings.context)
    }
}