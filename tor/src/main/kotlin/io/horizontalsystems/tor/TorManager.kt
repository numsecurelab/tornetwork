package io.horizontalsystems.tor

import android.content.Context
import io.horizontalsystems.tor.core.TorOperator
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class TorManager(
        context: Context,
        private val torListener: Tor.Listener?) {

    private var torSettings: Tor.Settings = Tor.Settings(context)
    private lateinit var torOperator: TorOperator
    val torObservable = PublishSubject.create<Tor.Info>()

    fun start(useBridges: Boolean): Subject<Tor.Info> {

        torSettings.useBridges = useBridges
        torOperator = TorOperator( torSettings, torListener, torObservable )

        return torOperator.start()
    }

    fun stop(): Single<Boolean> {
        return torOperator.stop()
    }

    fun newIdentity(): Boolean {
        return torOperator.newIdentity()
    }

    fun getTorInfo(): Tor.Info {
        return torOperator.torInfo
    }

}
