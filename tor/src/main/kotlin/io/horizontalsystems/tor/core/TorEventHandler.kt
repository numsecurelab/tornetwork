package io.horizontalsystems.tor.core

import android.text.TextUtils
import io.horizontalsystems.tor.ConnectionStatus
import net.freehaven.tor.control.EventHandler
import java.util.logging.Logger

open class TorEventHandler : EventHandler {

    private var torControl : TorControl = TorControl.instance

    private val logger = Logger.getLogger("TorEventHandler")

    override fun streamStatus(status: String?, streamID: String?, target: String?) {
    }

    override fun bandwidthUsed(read: Long, written: Long) {
        //logger.info("BandwidthUsed:${read},${written}")
    }

    override fun orConnStatus(status: String?, orName: String?) {
        status?.let {

            if(TextUtils.equals(status, "CONNECTED"))
            {
                torControl.onBootstrapped(torControl.torInfo)
            }
            else if(TextUtils.equals(status, "FAILED")) {
                //Thread(Runnable {
                    torControl.torInfo.connection.status = ConnectionStatus.FAILED
                    torControl.torObservable?.onNext(torControl.torInfo)
                    torControl.torListener?.onConnStatusUpdate(torControl.torInfo.connection, "ConnectionStatus: ${status}")
                //}).start()
            }
        }

        logger.info("Connection Status:${status}")
    }

    override fun newDescriptors(orList: MutableList<String>?) {
    }

    override fun unrecognized(type: String?, msg: String?) {
    }

    override fun circuitStatus(status: String?, circID: String?, path: String?) {
    }

    override fun message(severity: String?, msg: String?) {
        val logMessage = "Message:${msg}"
        logger.info(logMessage)
    }
}