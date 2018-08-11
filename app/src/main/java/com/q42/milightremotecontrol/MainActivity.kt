package com.q42.milightremotecontrol

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import java.net.InetAddress
import java.net.UnknownHostException

const val TAG = "MainActivity"
const val bridge = "hf-lpb100"

class MainActivity : AppCompatActivity() {

    lateinit var controller : MiLightController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launch(CommonPool) {
            try {
                controller = MiLightController(InetAddress.getByName(bridge))
            } catch (ex: UnknownHostException) {
                launch(UI) {
                    toast("Error while looking for bridge:\n${ex.message}")
                }
            }
        }

        on_button.onClick {
            Log.i(TAG, "Turning lights on")
            launch(CommonPool) {
                try {
                    controller.turnOn()
                } catch(ex: Exception)  {
                    launch(UI) {
                        toast("Error turning on lights:\n${ex.message}")
                    }
                }
            }
        }

        off_button.onClick {
            Log.i(TAG, "Turning lights off")
            launch(CommonPool) {
                try {
                    controller.turnOff()
                } catch(ex: Exception)  {
                    launch(UI) {
                        toast("Error turning off lights:\n${ex.message}")
                    }
                }
            }
        }
    }

}
