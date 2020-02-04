package io.horizontalsystems.tor.core

import com.jrummyapps.android.shell.Shell
import io.horizontalsystems.tor.*
import io.reactivex.Single
import java.io.*
import java.util.logging.Level
import java.util.logging.Logger

class TorOperator(private val torSettings: Tor.Settings, private val torListener: Tor.Listener?) {

    private val logger = Logger.getLogger("TorOperator")

    lateinit var torControl: TorControl
    lateinit var resManager: TorResourceManager
    private val torInfo = Tor.Info(Tor.ConnectionInfo())


    fun start(): Single<Tor.Info> {

        try {

            resManager = TorResourceManager(torSettings)
            val fileTorBin = resManager.installResources()
            val success = fileTorBin != null && fileTorBin.canExecute()

            if (success) {

                torInfo.isInstalled = true
                eventMonitor(torInfo = torInfo, message = "Tor install success.")

                runTorShellCmd(resManager.fileTor, resManager.fileTorrcCustom)
                eventMonitor(torInfo = torInfo, message = "Tor started successfully")

                // Wait for control file creation -> Replace this implementation with RX.
                //-----------------------------
                Thread.sleep(100)
                //-----------------------------

                torControl = TorControl(resManager.fileTorControlPort, torSettings.appDataDir, torListener)

                return torControl.initConnection(4, torInfo)
                        .map { connInfo ->
                            Tor.Info(connInfo)
                        }
            }

        } catch (e: java.lang.Exception) {
            torInfo.processId = -1
            eventMonitor(torInfo = torInfo, message = "Error starting Tor")
            eventMonitor(message = e.message.toString())
        }

        return Single.just(Tor.Info(Tor.ConnectionInfo(-1)))
    }

    fun stop(): Single<Boolean> {
        return Single.just(true)
    }

    fun newIdentity(): Boolean {
        return torControl.newIdentity()
    }

    private fun eventMonitor(torInfo: Tor.Info? = null, logLevel: Level = Level.SEVERE, message: String) {
        torListener?.let {
            torListener.onProcessStatusUpdate(torInfo, message)
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

        return shellResult.exitCode
    }

}