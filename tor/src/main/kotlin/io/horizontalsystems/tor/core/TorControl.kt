package io.horizontalsystems.tor.core

import io.horizontalsystems.tor.Tor
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import net.freehaven.tor.control.TorControlConnection
import java.io.*
import java.net.Socket
import java.util.logging.Logger


class TorControl(
        private val fileControlPort: File,
        private val appCacheHome: File,
        private val torListener: Tor.Listener?) {

    private val logger = Logger.getLogger("TorControl")

    private val CONTROL_SOCKET_TIMEOUT = 60000
    private var controlConn: TorControlConnection? = null
    private var torEventHandler: TorEventHandler? = null

    var torProcessId: Int = -1

    private fun eventMonitor(msg: String?) {
        msg?.let {
            logger.info(msg)
        }
    }

    fun initConnection(maxTries: Int, torInfo: Tor.Info): Single<Tor.ConnectionInfo> {

        return createControlConn(maxTries)
                .subscribeOn(Schedulers.io())
                .map {
                    configConnection(it, torInfo)
                }.onErrorReturn {
                    Tor.ConnectionInfo(-1)
                }
    }

    private fun createControlConn(maxTries: Int): Single<TorControlConnection> {

        return Single.create { emitter ->
            var attempt = 0

            while (controlConn == null && attempt++ < maxTries) {

                try {

                    val controlPort = getControlPort()

                    if (controlPort != -1) {

                        eventMonitor("Connecting to control port: $controlPort")

                        val torConnSocket = Socket(TorConstants.IP_LOCALHOST, controlPort)
                        torConnSocket.soTimeout = CONTROL_SOCKET_TIMEOUT

                        val conn = TorControlConnection(torConnSocket)
                        controlConn = conn

                        eventMonitor("SUCCESS connected to Tor control port.")
                        emitter.onSuccess(conn)
                    }
                } catch (e: Exception) {
                    controlConn = null
                    eventMonitor("Error connecting to Tor local control port: " + e.localizedMessage)
                    emitter.tryOnError(e)
                }
            }
        }
    }

    private fun configConnection(conn: TorControlConnection, torInfo: Tor.Info): Tor.ConnectionInfo {

        try {

            val fileCookie = File(appCacheHome, TorConstants.TOR_CONTROL_COOKIE)

            if (fileCookie.exists()) {
                val cookie = ByteArray(fileCookie.length().toInt())
                val fis = DataInputStream(FileInputStream(fileCookie))
                fis.read(cookie)
                fis.close()
                conn.authenticate(cookie)
                eventMonitor("SUCCESS - authenticated to control port.")
                val torProcId = conn.getInfo("process/pid")

                torProcessId = torProcId.toInt()
                torInfo.connectionInfo.processId = torProcessId

                TorEventHandler(torListener, torInfo.connectionInfo).let {
                    torEventHandler = it
                    addEventHandler(conn, it)
                }

                return torInfo.connectionInfo

            } else {
                eventMonitor("Tor authentication cookie does not exist yet")
            }
        } catch (e: Exception) {

            controlConn = null
            torProcessId = -1

            eventMonitor("Error configuring Tor connection: " + e.localizedMessage)
        }

        return Tor.ConnectionInfo(-1)
    }

    private fun getControlPort(): Int {
        var result = -1

        try {
            if (fileControlPort.exists()) {
                eventMonitor("Reading control port config file: " + fileControlPort.canonicalPath)
                val bufferedReader =
                        BufferedReader(FileReader(fileControlPort))
                val line = bufferedReader.readLine()
                if (line != null) {
                    val lineParts = line.split(":").toTypedArray()
                    result = lineParts[1].toInt()
                }
                bufferedReader.close()

            } else {
                eventMonitor(
                        "Control Port config file does not yet exist (waiting for tor): "
                                + fileControlPort.canonicalPath
                )
            }
        } catch (e: FileNotFoundException) {
            eventMonitor("unable to get control port; file not found")
        } catch (e: java.lang.Exception) {
            eventMonitor("unable to read control port config file")
        }

        return result
    }


    @Throws(java.lang.Exception::class)
    private fun addEventHandler(conn: TorControlConnection, torEventHandler: TorEventHandler) {
        eventMonitor("adding control port event handler")

        conn.let {
            it.setEventHandler(torEventHandler)
            it.setEvents(listOf("ORCONN", "CIRC", "NOTICE", "WARN", "ERR", "BW"))

            eventMonitor("SUCCESS added control port event handler")
        }
    }
}