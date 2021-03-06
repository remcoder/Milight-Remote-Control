package com.remcoder.milightremotecontrol

import android.content.Context
import android.util.Log
import de.eon.futurelab.utils.UByte
import de.eon.futurelab.utils.unsignedByteArrayOf
import kotlinx.coroutines.*

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.coroutines.CoroutineContext


const val DEFAULT_MILIGHT_PORT = 5987

// temporary constant
const val zone = 1

class MiLightController(
        private val context: Context,
        private val port: Int = DEFAULT_MILIGHT_PORT
): CoroutineScope {

    private val job = Job()
     var bridgeAddress : InetAddress? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    data class Session(val socket : DatagramSocket, val id1: Int, val id2: Int)

    private fun requestSession(): Session {
        val socket = DatagramSocket()
        val requestSessionCmd = unsignedByteArrayOf(
                0x20, 0x00, 0x00, 0x00, 0x16, 0x02, 0x62, 0x3A,
                0xD5, 0xED, 0xA3, 0x01, 0xAE, 0x08, 0x2D, 0x46,
                0x61, 0x41, 0xA7, 0xF6, 0xDC, 0xAF, 0xD3, 0xE6,
                0x00, 0x00, 0x1E) // (use checksum instead?)

        val packet = DatagramPacket(requestSessionCmd, requestSessionCmd.count(), bridgeAddress, port)
        // send UDP packet
        socket.send(packet)

        // receive session id
        val receiveData = ByteArray(1024)
        val receivePacket = DatagramPacket(receiveData, receiveData.count())
        socket.receive(receivePacket)
        val session1 = UByte.fromByte(receivePacket.data[19]).toInt()
        val session2 = UByte.fromByte(receivePacket.data[20]).toInt()
        Log.d(TAG, "session id: $session1 $session2")
        return Session(socket, session1, session2)
    }

    private fun sendCommand(cmd: ByteArray) {

        if (bridgeAddress == null) {
            Log.w(TAG, "NO BRIDGE")
            return
        }

        Log.d(TAG, "Request session")
        launch(Dispatchers.IO) {
            try {
                // request session

                val session = requestSession()

                // wait 50ms
                delay(50)
                val prefix = unsignedByteArrayOf(0x80, 0x00, 0x00, 0x00, 0x11, session.id1, session.id2, 0x00, 0x05, 0x00)
                val _cmd = cmd + unsignedByteArrayOf(zone, 0x00)
                val checksum = unsignedByteArrayOf(_cmd.sum() and 0xff)
                val buffer = prefix + _cmd + checksum
                // send command using session id
                val packet2 = DatagramPacket(buffer, buffer.count(), bridgeAddress, port)
                session.socket.send(packet2)

                // Close socket
                session.socket.close()
            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    context.toast("Error turning on lights:\n${ex.message}")
                }
            }
        }
    }

    private val discoveryMessageV6 = unsignedByteArrayOf(
        0x48, 0x46, 0x2D, 0x41,
        0x31, 0x31, 0x41, 0x53,
        0x53, 0x49, 0x53, 0x54,
        0x48, 0x52, 0x45, 0x41,
        0x44)

    suspend fun discover() : InetAddress {
        Log.i(TAG, "Send discovery packet")

        val address = InetAddress.getByAddress(byteArrayOfInts(255, 255, 255, 255))
        val broadcast = DatagramPacket(
            discoveryMessageV6,
            discoveryMessageV6.size,
            address,
            DEFAULT_MILIGHT_PORT
        )

        val recvBuf = ByteArray(32)
        val response = DatagramPacket(recvBuf, recvBuf.size)
        DatagramSocket().use { socket ->
            socket.broadcast = true

            try {
                socket.send(broadcast)
                socket.receive(response)
            } catch (e: Exception) {
               e.printStackTrace()
            }

            val data = String(response.data.slice(0..response.length).toByteArray())
            val parts = data.split(Regex(",|:"))

            withContext(Dispatchers.Main) {
                Log.i(TAG, ("received ${response.length} bytes from ${response.address}: $parts"))
            }
        }

        bridgeAddress = response.address
        return response.address
    }

    // WW/CW commands

    fun turnOn() {
        sendCommand(unsignedByteArrayOf(
                0x31, 0x00, 0x00, 0x01, 0x01, 0x07, 0x00, 0x00, 0x00))
    }

    fun turnOff() {
        sendCommand(unsignedByteArrayOf(
                0x31, 0x00, 0x00, 0x01, 0x01, 0x08, 0x00, 0x00, 0x00))
    }

    fun brighter() {
        sendCommand(unsignedByteArrayOf(
                0x31, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00))
    }

    fun dimmer() {
        sendCommand(unsignedByteArrayOf(
                0x31, 0x00, 0x00, 0x01, 0x01, 0x02, 0x00, 0x00, 0x00))
    }

    fun maxBrightness() {
        sendCommand(unsignedByteArrayOf(
                0x31, 0x00, 0x00, 0x01, 0x81, 0x07, 0x00, 0x00, 0x00))
    }

    fun nightMode() {
        sendCommand(unsignedByteArrayOf(
                0x31, 0x00, 0x00, 0x01, 0x01, 0x06, 0x00, 0x00, 0x00
        ))
    }

    fun warmer() {
        sendCommand(unsignedByteArrayOf(
                0x31, 0x00, 0x00, 0x01, 0x01, 0x03, 0x00, 0x00, 0x00
        ))
    }

    fun cooler() {
        sendCommand(unsignedByteArrayOf(
                0x31, 0x00, 0x00, 0x01, 0x01, 0x04, 0x00, 0x00, 0x00
        ))
    }

    fun link() {
        sendCommand(unsignedByteArrayOf(
                0x3D, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00
        ))
    }

    fun unlink() {
        sendCommand(unsignedByteArrayOf(
                0x3E, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00
        ))
    }

    fun cancelJobs() {
        job.cancel()
    }

    fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
}
