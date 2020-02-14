package io.horizontalsystems.tor.core

import android.text.TextUtils
import io.horizontalsystems.tor.ConnectionStatus
import io.horizontalsystems.tor.Tor
import io.reactivex.subjects.Subject
import net.freehaven.tor.control.EventHandler
import java.util.logging.Logger

open class TorEventHandler(
        private val torListener: Tor.Listener?,
        private val torInfo: Tor.Info,
        private val torObservable: Subject<Tor.Info>?)
    : EventHandler {

    private val logger = Logger.getLogger("TorEventHandler")

    override fun streamStatus(status: String?, streamID: String?, target: String?) {
    }

    override fun bandwidthUsed(read: Long, written: Long) {
    }

    override fun orConnStatus(status: String?, orName: String?) {
        status?.let {

            if(TextUtils.equals(status, "CONNECTED"))
                torInfo.connection.status = ConnectionStatus.CONNECTED
            else if(TextUtils.equals(status, "BUILT")
                //|| TextUtils.equals(status, "LAUNCHED")
                || TextUtils.equals(status, "EXTENDED")
                 ) {
                torInfo.connection.status = ConnectionStatus.CONNECTING

            } else if(TextUtils.equals(status, "CLOSED")|| TextUtils.equals(status, "FAILED")){
                torInfo.connection.status = ConnectionStatus.CLOSED
            }
        }

        logger.info("Connection Status:${status}")
        torObservable?.onNext(torInfo)
        torListener?.onConnStatusUpdate(torInfo.connection, "ConnectionStatus: ${status}")
    }

    override fun newDescriptors(orList: MutableList<String>?) {
    }

    override fun unrecognized(type: String?, msg: String?) {
    }

    override fun circuitStatus(status: String?, circID: String?, path: String?) {
    }

    override fun message(severity: String?, msg: String?) {
        val logMessage = "Message:${msg}"

        msg?.let {

            if (msg.contains("100") || msg.contains("Done")) {
                torInfo.connection.isBootstrapped = true
                logger.info(logMessage)
                torObservable?.onNext(torInfo)
                torListener?.onConnStatusUpdate(torInfo.connection, logMessage)
            }
        }
    }
}