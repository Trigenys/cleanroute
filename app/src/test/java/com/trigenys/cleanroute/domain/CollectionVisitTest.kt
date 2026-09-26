package com.trigenys.cleanroute.domain

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class CollectionVisitTest {
    private val visit = CollectionVisit(
        id = CollectionVisitId("visit-1"),
        routeDayId = RouteDayId("route-1"),
        customerId = CustomerId("customer-1"),
        scheduledDate = LocalDate.of(2026, 9, 26)
    )

    @Test
    fun repeatedOutcomeIsIdempotent() {
        val at = Instant.parse("2026-09-26T08:00:00Z")
        val collected = visit.recordOutcome(CollectionVisitStatus.COLLECTED, at)

        val replay = collected.recordOutcome(CollectionVisitStatus.COLLECTED, at.plusSeconds(30))

        assertSame(collected, replay)
        assertEquals(1, replay.revision)
        assertEquals(at, replay.statusChangedAt)
    }

    @Test
    fun correctionChangesOutcomeAndIncrementsRevision() {
        val collected = visit.recordOutcome(
            CollectionVisitStatus.COLLECTED,
            Instant.parse("2026-09-26T08:00:00Z")
        )

        val corrected = collected.recordOutcome(
            CollectionVisitStatus.ABSENT,
            Instant.parse("2026-09-26T08:05:00Z")
        )

        assertEquals(CollectionVisitStatus.ABSENT, corrected.status)
        assertEquals(2, corrected.revision)
    }

    @Test(expected = IllegalArgumentException::class)
    fun scheduledIsNotAcceptedAsOutcome() {
        visit.recordOutcome(
            CollectionVisitStatus.SCHEDULED,
            Instant.parse("2026-09-26T08:00:00Z")
        )
    }
}
