package ru.inforion.lab403.common.extensions

//import junit.framework.Assert.assertEquals
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by davydov_vn on 22/04/19.
 */

class StringTest {

    private val x = "DEADBEEF"
    private val xs = "DE AD BE EF"
    private val b = byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())

    @Test fun unhexlifyTest() = Assert.assertArrayEquals(b, x.unhexlify())
    @Test fun hexlifyTest() = assertEquals(x, b.hexlify())
    @Test fun hexlifySeparatorTest() = assertEquals(xs, b.hexlify(separator = ' '))

    @Test fun byte_hex2() = assertEquals("7F", 0x7F.hex2)

    @Test fun short_hex2() = assertEquals("FF", 0x7FFF.hex2)
    @Test fun short_hex4() = assertEquals("7FFF", 0x7FFF.hex4)

    @Test fun int_hex2() = assertEquals("FF", 0x7FFF_FFFF.hex2)
    @Test fun int_hex4() = assertEquals("FFFF", 0x7FFF_FFFF.hex4)
    @Test fun int_hex8() = assertEquals("7FFFFFFF", 0x7FFF_FFFF.hex8)

    @Test fun int_hex02() = assertEquals("0F", 0xF.hex2)
    @Test fun int_hex04() = assertEquals("00FF", 0xFF.hex4)
    @Test fun int_hex08() = assertEquals("000000FF", 0xFF.hex8)

    @Test fun long_hexF2() = assertEquals("FF", 0x7FFF_FFFF_FFFF_FFFF.hex2)
    @Test fun long_hexF4() = assertEquals("FFFF", 0x7FFF_FFFF_FFFF_FFFF.hex4)
    @Test fun long_hexF8() = assertEquals("FFFFFFFF", 0x7FFF_FFFF_FFFF_FFFF.hex8)
    @Test fun long_hexF16() = assertEquals("7FFFFFFFFFFFFFFF", 0x7FFF_FFFF_FFFF_FFFF.hex16)

    @Test fun long_hex02() = assertEquals("0F", 0xFL.hex2)
    @Test fun long_hex04() = assertEquals("00FF", 0xFFL.hex4)
    @Test fun long_hex08() = assertEquals("000000FF", 0xFFL.hex8)
    @Test fun long_hex016() = assertEquals("00000000000000FF", 0xFFL.hex16)

    @Test fun byte_hex_7F() = assertEquals("7F", 0x7F.hex)

    @Test fun short_hex_7F() = assertEquals("7F", 0x7F.hex)
    @Test fun short_hex_7FFF() = assertEquals("7FFF", 0x7FFF.hex)

    @Test fun int_hex_7F() = assertEquals("7F", 0x7F.hex)
    @Test fun int_hex_7FFF() = assertEquals("7FFF", 0x7FFF.hex)
    @Test fun int_hex_7FFF_FFFF() = assertEquals("7FFFFFFF", 0x7FFF_FFFF.hex)

    @Test fun long_hex_7F() = assertEquals("7F", 0x7F.hex)
    @Test fun long_hex_7FFF() = assertEquals("7FFF", 0x7FFF.hex)
    @Test fun long_hex_7FFF_FFFF() = assertEquals("7FFFFFFF", 0x7FFF_FFFF.hex)
    @Test fun long_hex_7FFF_FFFF_FFFF_FFFF() = assertEquals("7FFFFFFFFFFFFFFF", 0x7FFF_FFFF_FFFF_FFFF.hex)
}