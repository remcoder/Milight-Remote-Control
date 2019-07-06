package com.q42.milightremotecontrol

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

const val TAG = "MainActivity"
const val bridge = "hf-lpb100"

class MainActivity : AppCompatActivity(), CoroutineScope {

    lateinit var controller : MiLightController
    lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

        setContentView(R.layout.activity_main)

        controller = MiLightController(this@MainActivity)

        launch(Dispatchers.IO) {
            discoverBridge()
        }

        discover.setOnClickListener {
            discoverBridge()
        }

        on_button.setOnClickListener {
            Log.d(TAG, "Turning lights on")
            controller.turnOn()
        }

        off_button.setOnClickListener {
            Log.d(TAG, "Turning lights off")
            controller.turnOff()
        }

        brighter.setOnClickListener {
            Log.d(TAG, "Turning lights up a bit")
            controller.brighter()
        }

        dimmer.setOnClickListener {
            Log.d(TAG, "Turning lights down a bit")
            controller.dimmer()
        }

        max_brightness.setOnClickListener {
            Log.d(TAG, "Setting to max brightness")
            controller.maxBrightness()
        }

        night_mode.setOnClickListener {
            controller.nightMode()
        }

        warmer.setOnClickListener {
            controller.warmer()
        }

        cooler.setOnClickListener {
            controller.cooler()
        }

        link.setOnClickListener {
            controller.link()
        }

        unlink.setOnClickListener {
            controller.unlink()
        }
    }

    fun discoverBridge() {
        launch(Dispatchers.IO) {

            val bridgeAddress = async { controller.discover() }

            withContext(Dispatchers.Main) {

                bridge_id.text = bridgeAddress.await().toString()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        controller.cancelJobs() // Cancel job on activity destroy. After destroy all children jobs will be cancelled automatically
    }
}
