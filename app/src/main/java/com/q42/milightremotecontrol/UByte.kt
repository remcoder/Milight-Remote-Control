package de.eon.futurelab.utils

import java.lang.IllegalArgumentException

class UByte(private val internal: Int) {
    companion object {
        /**
         * A constant holding the minimum value an instance of UByte can have.
         */
        const val MIN_VALUE: Int = 0

        /**
         * A constant holding the maximum value an instance of UByte can have.
         */
        const val MAX_VALUE: Int = 255

        fun fromNibbles(highNibble: UByte, lowNibble: UByte): UByte {
            return UByte((highNibble.toInt() shl 4) + lowNibble.toInt())
        }

        fun fromByte(byte: Byte): UByte {
            return UByte(byte.toInt() and 0xff)
        }
    }

    init {
        if (internal < MIN_VALUE) throw IllegalArgumentException("UByte cannot by smaller than $MIN_VALUE (value: $internal)")
        if (internal > MAX_VALUE) throw IllegalArgumentException("UByte cannot by greater than $MAX_VALUE (value: $internal)")
    }

    fun getNibbles(): Pair<UByte, UByte> {
        val lowNibble: UByte = UByte(internal and 0x0f)
        val highNibble: UByte = UByte((internal shr 4) and 0x0f)
        return Pair(highNibble, lowNibble)
    }

    operator fun inc(): UByte {
        return if (internal == MAX_VALUE) UByte(MIN_VALUE) else UByte(internal + 1)
    }

    fun toByte(): Byte {
        return (internal and 0xFF).toByte()
    }

    fun toInt(): Int {
        return internal
    }

    override fun toString(): String {
        val hexArray = "0123456789ABCDEF".toCharArray()
        val char2 = hexArray[internal and 0x0f]
        val char1 = hexArray[internal shr 4 and 0x0f]
        return "$char1$char2"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as UByte

        if (internal != other.internal) return false

        return true
    }

    override fun hashCode(): Int {
        return internal
    }
}

fun IntArray.toUnsignedByteArray() = this.map { UByte(it).toByte() }.toByteArray()
fun unsignedByteArrayOf(vararg args: Int) = intArrayOf(*args).toUnsignedByteArray()
