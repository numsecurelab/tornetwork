package io.horizontalsystems.tor.vpn

import android.app.Application
import android.content.Context
import io.horizontalsystems.tor.ConnectionStatus
import io.horizontalsystems.tor.core.TorConstants
import java.io.File

object Vpn {

    class Settings(var context: Context){

        var appFilesDir: File
        var appDataDir: File
        var appNativeDir: File
        var appSourceDir: File

        init {
            appFilesDir = context.filesDir
            appDataDir = context.getDir(TorConstants.DIRECTORY_TOR_DATA, Application.MODE_PRIVATE)
            appNativeDir = File(context.applicationInfo.nativeLibraryDir)
            appSourceDir = File(context.applicationInfo.sourceDir)
        }
    }

    class Info(var connectionInfo: ConnectionInfo){

    }

    class ConnectionInfo{
        var connectionStatus = ConnectionStatus.CLOSED

    }

}