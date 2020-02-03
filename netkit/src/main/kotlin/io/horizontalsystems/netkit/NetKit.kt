package io.horizontalsystems.netkit

import io.horizontalsystems.netkit.network.NetworkRouterSettings
import io.horizontalsystems.tor.TorConnectionInfo
import io.horizontalsystems.tor.TorManager
import io.horizontalsystems.tor.TorStatus
import io.reactivex.Single

class NetKit(private val enableTor: Boolean,
             private val routerSettings: NetworkRouterSettings ) {

    fun initNetworkRouter() : Single<TorStatus> {

        if(enableTor) {
            val torManager = TorManager(routerSettings)
            return torManager.start()
        }
        else{
            return Single.just(TorStatus(TorConnectionInfo(-1)))
        }
    }
}