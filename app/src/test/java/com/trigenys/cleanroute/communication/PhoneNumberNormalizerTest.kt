package com.trigenys.cleanroute.communication

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneNumberNormalizerTest {
    private val normalizer = PhoneNumberNormalizer("237")

    @Test
    fun cameroonLocalNumberGetsCountryCode() {
        val result = normalizer.normalize("690 00 00 01")

        assertEquals("+237690000001", result?.e164)
        assertEquals("237690000001", result?.internationalDigits)
    }

    @Test
    fun alreadyInternationalNumbersArePreserved() {
        assertEquals(
            "+237690000001",
            normalizer.normalize("+237 690 00 00 01")?.e164
        )
        assertEquals(
            "+237690000001",
            normalizer.normalize("00237 690 00 00 01")?.e164
        )
        assertEquals(
            "+33612345678",
            normalizer.normalize("+33 6 12 34 56 78")?.e164
        )
    }

    @Test
    fun unusableNumbersAreRejected() {
        assertNull(normalizer.normalize(""))
        assertNull(normalizer.normalize("123"))
    }
}
