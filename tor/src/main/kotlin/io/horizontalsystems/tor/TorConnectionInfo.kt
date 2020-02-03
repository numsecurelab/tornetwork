package io.horizontalsystems.tor

import io.horizontalsystems.tor.core.TorConstants

class TorConnectionInfo(processIdArg: Int = -1) {

    var processId: Int = processIdArg
    var proxyHost = TorConstants.IP_LOCALHOST
    var proxySocksPort = TorConstants.SOCKS_PROXY_PORT_DEFAULT
    var proxyHttpPort = TorConstants.HTTP_PROXY_PORT_DEFAULT
    var isConnected: Boolean = false

    val isStarted: Boolean
        get() = processId > 0

}