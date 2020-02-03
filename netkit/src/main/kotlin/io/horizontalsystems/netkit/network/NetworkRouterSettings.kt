package io.horizontalsystems.netkit.network

import android.content.Context
import io.horizontalsystems.tor.TorSettings

class NetworkRouterSettings(context: Context) : TorSettings(context) {

    override var useBridges: Boolean
        get() = super.useBridges
        set(value) {}

}