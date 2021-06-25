@file:Suppress("NOTHING_TO_INLINE", "unused")

package ru.inforion.lab403.common.extensions

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.util.*

inline fun DataOutputStream.writeTribyte(v: Int) {
    write(v ushr 16 and 0xFF)
    write(v ushr 8 and 0xFF)
    write(v ushr 0 and 0xFF)
}

inline fun DataInputStream.readTribyte(): Int {
    val ch1 = read()
    val ch2 = read()
    val ch3 = read()
    if (ch1 or ch2 or ch3 < 0) throw EOFException()
    return (ch1 shl 16) or (ch2 shl 8) or (ch3 shl 0)
}

inline fun DataOutputStream.writeDate(timestamp: Date) = writeLong(timestamp.time)

inline fun DataInputStream.readDate(): Date = Date(readLong())

inline fun DataOutputStream.writeLongOptional(value: Long?) {
    writeBoolean(value != null)
    if (value != null) writeLong(value)
}

inline fun DataInputStream.readLongOrNull() = if (readBoolean()) readLong() else null

inline fun DataOutputStream.writeIntOptional(value: Int?) {
    writeBoolean(value != null)
    if (value != null) writeInt(value)
}

inline fun DataInputStream.readIntOrNull() = if (readBoolean()) readInt() else null

inline fun DataOutputStream.writeStringIso(value: String) {
    writeInt(value.length)
    write(value.convertToBytes())
}

inline fun DataInputStream.readStringIso() = readNBytes(readInt()).convertToString()

inline fun DataOutputStream.writeStringIsoOptional(value: String?) {
    writeBoolean(value != null)
    if (value != null) writeStringIso(value)
}

inline fun DataInputStream.readStringIsoOrNull() = if (readBoolean()) readStringIso() else null

inline fun DataOutputStream.writeByteArray(value: ByteArray) {
    writeInt(value.size)
    write(value)
}

inline fun DataInputStream.readByteArray(): ByteArray = readNBytes(readInt())

inline fun DataOutputStream.writeByteArrayOptional(value: ByteArray?) {
    writeBoolean(value != null)
    if (value != null) writeByteArray(value)
}

inline fun DataInputStream.readByteArrayOrNull(): ByteArray? = if (readBoolean()) readByteArray() else null

inline fun <K, V> DataOutputStream.writeDictionary(
    value: Dictionary<K, V>,
    write: DataOutputStream.(Map.Entry<K, V>) -> Unit
) {
    writeInt(value.size)
    value.forEach { write(it) }
}

inline fun <K, V> DataInputStream.readDictionary(
    read: DataInputStream.() -> Pair<K, V>
): Dictionary<K, V> {
    val size = readInt()
    val result = dictionary<K, V>(capacity(size))
    repeat(readInt()) {
        val (key, value) = read(this)
        result[key] = value
    }
    return result
}

inline fun <K, V> DataInputStream.readDictionary(
    key: DataInputStream.() -> K,
    value: DataInputStream.() -> V,
): Dictionary<K, V> {
    val size = readInt()
    val result = dictionary<K, V>(capacity(size))
    repeat(size) { result[key()] = value() }
    return result
}

inline fun <T : Enum<T>> DataOutputStream.writeEnumValue(value: T) = writeStringIso(value.name)

inline fun <reified T : Enum<T>> DataInputStream.readEnumValue() = enumValueOf<T>(readStringIso())