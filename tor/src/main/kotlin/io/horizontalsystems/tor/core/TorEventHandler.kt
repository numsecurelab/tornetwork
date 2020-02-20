package io.horizontalsystems.tor.core

import android.text.TextUtils
import io.horizontalsystems.tor.ConnectionStatus
import io.horizontalsystems.tor.Tor
import net.freehaven.tor.control.EventHandler
import java.util.logging.Logger

open class TorEventHandler : EventHandler {

    private var torControl: TorControl = TorControl.instance

    private val logger = Logger.getLogger("TorEventHandler")

    private fun eventMonitor(torInfo: Tor.Info? = null, msg: String? = null) {
        msg?.let {
            logger.info(msg)
        }

        torInfo?.let {
            torControl.torObservable?.let {
                if (it.hasObservers())
                    it.onNext(torInfo)
            }
        }
    }

    override fun streamStatus(status: String?, streamID: String?, target: String?) {
    }

    override fun bandwidthUsed(read: Long, written: Long) {
        //logger.info("BandwidthUsed:${read},${written}")
    }

    override fun orConnStatus(status: String?, orName: String?) {
        status?.let {

            if (TextUtils.equals(status, "CONNECTED")) {

                Thread(Runnable {
                    torControl.onBootstrapped(torControl.torInfo)
                }).start()

            } else if (TextUtils.equals(status, "FAILED")) {
                torControl.torInfo.connection.status = ConnectionStatus.FAILED
                eventMonitor(torControl.torInfo)
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
    }
}