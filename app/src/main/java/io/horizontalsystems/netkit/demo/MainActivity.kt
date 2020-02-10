package io.horizontalsystems.netkit.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import io.horizontalsystems.netkit.NetKit
import io.horizontalsystems.tor.Tor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStreamReader
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.URL


class MainActivity : AppCompatActivity(), Tor.Listener {

    private val listItems = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private val disposables = CompositeDisposable()
    private var torStarted: Boolean = false

    val netKit = NetKit(this)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btnTor.setOnClickListener {
            if(!torStarted) {
                startTor()
                btnTor.text = "Stop Vpn"
            }
            else{
                stopTor()
                btnTor.text = "Start Vpn"
            }
        }

        btnTorTest.setOnClickListener {
            testTORConnection()
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        statusView.adapter = adapter
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

    private fun stopTor() {

    }


    private fun startTor() {

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
                    val socket = netKit.getSocketConnection("api.ipify.org",443)
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