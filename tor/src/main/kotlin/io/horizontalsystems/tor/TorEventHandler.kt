package io.horizontalsystems.tor

import net.freehaven.tor.control.EventHandler
import java.util.logging.Logger

open class TorEventHandler() : EventHandler {

    var torConnectionInfo: TorConnectionInfo? = null

    private val logger = Logger.getLogger("TorEventHandler")

    override fun streamStatus(status: String?, streamID: String?, target: String?) {
        logger.info("StreamStatus: Status:${status}, streamID:${streamID}, target:${target}\n")
    }

    override fun bandwidthUsed(read: Long, written: Long) {
    }

    override fun orConnStatus(status: String?, orName: String?) {
        logger.info("ConnStatus: status:${status}, orName:${orName}\n")
    }

    override fun newDescriptors(orList: MutableList<String>?) {
        logger.info("newDescriptors: orList:${orList.toString()}\n")
    }

    override fun unrecognized(type: String?, msg: String?) {
        logger.info("unrecognized: type:${type}, msg:${msg}\n")
    }

    override fun circuitStatus(status: String?, circID: String?, path: String?) {
        logger.info("circuitStatus: Status:${status}, circID:${circID}, path:${path}\n")
    }

    override fun message(severity: String?, msg: String?) {
        logger.info("Message:${msg}, severity:${severity}")
    }
}