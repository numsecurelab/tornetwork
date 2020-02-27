package io.horizontalsystems.tor

import android.content.Context
import io.horizontalsystems.tor.core.TorOperator
import io.reactivex.Single

class TorManager(context: Context, private val listener: Listener): TorOperator.Listener {

    interface Listener{
        fun statusUpdate(torInfo: Tor.Info)
    }

    private var torSettings: Tor.Settings = Tor.Settings(context)
    private lateinit var torOperator: TorOperator

    fun start(useBridges: Boolean) {
        torSettings.useBridges = useBridges
        torOperator = TorOperator(torSettings, this)
        torOperator.start()
    }

    fun stop(): Single<Boolean> {
        return torOperator.stop()
    }

    override fun statusUpdate(torInfo: Tor.Info) {
        listener.statusUpdate(torInfo)
    }

    fun newIdentity(): Boolean {
        return torOperator.newIdentity()
    }

    fun getTorInfo(): Tor.Info {
        return torOperator.torInfo
    }

}
