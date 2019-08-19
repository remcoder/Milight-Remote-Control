package com.remcoder.milightremotecontrol

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var controller : MiLightController
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

        setSupportActionBar(toolbar)
        setContentView(R.layout.activity_main)
        toolbar.title = "Milight Control"
        toolbar.setTitleTextColor(getColor(R.color.white))


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

    private fun discoverBridge() {
        bridge_status.setImageResource(R.drawable.ic_circle_orange)

        launch(Dispatchers.IO) {

            val bridgeAddress  = try {
                async { controller.discover() }
            }
            catch (e: Exception) {
                bridge_status.setImageResource(R.drawable.ic_circle_red)
                null
            }

            bridgeAddress?.let {

                withContext(Dispatchers.Main) {

                    bridge_id.text = it.await().toString()
                    bridge_status.setImageResource(R.drawable.ic_circle_green)
                }
            }

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        controller.cancelJobs() // Cancel job on activity destroy. After destroy all children jobs will be cancelled automatically
    }
}
