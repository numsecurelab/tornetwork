package io.horizontalsystems.tor

import android.content.Intent
import io.horizontalsystems.tor.core.TorOperator
import io.horizontalsystems.tor.service.TorService
import io.reactivex.Single

class TorManager(
        val startNotificationService: Boolean = false,
        val torSettings: Tor.Settings,
        val torListener: Tor.Listener?) {

    private val torOperator = TorOperator(torSettings, torListener)

    fun start(): Single<Tor.Info> {

        if (startNotificationService) {
            torSettings.context.startService(
                    Intent(torSettings.context, TorService::class.java).setAction(TorService.RENEW_IDENTITY))
        }

        return torOperator.start()
    }

    fun stop(): Single<Boolean> {

        if (startNotificationService) {
            torSettings.context.stopService(Intent(torSettings.context, TorService::class.java))
        }

        return torOperator.stop()
    }

    fun newIdentity(): Boolean {
        return torOperator.newIdentity()
    }

}