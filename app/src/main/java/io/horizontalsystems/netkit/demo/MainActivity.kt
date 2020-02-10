package io.horizontalsystems.netkit.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import io.horizontalsystems.netkit.NetKit
import io.horizontalsystems.tor.Tor
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url
import java.io.InputStreamReader
import java.net.URL


class MainActivity : AppCompatActivity(), Tor.Listener {

    interface GetIPApi {
        @GET
        @Headers("Content-Type: text/plain")
        fun getIP(@Url path: String): Flowable<String>
    }

    private lateinit var netKit: NetKit
    private val listItems = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private val disposables = CompositeDisposable()
    private var torStarted: Boolean = false


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //------------Init NetKit -------------------
        netKit = NetKit(context = applicationContext, torListener = this)
        //-------------------------------------------

        btnTor.setOnClickListener {
            if (!torStarted) {
                startTor()
                torStarted = true
                btnTor.text = "Stop Tor"
            } else {
                stopTor()
                torStarted = false
                btnTor.text = "Start Tor"
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
        disposables.add(
                netKit.stopTor()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { torStopped ->
                                    logEvent("Tor stopped:${torStopped}")
                                },
                                { error ->
                                    logEvent("TorError:${error}")
                                }))

    }


    private fun startTor() {

        disposables.add(
                netKit.startTor(useBridges = false)
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

        val checkIPUrl = "https://api.ipify.org"

        object : Thread() {
            override fun run() {
                val urlConnection = netKit.getHttpConnection(URL(checkIPUrl))

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

                txTorTestStatus2.text = "Getting IP from RetroFit :"

                //------------------------------------------------------
                val obser = netKit.buildRetrofit(checkIPUrl)
                        .create(GetIPApi::class.java)

                disposables.add(
                        obser.getIP("/").subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ result -> txTorTestStatus2.text = "IP assigned :" + result },
                                           { error ->
                                               txTorTestStatus2.text = error.toString()
                                           })
                )

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
