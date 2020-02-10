package io.horizontalsystems.tor

import io.horizontalsystems.tor.core.TorOperator
import io.horizontalsystems.tor.service.TorService
import io.reactivex.Observable

class TorManager(
        val startNotificationService: Boolean = false,
        val torSettings: Tor.Settings,
        val torListener: Tor.Listener?) {

    init {
        tmInstance = this
    }

    var torInternalListener: Tor.Listener? = null
    var torInfo: Tor.Info = Tor.Info(Tor.ConnectionInfo(-1))
    private lateinit var torOperator: TorOperator


    companion object{

        lateinit var tmInstance: TorManager

        fun getInstance(): TorManager {
            return tmInstance
        }
    }

    fun start(): Observable<Tor.Info> {

        if(startNotificationService) {
            TorService.start(torSettings)
        }

        torOperator = TorOperator(torSettings, torInfo, torListener, torInternalListener)

        return torOperator.start()
    }

    fun stop(): Observable<Boolean> {
        return torOperator.stop()
    }

    fun newIdentity(): Boolean {
        return torOperator.newIdentity()
    }

    fun startTorService(){
        TorService.start(torSettings)
    }

    fun stopTorService(){
        TorService.stop(torSettings)
    }
}