package io.horizontalsystems.tor

class TorStatus(val connectionInfo: TorConnectionInfo) {

    var processId: Int
        get() = connectionInfo.processId
        set(value) {
            connectionInfo.processId = value
        }

    val isStarted: Boolean
        get() = connectionInfo.isStarted

    val isConnected: Boolean
        get() = connectionInfo.isConnected

}