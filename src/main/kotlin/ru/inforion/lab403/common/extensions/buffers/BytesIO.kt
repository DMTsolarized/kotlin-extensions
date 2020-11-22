package ru.inforion.lab403.common.extensions.buffers

/**
 * Bytes buffer input-output interface
 *
 * @since 0.3.4
 */
interface BytesIO {
    /**
     * Available number of bytes that can be written to buffer before overflow
     */
    val writeAvailable: Int

    /**
     * Available number of bytes that can be read from buffer before underflow
     */
    val readAvailable: Int

    /**
     * Writes [count] specified [bytes] from [offset] into buffer
     *
     * @throws IllegalArgumentException if overflow will occurred
     */
    fun write(bytes: ByteArray, offset: Int = 0, count: Int = bytes.size)

    /**
     * Read [count] bytes from buffer
     *
     * @throws IllegalArgumentException if underflow will occurred
     */
    fun read(count: Int): ByteArray
}