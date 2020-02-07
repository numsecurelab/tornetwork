package io.horizontalsystems.tor.vpn

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Message
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import com.runjva.sourceforge.jsocks.protocol.ProxyServer
import io.horizontalsystems.tor.R
import io.horizontalsystems.tor.Tor
import io.horizontalsystems.tor.core.TorConstants
import io.horizontalsystems.tor.utils.NativeLoader
import io.horizontalsystems.tor.utils.ProcessUtils
import java.io.*
import java.util.concurrent.TimeoutException


class VpnManager(val service: VpnService, val torSettings: Vpn.Settings) {

    private val TAG = "HS-VPN"
    private var mThreadVPN: Thread? = null
    private val mConfigureIntent: PendingIntent? = null

    private val mSessionName = "HSVPN"
    private var mInterface: ParcelFileDescriptor? = null

    private var mTorSocks = TorConstants.getIntValue(TorConstants.SOCKS_PROXY_PORT_DEFAULT)
    private var mTorDns = TorConstants.getIntValue(TorConstants.TOR_DNS_PORT_DEFAULT)

    var sSocksProxyServerPort = -1
    var sSocksProxyLocalhost: String? = null
    private var mSocksProxyServer: ProxyServer? = null
    var tun2Socks = Tun2Socks()

    private val VPN_MTU = 1500

    private var filePdnsd: File? = null

    private val PDNSD_BIN = "pdnsd"

    private var isRestart = false

    private var mService: VpnService? = null

    private var mLastBuilder: VpnService.Builder? = null

