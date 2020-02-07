package io.horizontalsystems.tor.vpn

interface VpnConstants {
    companion object {
        val PDNSD_BIN = "pdnsd"

        var FILE_WRITE_BUFFER_SIZE = 2048

        var SHELL_CMD_PS = "toolbox ps"

        const val LOCAL_ACTION_PORTS = "ports"
        const val ACTION_START = "start"
        const val ACTION_STOP = "stop"
    }
}