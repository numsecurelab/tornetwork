package io.horizontalsystems.netkit.network

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress


class ConnectionManager {

    private val READ_TIMEOUT_MILLISECONDS = 60000
    private val CONNECT_TIMEOUT_MILLISECONDS = 60000

    @Throws(IOException::class)
    fun socks4aSocketConnection(networkHost: String, networkPort: Int, socksHost: String, socksPort: Int): Socket? {

        val socket = Socket()
        val socksAddress: SocketAddress = InetSocketAddress(socksHost, socksPort)
        socket.setSoTimeout(READ_TIMEOUT_MILLISECONDS)
        socket.connect(socksAddress, CONNECT_TIMEOUT_MILLISECONDS)

        val outputStream = DataOutputStream(socket.getOutputStream())

        outputStream.writeByte(0x04)
        outputStream.writeByte(0x01)
        outputStream.writeShort(networkPort)
        outputStream.writeInt(0x01)
        outputStream.writeByte(0x00)
        outputStream.write(networkHost.toByteArray())
        outputStream.writeByte(0x00)

        val inputStream = DataInputStream(socket.getInputStream())
        val firstByte: Byte = inputStream.readByte()
        val secondByte: Byte = inputStream.readByte()

        if (firstByte != 0x00.toByte() || secondByte != 0x5a.toByte()) {
            socket.close()
            throw IOException(
                "SOCKS4a connect failed, got " + firstByte + " - " + secondByte +
                        ", but expected 0x00 - 0x5a:, networkHost= " + networkHost + ", networkPort = " + networkPort
                        + ", socksHost=" + socksHost + ",socksPort=" + socksPort
            )
        }
        inputStream.readShort()
        inputStream.readInt()

        return socket
    }
}