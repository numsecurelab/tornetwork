package io.horizontalsystems.tor.core

import com.jaredrummler.android.shell.Shell
import io.horizontalsystems.tor.EntityState
import io.horizontalsystems.tor.Tor
import io.horizontalsystems.tor.utils.ProcessUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.Subject
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

class TorOperator(
        private val torSettings: Tor.Settings,
        private val torListener: Tor.Listener?,
        private val torObservable: Subject<Tor.Info>?) {

    private val logger = Logger.getLogger("TorOperator")
    val torInfo = Tor.Info(Tor.Connection())

    private var torControl: TorControl? = null
    private lateinit var resManager: TorResourceManager

    fun start(): Subject<Tor.Info> {

        try {

            resManager = TorResourceManager(torSettings)
            val fileTorBin = resManager.installResources()
            val success = fileTorBin != null && fileTorBin.canExecute()

            if (success) {

                torInfo.isInstalled = true
                torObservable?.onNext(torInfo)

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
                            torListener,
                            torObservable,
                            torInfo)

                    torInfo.state = EntityState.RUNNING
                    torObservable?.onNext(torInfo)
                    eventMonitor(torInfo = torInfo, message = "Tor started successfully")

                    torControl?.let {
                        it.initConnection(4)
                                .subscribe(
                                        { torConnection ->
                                            torInfo.connection = torConnection
                                            torObservable?.onNext(torInfo)
                                        },
                                        {
                                            torInfo.processId = -1
                                            torObservable?.onNext(torInfo)
                                        })

                    }
                }
            }

        } catch (e: java.lang.Exception) {
            torInfo.processId = -1
            torObservable?.onNext(torInfo)

            eventMonitor(torInfo = torInfo, message = "Error starting Tor")
            eventMonitor(message = e.message.toString())
        }

        return torObservable?.let { it } ?: Subject.just(torInfo) as Subject<Tor.Info>
    }

    fun stop(): Single<Boolean> {
        return killAllDaemons().subscribeOn(Schedulers.io())
    }

    fun newIdentity(): Boolean {
        return torControl?.newIdentity() ?: false
    }

    private fun eventMonitor(
            torInfo: Tor.Info? = null,
            logLevel: Level = Level.SEVERE,
            message: String) {

        torListener?.let {
            torListener.onProcessStatusUpdate(torInfo, message)
        }

        logger.log(logLevel, message)
    }

    @Throws(java.lang.Exception::class)
    private fun killAllDaemons(): Single<Boolean> {

        return Single.create { emitter ->

            try {
                var result = torControl?.shutdownTor() ?: false

                if (!result) {
                    result = killTorProcess()
                }

                torInfo.state = EntityState.STOPPED
                torObservable?.onNext(torInfo)

                eventMonitor(torInfo, Level.INFO, "Tor stopped")
                emitter.onSuccess(result)

            } catch (e: java.lang.Exception) {
                emitter.onError(e)
            }
        }
    }

    private fun killTorProcess(): Boolean {
        try {
            ProcessUtils.killProcess(resManager.fileTor, "-9") // this is -HUP
            return true
        } catch (e: Exception) {
            return false
        }
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

        var exitCode: Int

        exitCode = try {
            exec("$torCmdString --verify-config", true)
        } catch (e: Exception) {
            eventMonitor(message = "Tor configuration did not verify: " + e.message + e)
            return false
        }

        if (exitCode != 0) {
            eventMonitor(message = "Tor configuration did not verify:$exitCode")
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