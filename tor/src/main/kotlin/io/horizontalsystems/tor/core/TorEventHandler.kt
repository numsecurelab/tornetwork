package io.horizontalsystems.tor.core

import android.text.TextUtils
import io.horizontalsystems.tor.ConnectionStatus
import io.horizontalsystems.tor.Tor
import net.freehaven.tor.control.EventHandler
import java.util.logging.Logger

open class TorEventHandler(
        private val torInternalListener: Tor.Listener?,
        private val torMainListener: Tor.Listener?,
        private val connInfo: Tor.ConnectionInfo)
    : EventHandler {

    private val logger = Logger.getLogger("TorEventHandler")

    override fun streamStatus(status: String?, streamID: String?, target: String?) {
    }

    override fun bandwidthUsed(read: Long, written: Long) {
    }

    override fun orConnStatus(status: String?, orName: String?) {
    }

    override fun newDescriptors(orList: MutableList<String>?) {
    }

    override fun unrecognized(type: String?, msg: String?) {
    }

    override fun circuitStatus(status: String?, circID: String?, path: String?) {

        status?.let {

            if(TextUtils.equals(status, "BUILT")) {
                if (connInfo.circuitStatus == ConnectionStatus.CONNECTING)
                    connInfo.circuitStatus = ConnectionStatus.CONNECTED
            } else if(TextUtils.equals(status, "CLOSED")){
                connInfo.circuitStatus = ConnectionStatus.CLOSED
            }
            else{
                connInfo.circuitStatus = ConnectionStatus.CONNECTING
            }
        }

        torInternalListener?.onConnStatusUpdate(connInfo, "ConnectionStatus: ${status}")
        torMainListener?.onConnStatusUpdate(connInfo, "ConnectionStatus: ${status}")
    }

    override fun message(severity: String?, msg: String?) {
        val logMessage = "Message:${msg}"

        msg?.let {

            if (msg.contains("100")) {
                connInfo.isBootstrapped = true
                logger.info(logMessage)
                torInternalListener?.onConnStatusUpdate(connInfo, logMessage)
                torMainListener?.onConnStatusUpdate(connInfo, logMessage)
            }
        }
    }
}