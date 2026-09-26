package com.trigenys.cleanroute.domain

import java.time.Instant
import java.time.LocalDate

enum class CollectionVisitStatus {
    SCHEDULED,
    COLLECTED,
    ABSENT,
    NO_WASTE
}

data class RouteDay(
    val id: RouteDayId,
    val date: LocalDate,
    val zoneId: ZoneId,
    val customerIds: List<CustomerId>
) {
    init {
        require(customerIds.distinct().size == customerIds.size) {
            "A customer may appear only once in a route day"
        }
    }
}

data class CollectionVisit(
    val id: CollectionVisitId,
    val routeDayId: RouteDayId,
    val customerId: CustomerId,
    val scheduledDate: LocalDate,
    val status: CollectionVisitStatus = CollectionVisitStatus.SCHEDULED,
    val statusChangedAt: Instant? = null,
    val revision: Int = 0
) {
    init {
        require(revision >= 0) { "Visit revision must not be negative" }
        require(
            (status == CollectionVisitStatus.SCHEDULED && statusChangedAt == null) ||
                (status != CollectionVisitStatus.SCHEDULED && statusChangedAt != null)
        ) {
            "A finalized visit must have statusChangedAt and a scheduled visit must not"
        }
    }

    fun recordOutcome(
        outcome: CollectionVisitStatus,
        at: Instant
    ): CollectionVisit {
        require(outcome != CollectionVisitStatus.SCHEDULED) {
            "SCHEDULED is not a collection outcome"
        }

        if (outcome == status) return this

        return copy(
            status = outcome,
            statusChangedAt = at,
            revision = revision + 1
        )
    }
}
