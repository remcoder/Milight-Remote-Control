package com.q42.milightremotecontrol

import android.util.Log
import de.eon.futurelab.utils.UByte
import de.eon.futurelab.utils.unsignedByteArrayOf
import kotlinx.coroutines.delay

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

const val DEFAULT_MILIGHT_PORT = 5987

class MiLightController(private val address: InetAddress, private val port: Int = DEFAULT_MILIGHT_PORT) {

    data class Session(val id1 : Int, val id2 : Int)

    private fun requestSession(socket : DatagramSocket) : Session {
        val requestSessionCmd = unsignedByteArrayOf(
                0x20, 0x00, 0x00, 0x00, 0x16, 0x02, 0x62, 0x3A,
                0xD5, 0xED, 0xA3, 0x01, 0xAE, 0x08, 0x2D, 0x46,
                0x61, 0x41, 0xA7, 0xF6, 0xDC, 0xAF, 0xD3, 0xE6,
                0x00, 0x00, 0x1E) // (use checksum instead?)

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

    private suspend fun sendCommand(func : (s1:Int, s2:Int)-> ByteArray) {
        // request session
        val socket = DatagramSocket()
        val (session1, session2) = requestSession(socket)

        // wait 50ms
        delay(50)
        val cmd = func(session1, session2)
        // send command using session id
        val packet2 = DatagramPacket(cmd, cmd.count(), address, port)
        socket.send(packet2)

        // Close socket
        socket.close()
    }

    private fun turnLightsOnCmd(session1: Int, session2 : Int) = unsignedByteArrayOf(
            0x80,0x00,0x00,0x00,0x11,session1,session2,0x00,0x05,
            0x00,0x31,0x00,0x00,0x01,0x01,0x07,0x00,0x00,
            0x00,0x01,0x00,0x3B)

    private fun turnLightsOffCmd(session1: Int, session2 : Int) = unsignedByteArrayOf(
            0x80,0x00,0x00,0x00,0x11,session1,session2,0x00,0x05,
            0x00,0x31,0x00,0x00,0x01,0x01,0x08,0x00,0x00,
            0x00,0x01,0x00,0x3C)

    suspend fun turnOn() {
       sendCommand(this::turnLightsOnCmd)
    }

    suspend fun turnOff() {
       sendCommand(this::turnLightsOffCmd)
    }
}
