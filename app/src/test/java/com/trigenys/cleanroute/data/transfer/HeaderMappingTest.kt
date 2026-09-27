package com.trigenys.cleanroute.data.transfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeaderMappingTest {
    @Test
    fun frenchHeadersAreMappedByNameNotPosition() {
        val mapping = HeaderMapping.map(
            listOf("Quartier", "Téléphone", "Nom client", "Tarif", "Observation")
        )

        assertEquals("zone", mapping.canonicalByIndex[0])
        assertEquals("phone", mapping.canonicalByIndex[1])
        assertEquals("name", mapping.canonicalByIndex[2])
        assertEquals("monthly_fee_xaf", mapping.canonicalByIndex[3])
        assertTrue(mapping.unknownHeaders.contains("Observation"))
        assertTrue(mapping.missingRequired.isEmpty())
    }
}
