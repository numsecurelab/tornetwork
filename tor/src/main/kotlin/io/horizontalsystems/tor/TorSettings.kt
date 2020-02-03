package io.horizontalsystems.tor

import android.app.Application
import android.content.Context
import io.horizontalsystems.tor.core.TorConstants
import java.io.File

open class TorSettings(val context: Context) {

    var appFilesDir: File
    var appDataDir: File
    var appNativeDir: File
    var appSourceDir: File

    open var useBridges: Boolean = false

    init {
        appFilesDir = context.filesDir
        appDataDir = context.getDir(TorConstants.DIRECTORY_TOR_DATA, Application.MODE_PRIVATE)
        appNativeDir = File(context.applicationInfo.nativeLibraryDir)
        appSourceDir = File(context.applicationInfo.sourceDir)
    }
}