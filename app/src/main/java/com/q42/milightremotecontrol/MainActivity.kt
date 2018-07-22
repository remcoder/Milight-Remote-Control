package com.q42.milightremotecontrol

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.net.InetAddress

const val TAG = "MainActivity"
const val bridge = "hf-lpb100"

class MainActivity : AppCompatActivity() {

    lateinit var controller : MiLightController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launch(CommonPool) {
            controller = MiLightController(InetAddress.getByName(bridge))
        }

        on_button.onClick {
            Log.i(TAG, "Turning lights on")
            launch(CommonPool) {
                controller.turnOn()
            }
        }

        off_button.onClick {
            Log.i(TAG, "Turning lights off")
            launch(CommonPool) {
                controller.turnOff()
            }
        }
    }

}