    init {

        mService = service
        filePdnsd = NativeLoader.loadNativeBinary(torSettings.appNativeDir,torSettings.appSourceDir, PDNSD_BIN, null)

        try {
            ProcessUtils.killProcess(filePdnsd!!, "-1")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        tun2Socks.init()
    }

    var isStarted = false

    //public int onStartCommand(Intent intent, int flags, int startId) {
    fun handleIntent(builder: VpnService.Builder, intent: Intent?): Int {
        if (intent != null) {
            val action = intent.action
            if (action == VpnConstants.ACTION_START) {
                isStarted = true

                // Stop the previous session by interrupting the thread.
                if (mThreadVPN != null && mThreadVPN!!.isAlive())
                    stopVPN()

                mLastBuilder = builder

                if (mTorSocks != -1) {
                    setupTun2Socks(builder)
                }
            } else if (action == VpnConstants.ACTION_STOP) {
                isStarted = false
                Log.d(TAG, "stop OrbotVPNService service!")
                stopVPN()
            } else if (action == VpnConstants.LOCAL_ACTION_PORTS) {
                Log.d(TAG, "starting OrbotVPNService service!")
                val torSocks = TorConstants.getIntValue(TorConstants.SOCKS_PROXY_PORT_DEFAULT)
                val torDns = TorConstants.getIntValue(TorConstants.HTTP_PROXY_PORT_DEFAULT)

                //if running, we need to restart
                if (torSocks != mTorSocks || torDns != mTorDns) {
                    mTorSocks = torSocks
                    mTorDns = torDns
                    setupTun2Socks(builder)
                }
            }
        }
        return Service.START_STICKY
    }


    @Synchronized
    private fun stopSocksBypass() {
        if (mSocksProxyServer != null) {
            mSocksProxyServer!!.stop()
            mSocksProxyServer = null
        }
    }

    private fun stopVPN() {
        if (mInterface != null) {
            try {
                Log.d("", "closing interface, destroying VPN interface")
                mInterface!!.close()
                mInterface = null

            } catch (e: java.lang.Exception) {
                Log.d(TAG, "error stopping tun2socks", e)
            } catch (e: Error) {
                Log.d(TAG, "error stopping tun2socks", e)
            }
        }
        stopDns()
        tun2Socks.stop()
        mThreadVPN = null
    }

    fun handleMessage(message: Message?): Boolean {
        if (message != null) {
            Toast.makeText(mService, message.what, Toast.LENGTH_SHORT).show()
        }
        return true
    }


    @Synchronized
    private fun setupTun2Socks(builder: VpnService.Builder) {
        if (mInterface != null) //stop tun2socks now to give it time to clean up
        {
            isRestart = true
            tun2Socks.stop()
            stopDns()
        }
        mThreadVPN = object : Thread() {
            override fun run() {
                try {
                    if (isRestart) {
                        Log.d(TAG, "is a restart... let's wait for a few seconds")
                        sleep(3000)
                    }
                    val vpnName = "HS-TorVPN"
                    val localhost = "127.0.0.1"
                    val virtualGateway = "192.162.200.1"
                    val virtualIP = "192.162.200.2"
                    val virtualNetMask = "255.255.255.0"
                    val dummyDNS = "1.1.1.1" //this is intercepted by the tun2socks library, but we must put in a valid DNS to start
                    val defaultRoute = "0.0.0.0"
                    val localSocks = "$localhost:$mTorSocks"
                    builder.setMtu(VPN_MTU)
                    builder.addAddress(virtualGateway, 32)
                    builder.setSession(vpnName)
                    //route all traffic through VPN (we might offer country specific exclude lists in the future)
                    builder.addRoute(defaultRoute, 0)
                    builder.addDnsServer(dummyDNS)
                    builder.addRoute(dummyDNS, 32)
                    //handle ipv6
                    //builder.addAddress("fdfe:dcba:9876::1", 126);
                    //builder.addRoute("::", 0);

                    // Create a new interface using the builder and save the parameters.
                    val packageName = torSettings.context.packageName
                    Log.e(TAG, "packageName: $packageName")
                    builder.addAllowedApplication(packageName)

                    val newInterface = builder
                        //.setSession(mSessionName)
                            .setConfigureIntent(mConfigureIntent)
                        .establish()
                    if (mInterface != null) {
                        Log.d(TAG, "Stopping existing VPN interface")
                        mInterface!!.close()
                        mInterface = null
                    }
                    mInterface = newInterface
                    isRestart = false
                    //start PDNSD daemon pointing to actual DNS
                    if (filePdnsd != null) {
                        val pdnsdPort = 8091
                        startDNS(filePdnsd!!.canonicalPath, localhost, mTorDns, virtualGateway, pdnsdPort)
                        val localDnsTransparentProxy = true
                        tun2Socks.start(mService, mInterface, VPN_MTU, virtualIP, virtualNetMask, localSocks,
                                        "$virtualGateway:$pdnsdPort", localDnsTransparentProxy)
                    }
                } catch (e: java.lang.Exception) {
                    Log.d(TAG, "tun2Socks has stopped", e)
                }
            }
        }
        mThreadVPN!!.start()
    }


    @Throws(IOException::class, TimeoutException::class)
    private fun startDNS(pdnsPath: String, torDnsHost: String, torDnsPort: Int,
                         pdnsdHost: String, pdnsdPort: Int) {
        val fileConf =
                makePdnsdConf(mService!!, mService!!.getFilesDir(), torDnsHost, torDnsPort, pdnsdHost, pdnsdPort)
        val cmdString =
                arrayOf(pdnsPath, "-c", fileConf.toString(), "-g", "-v2")
        val pb = ProcessBuilder(*cmdString)
        pb.redirectErrorStream(true)
        val proc = pb.start()
        try {
            proc.waitFor()
        } catch (e: Exception) {
            print(e)
        }
        Log.i(TAG, "PDNSD: " + proc.exitValue())
        if (proc.exitValue() != 0) {
            val br = BufferedReader(InputStreamReader(proc.inputStream))
            var line: String? = null
            while (br.readLine().also { line = it } != null) {
                Log.d(TAG, "pdnsd: $line")
            }
        }
    }

    private fun stopDns(): Boolean { // if that fails, try again using native utils
        try {
            ProcessUtils.killProcess(filePdnsd!!, "-1") // this is -HUP
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val filePid = File(mService!!.getFilesDir(), "pdnsd.pid")
        var pid: String? = null
        if (filePid.exists()) {
            try {
                val reader = BufferedReader(FileReader(filePid))
                val line = reader.readLine()
                if (line != null) {
                    pid = line.trim { it <= ' ' }
                    ProcessUtils.killProcess(pid, "-9")
                    filePid.delete()
                    return true
                }
            } catch (e: Exception) {
                Log.e(TAG, "error killing DNS Process: $pid", e)
            }
        }
        return false
    }

    @Throws(FileNotFoundException::class, IOException::class)
    fun makePdnsdConf(context: Context, fileDir: File, torDnsHost: String?,
                      torDnsPort: Int, pdnsdHost: String?, pdnsdPort: Int): File {

        val conf = String.format(context.getString(R.string.pdnsd_conf), torDnsHost, torDnsPort,
                                 fileDir.canonicalPath, pdnsdHost, pdnsdPort)
        Log.d(TAG, "pdsnd conf:$conf")
        val f = File(fileDir, "pdnsd.conf")
        if (f.exists()) {
            f.delete()
        }
        val fos = FileOutputStream(f, false)
        val ps = PrintStream(fos)
        ps.print(conf)
        ps.close()
        val cache = File(fileDir, "pdnsd.cache")
        if (!cache.exists()) {
            try {
                cache.createNewFile()
            } catch (e: Exception) {
            }
        }
        return f
    }

}