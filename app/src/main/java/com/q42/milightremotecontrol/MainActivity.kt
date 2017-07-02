package com.q42.milightremotecontrol

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import de.eon.futurelab.utils.UByte
import de.eon.futurelab.utils.toUnsignedByteArray
import de.eon.futurelab.utils.unsignedByteArrayOf
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.logging.Logger

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MainActivity"
    }

    val MILIGHT_PORT = 5987
    val bridge = "hf-lpb100"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        on_button.onClick {
            Log.i(TAG, "Turning lights on")
            async() {
                turnOn(InetAddress.getByName(bridge))
            }
        }

        off_button.onClick {
            Log.i(TAG, "Turning lights off")
            async() {
                turnOff(InetAddress.getByName(bridge))
            }
        }
    }

    data class Session(val id1 : Int, val id2 : Int)
    fun requestSession(socket : DatagramSocket, address: InetAddress, port: Int) : Session {
        val requestSessionCmd = intArrayOf(
                0x20, 0x00, 0x00, 0x00, 0x16, 0x02, 0x62, 0x3A,
                0xD5, 0xED, 0xA3, 0x01, 0xAE, 0x08, 0x2D, 0x46,
                0x61, 0x41, 0xA7, 0xF6, 0xDC, 0xAF, 0xD3, 0xE6,
                0x00, 0x00, 0x1E) // (use checksum instead?)
                .toUnsignedByteArray()

        val packet = DatagramPacket(requestSessionCmd ,requestSessionCmd.count(), address, port)
        // send UDP packet
        socket.send(packet)

        // receive session id
        val receiveData = ByteArray(1024)
        val receivePacket = DatagramPacket(receiveData, receiveData.count())
        socket.receive(receivePacket)
        val session1 = UByte.fromByte(receivePacket.data[19])
        val session2 = UByte.fromByte(receivePacket.data[20])
        Log.i(TAG, "session id: $session1 $session2")
        return Session(session1.toInt(), session2.toInt())
    }

    fun turnOn(address: InetAddress) {
        // request session
        val socket = DatagramSocket()
        val (session1, session2) = requestSession(socket, address, MILIGHT_PORT)

        // wait 50ms
        Thread.sleep(50)

        // send command using session id
        val turnLightsOnCmd = unsignedByteArrayOf(
                0x80,0x00,0x00,0x00,0x11,session1,session2,0x00,0x05,
                0x00,0x31,0x00,0x00,0x01,0x01,0x07,0x00,0x00,
                0x00,0x01,0x00,0x3B)

        val packet2 = DatagramPacket(turnLightsOnCmd, turnLightsOnCmd.count(), address, MILIGHT_PORT)
        socket.send(packet2)

        // Close socket
        socket.close()
    }

    fun turnOff(address: InetAddress) {
        // request session
        val socket = DatagramSocket()
        val (session1, session2) = requestSession(socket, address, MILIGHT_PORT)

        // wait 50ms
        Thread.sleep(50)

        // send command using session id
        val turnLightsOffCmd = unsignedByteArrayOf(
                0x80,0x00,0x00,0x00,0x11,session1,session2,0x00,0x05,
                0x00,0x31,0x00,0x00,0x01,0x01,0x08,0x00,0x00,
                0x00,0x01,0x00,0x3C)

        val packet2 = DatagramPacket(turnLightsOffCmd, turnLightsOffCmd.count(), address, MILIGHT_PORT)
        socket.send(packet2)

        // Close socket
        socket.close()
    }
}
