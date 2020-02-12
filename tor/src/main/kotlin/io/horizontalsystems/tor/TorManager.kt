package io.horizontalsystems.tor

import android.content.Context
import io.horizontalsystems.tor.core.TorOperator
import io.reactivex.Observable
import io.reactivex.Single

class TorManager(
        context: Context,
        private val torMainListener: Tor.Listener?) {

    private var torSettings: Tor.Settings = Tor.Settings(context)
    var torInfo: Tor.Info = Tor.Info(Tor.ConnectionInfo(-1))
    private lateinit var torOperator: TorOperator

    fun start(useBridges: Boolean): Observable<Tor.Info> {

        torSettings.useBridges = useBridges
        torOperator = TorOperator(torSettings, torInfo, torMainListener)

        return torOperator.start()
    }

    fun stop(): Single<Boolean> {
        return torOperator.stop()
    }

    fun newIdentity(): Boolean {
        return torOperator.newIdentity()
    }
}