package com.q42.milightremotecontrol

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.jetbrains.anko.toast
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

        launch(Dispatchers.IO) {
            val ip = try {
                InetAddress.getByName(bridge)
            } catch (ex: UnknownHostException) {
                withContext(Dispatchers.Main) {
                    toast("Error while looking for bridge:\n${ex.message}")
                }
                null
            }
            ip?.let {
                Log.i(TAG, "$ip")
                controller = MiLightController(this@MainActivity, ip)
            }
        }

        on_button.setOnClickListener {
            Log.i(TAG, "Turning lights on")
            controller.turnOn()
        }

        off_button.setOnClickListener {
            Log.i(TAG, "Turning lights off")
            controller.turnOff()
        }

        brighter.setOnClickListener {
            Log.i(TAG, "Turning lights up a bit")
            controller.brighter()
        }

        dimmer.setOnClickListener {
            Log.i(TAG, "Turning lights down a bit")
            controller.dimmer()
        }

        max_brightness.setOnClickListener {
            Log.i(TAG, "Setting to max brightness")
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


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        controller.cancelJobs() // Cancel job on activity destroy. After destroy all children jobs will be cancelled automatically
    }
}
