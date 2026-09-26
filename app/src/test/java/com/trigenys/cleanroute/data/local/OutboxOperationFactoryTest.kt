package com.trigenys.cleanroute.data.local

import com.trigenys.cleanroute.domain.CollectionVisit
import com.trigenys.cleanroute.domain.CollectionVisitId
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.RouteDayId
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class OutboxOperationFactoryTest {
    @Test
    fun sameVisitRevisionProducesSameOperationId() {
        val visit = CollectionVisit(
            id = CollectionVisitId("visit-1"),
            routeDayId = RouteDayId("route-1"),
            customerId = CustomerId("customer-1"),
            scheduledDate = LocalDate.of(2026, 9, 26),
            status = CollectionVisitStatus.COLLECTED,
            statusChangedAt = Instant.parse("2026-09-26T08:00:00Z"),
            revision = 1
        )

        assertEquals(
            OutboxOperationFactory.collectionVisit(visit).id,
            OutboxOperationFactory.collectionVisit(visit).id
        )
    }

    @Test
    fun correctedVisitProducesNewOperationId() {
        val original = CollectionVisit(
            id = CollectionVisitId("visit-1"),
            routeDayId = RouteDayId("route-1"),
            customerId = CustomerId("customer-1"),
            scheduledDate = LocalDate.of(2026, 9, 26),
            status = CollectionVisitStatus.COLLECTED,
            statusChangedAt = Instant.parse("2026-09-26T08:00:00Z"),
            revision = 1
        )
        val corrected = original.recordOutcome(
            CollectionVisitStatus.ABSENT,
            Instant.parse("2026-09-26T08:05:00Z")
        )

        assertNotEquals(
            OutboxOperationFactory.collectionVisit(original).id,
            OutboxOperationFactory.collectionVisit(corrected).id
        )
    }
}
