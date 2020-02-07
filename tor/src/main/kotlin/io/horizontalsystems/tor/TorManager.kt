package io.horizontalsystems.tor

import io.horizontalsystems.tor.core.TorOperator
import io.horizontalsystems.tor.vpn.service.TorVpnService
import io.reactivex.Single

class TorManager(
        val startNotificationService: Boolean = false,
        val torSettings: Tor.Settings,
        val torListener: Tor.Listener?) {

    private val torOperator = TorOperator(torSettings, torListener)

    fun start(): Single<Tor.Info> {
        return torOperator.start()
    }

    fun stop(): Single<Boolean> {
        return torOperator.stop()
    }

    fun newIdentity(): Boolean {
        return torOperator.newIdentity()
    }

}