package io.horizontalsystems.netkit.demo

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.VpnService
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import io.horizontalsystems.netkit.NetKit
import io.horizontalsystems.tor.Tor
import io.horizontalsystems.tor.vpn.Vpn
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStreamReader
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.URL


class MainActivity : AppCompatActivity(), Tor.Listener {

    private val REQUEST_VPN = 1
    private val listItems = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private val disposables = CompositeDisposable()
    private var vpnStarted: Boolean = false

    val netKit = NetKit(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btnTor.setOnClickListener {
            startTORClient()
        }

        btnVpn.setOnClickListener {

            if(vpnStarted){
                vpnStarted = false
                btnVpn.text = "Start Vpn"
                btnVpn.setBackgroundColor(Color.GRAY)
            }
            else{
                startVPN()
                netKit.startVpn(Vpn.Settings(context = applicationContext))
                vpnStarted = true
                btnVpn.text = "Stop Vpn"
                btnVpn.setBackgroundColor(Color.CYAN)
            }
        }


        btnTorTest.setOnClickListener {
            testTORConnection()
        }


        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)

        statusView.adapter = adapter
    }

    private fun startVPN(){

        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, 1)
        } else {
            onActivityResult(REQUEST_VPN, AppCompatActivity.RESULT_OK, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_VPN) {

        }
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    private fun logEvent(logMessage: String? = "") {

        logMessage?.let {
            listItems.add(logMessage)
            adapter.notifyDataSetChanged()
        }
    }

    private fun startTORClient() {

        startVPN()
        disposables.add(
                netKit.startTor(Tor.Settings(context = applicationContext, vpnMode = false, useBridges = false))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { netStatus ->
                                    logEvent("Tor Process ID:${netStatus.processId}")
                                },
                                { error ->
                                    logEvent("TorError:${error}")
                                }))
    }

    @SuppressLint("SetTextI18n")
    private fun testTORConnection() {

        txTorTestStatus.text = "Getting IP Address ... "
        txTorTestStatus2.text = "Checking socket connection ... "

        getIP()
        // Last IP 185.220.101.29
    }

    @SuppressLint("SetTextI18n")
    private fun getIP() {

        object : Thread() {
            override fun run() {
                val urlConnection = netKit.getHttpConnection(URL("https://api.ipify.org"))

                try {
                    val inStream = urlConnection.inputStream
                    val isw = InputStreamReader(inStream)

                    var data: Int = isw.read()
                    var output = ""

                    while (data != -1) {
                        val current = data.toChar()
                        data = isw.read()
                        output += current
                    }
                    txTorTestStatus.text = "IP assigned :" + output

                } catch (e: Exception) {
                    txTorTestStatus.text = e.toString()
                } finally {
                    urlConnection.disconnect()
                }


                //--------Socket Conn ----------------------------------
                try {
                    var socket = netKit.getSocketConnection("wss://echo.websocket.org",0)
                    var oos: ObjectOutputStream? = null
                    var ois: ObjectInputStream? = null

                    oos = ObjectOutputStream(socket.getOutputStream())
                    println("Sending request to Socket Server")
                    oos.writeObject("Data Sent");

                    ois = ObjectInputStream(socket.getInputStream())
                    var message = ois.readObject() as String
                    txTorTestStatus2.text = "Message: $message"
                    oos.writeObject("exit")
                    ois = ObjectInputStream(socket.getInputStream())
                    message = ois.readObject() as String
                    txTorTestStatus2.text = "Message: $message"

                    ois.close()
                    oos.close()
                } catch (e: Exception) {
                    txTorTestStatus2.text = e.toString()
                } finally {
                }

                //------------------------------------------------------
            }

        }.start()
    }

    override fun onProcessStatusUpdate(torInfo: Tor.Info?, message: String) {
        runOnUiThread {
            logEvent("Tor Status:${message}")
        }
    }

    override fun onConnStatusUpdate(torConnInfo: Tor.ConnectionInfo?, message: String) {
        runOnUiThread {
            logEvent("Tor Connection Status:${message}")
        }
    }

}