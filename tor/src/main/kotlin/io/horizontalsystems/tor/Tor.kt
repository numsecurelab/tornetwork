package io.horizontalsystems.tor

import android.app.Application
import android.content.Context
import io.horizontalsystems.tor.core.TorConstants
import java.io.File

object Tor {

    enum class ConnectionStatus {

        UNDEFINED,
        EXTENDET,
        LAUNCHED,
        BUILT,
        FAILED,
        CONNECTED,
        CLOSED;

        companion object {

            fun getByName(typName: String): ConnectionStatus {
                return ConnectionStatus.values()
                        .find { it.name.contentEquals(typName.toUpperCase()) } ?: UNDEFINED
            }
        }
    }

    class Info(val connectionInfo: ConnectionInfo) {

        var processId: Int
            get() = connectionInfo.processId
            set(value) {
                connectionInfo.processId = value
            }

        var isInstalled: Boolean = false

        val isStarted: Boolean
            get() = connectionInfo.processId > 0
    }

    class ConnectionInfo(processIdArg: Int = -1) {

        var processId: Int = processIdArg
        var proxyHost = TorConstants.IP_LOCALHOST
        var proxySocksPort = TorConstants.SOCKS_PROXY_PORT_DEFAULT
        var proxyHttpPort = TorConstants.HTTP_PROXY_PORT_DEFAULT
        val isConnected: Boolean
            get() = connectionStatus == ConnectionStatus.CONNECTED

        var connectionStatus = ConnectionStatus.UNDEFINED
        var circuitStatus = ConnectionStatus.UNDEFINED
        var circuitId: String? = null
    }

    class Settings(val context: Context) {

        var appFilesDir: File
        var appDataDir: File
        var appNativeDir: File
        var appSourceDir: File

        var useBridges: Boolean = false

        init {
            appFilesDir = context.filesDir
            appDataDir = context.getDir(TorConstants.DIRECTORY_TOR_DATA, Application.MODE_PRIVATE)
            appNativeDir = File(context.applicationInfo.nativeLibraryDir)
            appSourceDir = File(context.applicationInfo.sourceDir)
        }
    }

    interface Listener {
        fun onProcessStatusUpdate(torInfo: Info?, message: String)
        fun onConnStatusUpdate(torConnInfo: ConnectionInfo?, message: String)
    }
}