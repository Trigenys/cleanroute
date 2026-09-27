package com.trigenys.cleanroute.domain

import java.time.Instant
import java.time.LocalDate

data class ZoneWorkload(
    val zoneId: ZoneId,
    val zoneName: String,
    val totalStops: Int,
    val completedStops: Int
) {
    val remainingStops: Int
        get() = (totalStops - completedStops).coerceAtLeast(0)
}

data class DailyCollectionStop(
    val visit: CollectionVisit,
    val customerName: String,
    val phone: String?,
    val addressLabel: String?
)

data class DailyCollectionRoute(
    val routeDayId: RouteDayId,
    val date: LocalDate,
    val zoneId: ZoneId,
    val zoneName: String,
    val stops: List<DailyCollectionStop>
) {
    val completedStops: Int
        get() = stops.count { it.visit.status != CollectionVisitStatus.SCHEDULED }

    val remainingStops: Int
        get() = stops.size - completedStops
}

data class CollectionVisitRevision(
    val visitId: CollectionVisitId,
    val revision: Int,
    val status: CollectionVisitStatus,
    val changedAt: Instant
)

interface CollectionWorkflowRepository {
    suspend fun zoneWorkloads(date: LocalDate): List<ZoneWorkload>

    suspend fun ensureRoute(
        date: LocalDate,
        zoneId: ZoneId
    ): DailyCollectionRoute

    suspend fun getRoute(
        date: LocalDate,
        zoneId: ZoneId
    ): DailyCollectionRoute?

    suspend fun recordOutcome(
        visitId: CollectionVisitId,
        outcome: CollectionVisitStatus,
        at: Instant
    ): CollectionVisit

    suspend fun revisions(
        visitId: CollectionVisitId
    ): List<CollectionVisitRevision>
}
