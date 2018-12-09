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
                controller = MiLightController(ip)
            }
        }

        on_button.setOnClickListener {
            Log.i(TAG, "Turning lights on")
            launch(Dispatchers.IO) {
                try {
                    controller.turnOn()
                } catch (ex: Exception) {
                    withContext(Dispatchers.Main) {
                        toast("Error turning on lights:\n${ex.message}")
                    }
                }
            }
        }

        off_button.setOnClickListener {
            Log.i(TAG, "Turning lights off")
            launch(Dispatchers.IO) {
                try {
                    controller.turnOff()
                } catch (ex: Exception) {
                    withContext(Dispatchers.Main) {
                        toast("Error turning off lights:\n${ex.message}")
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel job on activity destroy. After destroy all children jobs will be cancelled automatically
    }
}
