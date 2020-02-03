package io.horizontalsystems.tor.core

import com.jrummyapps.android.shell.Shell
import io.horizontalsystems.tor.TorConnectionInfo
import io.horizontalsystems.tor.TorEventHandler
import io.horizontalsystems.tor.TorSettings
import io.horizontalsystems.tor.TorStatus
import io.reactivex.Single
import java.io.*
import java.util.logging.Level
import java.util.logging.Logger

class TorOperator(private val torSettings: TorSettings) {

    private val logger = Logger.getLogger("TorOperator")

    lateinit var torControl: TorControl
    lateinit var resManager: TorResourceManager

    fun start(): Single<TorStatus> {

        try {

            resManager = TorResourceManager(torSettings)
            val fileTorBin = resManager.installResources()
            val success = fileTorBin != null && fileTorBin.canExecute()

            if (success) {

                eventMonitor(message = "Tor install success.")
                eventMonitor(message = "Starting Tor... ")

                runTorShellCmd(resManager.fileTor, resManager.fileTorrcCustom)

                torControl = TorControl(resManager.fileTorControlPort, torSettings.appDataDir, TorEventHandler())

                return torControl.initConnection(4)
                        .map { connInfo ->
                            TorStatus(connInfo)
                        }
            }

        } catch (e: java.lang.Exception) {
            eventMonitor(message = e.message.toString())
        }

        return Single.just(TorStatus(TorConnectionInfo(-1)))
    }

    private fun eventMonitor(logLevel: Level = Level.SEVERE, message: String) {
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
        eventMonitor(message = "\nResult\n:${shellResult}")

        return shellResult.exitCode
    }

}