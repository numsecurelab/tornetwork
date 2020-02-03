package io.horizontalsystems.netkit.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import io.horizontalsystems.netkit.NetKit
import io.horizontalsystems.netkit.network.NetworkRouterSettings
import io.horizontalsystems.tor.TorEventHandler
import io.horizontalsystems.tor.TorSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    val listItems = ArrayList<String>()
    lateinit var adapter: ArrayAdapter<String>
    lateinit var netKit: NetKit
    val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnTor.setOnClickListener {
            startTORClient()
        }

        btnTorTest.setOnClickListener {
            testTORConnection()
        }

        adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                listItems
        )

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

    private fun startTORClient() {
        netKit = NetKit(true, NetworkRouterSettings(context = applicationContext))

        disposables.add(
                netKit.initNetworkRouter()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { torStatus ->
                                    logEvent("TorStatus:${torStatus.processId}")
                                },
                                { error ->
                                    logEvent("TorError:${error}")
                                }))
    }

    @SuppressLint("SetTextI18n")
    private fun testTORConnection() {
        System.setProperty("https.proxyHost", "localhost")
        System.setProperty("https.proxyPort", "8118")

        Settings.Secure.getString(
                contentResolver, "http_proxy"
        )

        txTorTestStatus.text =
                "Proxy Host:${System.getProperty("https.proxyHost")}," +
                        "Proxy Port:${Settings.Global.HTTP_PROXY}"

        getIP()
        // Last IP 185.220.101.29
    }

    private fun getIP() {

        object : Thread() {
            override fun run() {
                val url = URL("https://api.ipify.org")
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection


                try {
                    val inStream = urlConnection.getInputStream()
                    val isw = InputStreamReader(inStream)

                    var data: Int = isw.read();
                    var output: String = ""

                    while (data != -1) {
                        val current = data.toChar()
                        data = isw.read();
                        output += current
                    }
                    txTorTestStatus.text = "IP assigned :  ${output}"
                } catch (e: Exception) {
                    txTorTestStatus.text = e.toString()
                } finally {
                    urlConnection.disconnect()
                }
            }

        }.start()
    }
}