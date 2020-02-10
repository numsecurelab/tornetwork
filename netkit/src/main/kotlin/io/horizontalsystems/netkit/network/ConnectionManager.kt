package io.horizontalsystems.netkit.network

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.*

enum class ProxyEnvVar(val value: String) {

    USE_SYSTEM_PROXIES("java.net.useSystemProxies"),
    HTTP_PROXY_HOST("http.proxyHost"),
    HTTP_PROXY_PORT("http.proxyPOrt"),
    HTTPS_PROXY_HOST("https.proxyHost"),
    HTTPS_PROXY_PORT("https.proxyPort"),
    SOCKS_PROXY_HOST("socksProxyHost"),
    SOCKS_PROXY_PORT("socksProxyPort");
}

class ConnectionManager {


    private val READ_TIMEOUT_MILLISECONDS = 60000
    private val CONNECT_TIMEOUT_MILLISECONDS = 60000

    fun getSocketConnection(host: String, port: Int): Socket {
        return Socket(host, port)
    }


    @Throws(IOException::class)
    fun socks4aSocketConnection(
        networkHost: String, networkPort: Int, useProxy: Boolean, proxyHost: String = "",
        proxyPort: Int = 0
    ): Socket? {

        val socket = Socket()
        val socksAddress: SocketAddress = InetSocketAddress(proxyHost, proxyPort)
        socket.soTimeout = READ_TIMEOUT_MILLISECONDS
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
                        + ", socksHost=" + proxyHost + ",socksPort=" + proxyPort
            )
        }
        inputStream.readShort()
        inputStream.readInt()

        return socket
    }

    fun httpURLConnection(
        url: URL,
        useProxy: Boolean,
        proxyHost: String = "",
        proxyPort: Int = 0
    ): HttpURLConnection {

        if (useProxy) {
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyPort))
            return url.openConnection(proxy) as HttpURLConnection
        } else
            return url.openConnection() as HttpURLConnection
    }


    fun setSystemProxy(host: String, httpPort: String, socksPort: String) {

        System.setProperty(ProxyEnvVar.USE_SYSTEM_PROXIES.value, "true");
        System.setProperty(ProxyEnvVar.HTTP_PROXY_HOST.value, host)
        System.setProperty(ProxyEnvVar.HTTP_PROXY_PORT.value, httpPort)
        System.setProperty(ProxyEnvVar.HTTPS_PROXY_HOST.value, host)
        System.setProperty(ProxyEnvVar.HTTPS_PROXY_PORT.value, httpPort)
        System.setProperty(ProxyEnvVar.SOCKS_PROXY_HOST.value, host)
        System.setProperty(ProxyEnvVar.SOCKS_PROXY_PORT.value, socksPort)
    }
}