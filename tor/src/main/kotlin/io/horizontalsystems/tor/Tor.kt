package io.horizontalsystems.tor

import android.app.Application
import android.content.Context
import io.horizontalsystems.tor.core.TorConstants
import java.io.File

enum class EntityState(val processId: Int) {
    STARTING(-1),
    RUNNING(1),
    STOPPED(0);

    companion object {

        fun getByProcessId(procId: Int): EntityState {
            return values()
                    .find { it.processId == procId } ?: RUNNING
        }
    }
}

enum class ConnectionStatus {

    UNDEFINED,
    CLOSED,
    CONNECTING,
    CONNECTED,
    FAILED;

    companion object {

        fun getByName(typName: String): ConnectionStatus {
            return values()
                    .find { it.name.contentEquals(typName.toUpperCase()) } ?: CLOSED
        }
    }
}

object Tor {


    class Info(val connectionInfo: ConnectionInfo) {

        var processId: Int
            get() = connectionInfo.processId
            set(value) {
                connectionInfo.processId = value
            }

        var isInstalled: Boolean = false

        var state: EntityState
            get() = EntityState.getByProcessId(processId)
            set(value) {
                processId = value.processId
            }

        val isStarted: Boolean
            get() = connectionInfo.processId > 0
    }

    class ConnectionInfo(processIdArg: Int = -1) {

        var processId: Int = processIdArg
        var proxyHost = TorConstants.IP_LOCALHOST
        var proxySocksPort = TorConstants.SOCKS_PROXY_PORT_DEFAULT
        var proxyHttpPort = TorConstants.HTTP_PROXY_PORT_DEFAULT
        var isBootstrapped: Boolean = false
        var circuitStatus = ConnectionStatus.CLOSED

        fun getConnectionStatus(): ConnectionStatus {

            if (circuitStatus == ConnectionStatus.CONNECTED && isBootstrapped)
                return ConnectionStatus.CONNECTED
            else {

                if (circuitStatus == ConnectionStatus.CONNECTING)
                    return ConnectionStatus.CONNECTING
                else
                    return ConnectionStatus.CLOSED
            }
        }


    }


    class Settings(var context: Context) {

        constructor(context: Context, vpnMode: Boolean, useBridges: Boolean) : this(context) {
            this.context = context
            this.vpnMode = vpnMode
            this.useBridges = useBridges
        }

        var appFilesDir: File
        var appDataDir: File
        var appNativeDir: File
        var appSourceDir: File

        var vpnMode: Boolean = false
        var useBridges: Boolean = false

        init {
            appFilesDir = context.filesDir
            appDataDir = context.getDir(TorConstants.DIRECTORY_TOR_DATA, Application.MODE_PRIVATE)
            appNativeDir = File(context.applicationInfo.nativeLibraryDir)
            appSourceDir = File(context.applicationInfo.sourceDir)
        }
    }

    interface Listener {
        fun onProcessStatusUpdate(torInfo: Tor.Info?, message: String)
        fun onConnStatusUpdate(torConnInfo: Tor.ConnectionInfo?, message: String)
    }
}