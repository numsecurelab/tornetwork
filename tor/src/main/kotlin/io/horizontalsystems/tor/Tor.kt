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
    IDLE,
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

    class Info(var connection: Connection) {

        var processId: Int
            get() = connection.processId
            set(value) {
                connection.processId = value
            }

        var isInstalled: Boolean = false

        var state: EntityState
            get() = EntityState.getByProcessId(processId)
            set(value) {
                processId = value.processId

                if (value == EntityState.STOPPED)
                    connection.status = ConnectionStatus.CLOSED
            }

        val isStarted: Boolean
            get() = connection.processId > 0
    }

    class Connection(processIdArg: Int = -1) {

        var processId: Int = processIdArg
        var proxyHost = TorConstants.IP_LOCALHOST
        var proxySocksPort = TorConstants.SOCKS_PROXY_PORT_DEFAULT
        var proxyHttpPort = TorConstants.HTTP_PROXY_PORT_DEFAULT
        var isBootstrapped: Boolean = false
        var status = ConnectionStatus.CLOSED

        fun getState(): ConnectionStatus {

            return if (status == ConnectionStatus.CONNECTED ){

                if(isBootstrapped)
                    ConnectionStatus.CONNECTED
                else
                    ConnectionStatus.CONNECTING
            }
            else {

                if (status == ConnectionStatus.CONNECTING)
                    ConnectionStatus.CONNECTING
                else
                    ConnectionStatus.CLOSED
            }
        }
    }

    class Settings(var context: Context) {
        var appFilesDir: File = context.filesDir
        var appDataDir: File = context.getDir(TorConstants.DIRECTORY_TOR_DATA, Application.MODE_PRIVATE)
        var appNativeDir: File = File(context.applicationInfo.nativeLibraryDir)
        var appSourceDir: File = File(context.applicationInfo.sourceDir)
        var useBridges: Boolean = false
    }

    interface Listener {
        fun onProcessStatusUpdate(torInfo: Info?, message: String)
        fun onConnStatusUpdate(torConnInfo: Connection?, message: String)
    }
}
