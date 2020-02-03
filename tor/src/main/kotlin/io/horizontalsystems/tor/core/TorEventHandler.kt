package io.horizontalsystems.tor.core

import io.horizontalsystems.tor.Tor
import net.freehaven.tor.control.EventHandler
import java.util.logging.Logger

open class TorEventHandler(private val torListener: Tor.Listener?,
                           private val connInfo: Tor.ConnectionInfo)
    : EventHandler {

    private val logger = Logger.getLogger("TorEventHandler")

    override fun streamStatus(status: String?, streamID: String?, target: String?) {
        //val logMessage = "StreamStatus: Status:${status}, streamID:${streamID}, target:${target}\n"
        //logger.info(logMessage)
        //torListener?.onConnStatusUpdate(null, logMessage)
    }

    override fun bandwidthUsed(read: Long, written: Long) {
    }

    override fun orConnStatus(status: String?, orName: String?) {

        logger.info("ConnectionStatus: ${status}, orName:${orName}\n")
        status?.let {
            connInfo.connectionStatus = Tor.ConnectionStatus.getByName(status)
        }
        torListener?.onConnStatusUpdate(connInfo, "ConnectionStatus: ${status}")
    }

    override fun newDescriptors(orList: MutableList<String>?) {
        //val logMessage = "newDescriptors: orList:${orList.toString()}\n"
        //logger.info(logMessage)
        //torListener?.onConnStatusUpdate(null, logMessage)
    }

    override fun unrecognized(type: String?, msg: String?) {
        //val logMessage = "unrecognized: type:${type}, msg:${msg}\n"
        //logger.info(logMessage)
        //torListener?.onConnStatusUpdate(null, logMessage)
    }

    override fun circuitStatus(status: String?, circID: String?, path: String?) {

        logger.info("circuitStatus:${status}, circID:${circID}, path:${path}\n")
        status?.let {
            connInfo.circuitId = circID
            connInfo.circuitStatus = Tor.ConnectionStatus.getByName(status)
        }
        torListener?.onConnStatusUpdate(connInfo, "CircuitStatus:${status}")
    }

    override fun message(severity: String?, msg: String?) {
        val logMessage = "Message:${msg}"

        logger.info(logMessage)
        torListener?.onConnStatusUpdate(connInfo, logMessage)
    }
}