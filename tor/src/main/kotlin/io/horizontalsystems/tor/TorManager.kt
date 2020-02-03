package io.horizontalsystems.tor

import io.horizontalsystems.tor.core.TorOperator
import io.reactivex.Single
import java.util.logging.Logger

class TorManager(torSettings: TorSettings) {

    private val logger = Logger.getLogger("TorManager")
    private val torOperator = TorOperator(torSettings)

    fun start(): Single<TorStatus> {
        return torOperator.start()
    }
}