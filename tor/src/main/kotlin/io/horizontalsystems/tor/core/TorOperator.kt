package io.horizontalsystems.tor.core

import com.jrummyapps.android.shell.Shell
import io.horizontalsystems.tor.*
import io.reactivex.Observable
import io.reactivex.Single
import java.io.*
import java.util.logging.Level
import java.util.logging.Logger

class TorOperator(
        private val torSettings: Tor.Settings,
        private val torInfo: Tor.Info,
        private val torInternalListener: Tor.Listener?,
        private val torMainListener: Tor.Listener?) {

    private val logger = Logger.getLogger("TorOperator")

    lateinit var torControl: TorControl
    lateinit var resManager: TorResourceManager

    fun start(): Observable<Tor.Info> {

        try {

            resManager = TorResourceManager(torSettings)
            val fileTorBin = resManager.installResources()
            val success = fileTorBin != null && fileTorBin.canExecute()

            if (success) {

                torInfo.isInstalled = true
                eventMonitor(torInfo = torInfo, message = "Tor install success.")

                if (runTorShellCmd(resManager.fileTor, resManager.fileTorrcCustom)) {

                    eventMonitor(torInfo = torInfo, message = "Successfully verified config")

                    // Wait for control file creation -> Replace this implementation with RX.
                    //-----------------------------
                    Thread.sleep(100)
                    //-----------------------------

                    torControl = TorControl(
                            resManager.fileTorControlPort,
                            torSettings.appDataDir,
                            torInternalListener,
                            torMainListener)

                    eventMonitor(torInfo = torInfo, message = "Tor started successfully")

                    return torControl.initConnection(4, torInfo).map {
                        torInfo
                    }
                }
            }

        } catch (e: java.lang.Exception) {
            torInfo.processId = -1
            eventMonitor(torInfo = torInfo, message = "Error starting Tor")
            eventMonitor(message = e.message.toString())
        }

        return Observable.just(torInfo)
    }

    fun stop(): Observable<Boolean> {
        return Observable.just(true)
    }

    fun newIdentity(): Boolean {
        return torControl.newIdentity()
    }

    private fun eventMonitor(torInfo: Tor.Info? = null, logLevel: Level = Level.SEVERE, message: String) {

        torMainListener?.let {
            torMainListener.onProcessStatusUpdate(torInfo, message)
        }

        logger.log(logLevel, message)
    }

    @Throws(Exception::class)
    private fun runTorShellCmd(fileTor: File, fileTorrc: File): Boolean {
        val appCacheHome: File = torSettings.appDataDir

        if (!fileTorrc.exists()) {
            eventMonitor(message = "torrc not installed: " + fileTorrc.canonicalPath)
            return false
        }
        val torCmdString = (fileTor.canonicalPath
                + " DataDirectory " + appCacheHome.canonicalPath
                + " --defaults-torrc " + fileTorrc)
        var exitCode = -1
        exitCode = try {
            exec("$torCmdString --verify-config", true)
        } catch (e: Exception) {
            eventMonitor(message = "Tor configuration did not verify: " + e.message + e)
            return false
        }
        exitCode = try {
            exec(torCmdString, true)
        } catch (e: Exception) {
            eventMonitor(message = "Tor was unable to start: " + e.message + e)
            return false
        }
        if (exitCode != 0) {
            eventMonitor(message = "Tor did not start. Exit:$exitCode")
            return false
        }
        return true
    }

    @Throws(Exception::class)
    private fun exec(cmd: String, wait: Boolean = false): Int {
        val shellResult = Shell.run(cmd)
        //  debug("CMD: " + cmd + "; SUCCESS=" + shellResult.isSuccessful());

        if (!shellResult.isSuccessful) {
            throw Exception(
                    "Error: " + shellResult.exitCode + " ERR=" + shellResult.getStderr() + " OUT=" + shellResult.getStdout()
            )
        }

        eventMonitor(message = "Result:$shellResult")


        return shellResult.exitCode
    }

}