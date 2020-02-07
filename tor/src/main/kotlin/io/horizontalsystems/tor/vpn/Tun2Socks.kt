package io.horizontalsystems.tor.vpn

import android.content.Context
import android.os.ParcelFileDescriptor
import org.torproject.android.service.vpn.Tun2Socks

class Tun2Socks {

    fun init() {}

    fun start(
        context: Context?,
        vpnInterfaceFileDescriptor: ParcelFileDescriptor?,
        vpnInterfaceMTU: Int,
        vpnIpAddress: String?,
        vpnNetMask: String?,
        socksServerAddress: String?,
        udpgwServerAddress: String?,
        udpgwTransparentDNS: Boolean) {

        vpnInterfaceFileDescriptor?.let {
            Tun2Socks.Start(
                context, vpnInterfaceFileDescriptor
                , vpnInterfaceMTU,
                vpnIpAddress, vpnNetMask, socksServerAddress, udpgwServerAddress,
                udpgwTransparentDNS
            )
        }
    }

    fun stop() {
        Tun2Socks.Stop()
    }
}

