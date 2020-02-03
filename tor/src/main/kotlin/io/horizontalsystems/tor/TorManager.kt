package io.horizontalsystems.tor

import io.horizontalsystems.tor.core.TorOperator
import io.reactivex.Single

class TorManager(torSettings: Tor.Settings, torListener: Tor.Listener) {

    private val torOperator = TorOperator(torSettings, torListener)

    fun start(): Single<Tor.Info> {
        return torOperator.start()
    }
}