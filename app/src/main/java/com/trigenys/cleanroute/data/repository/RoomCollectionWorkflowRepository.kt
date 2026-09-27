package com.trigenys.cleanroute.data.repository

import com.trigenys.cleanroute.data.local.CleanRouteDatabase
import com.trigenys.cleanroute.data.local.OutboxOperationFactory
import com.trigenys.cleanroute.data.local.entity.CollectionVisitEntity
import com.trigenys.cleanroute.data.local.entity.CollectionVisitRevisionEntity
import com.trigenys.cleanroute.data.local.entity.RouteDayCustomerEntity
import com.trigenys.cleanroute.data.local.entity.RouteDayEntity
import com.trigenys.cleanroute.data.local.toDomain
import com.trigenys.cleanroute.data.local.toEntity
import com.trigenys.cleanroute.domain.CollectionVisit
import com.trigenys.cleanroute.domain.CollectionVisitId
import com.trigenys.cleanroute.domain.CollectionVisitRevision
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.CollectionWorkflowRepository
import com.trigenys.cleanroute.domain.DailyCollectionRoute
import com.trigenys.cleanroute.domain.DailyCollectionStop
import com.trigenys.cleanroute.domain.RouteDayId
import com.trigenys.cleanroute.domain.ZoneId
import com.trigenys.cleanroute.domain.ZoneWorkload
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class RoomCollectionWorkflowRepository(
    private val database: CleanRouteDatabase
) : CollectionWorkflowRepository {
    override suspend fun zoneWorkloads(date: LocalDate): List<ZoneWorkload> =
        database.routeDayDao()
            .zoneWorkloads(date.toString())
            .map { row ->
                ZoneWorkload(
                    zoneId = ZoneId(row.zoneId),
                    zoneName = row.zoneName,
                    totalStops = row.totalStops,
                    completedStops = row.completedStops
                )
            }

    override suspend fun ensureRoute(
        date: LocalDate,
        zoneId: ZoneId
    ): DailyCollectionRoute {
        val routeDayDao = database.routeDayDao()
        val zone = routeDayDao.getZone(zoneId.value)
            ?: error("Zone introuvable.")
        val routeDayId = RouteDayId(
            stableId("route:${date}:${zoneId.value}")
        )
        val customers = routeDayDao.activeCustomersInZone(zoneId.value)

        val routeDay = RouteDayEntity(
            id = routeDayId.value,
            dateIso = date.toString(),
            zoneId = zoneId.value
        )
        val routeCustomers = customers.mapIndexed { index, customer ->
            RouteDayCustomerEntity(
                routeDayId = routeDayId.value,
                customerId = customer.id,
                position = index
            )
        }
        val visits = customers.map { customer ->
            CollectionVisitEntity(
                id = stableId("visit:${date}:${customer.id}"),
                routeDayId = routeDayId.value,
                customerId = customer.id,
                scheduledDateIso = date.toString(),
                status = CollectionVisitStatus.SCHEDULED.name,
                statusChangedAtEpochMs = null,
                revision = 0
            )
        }

        routeDayDao.ensureRoute(
            routeDay = routeDay,
            customers = routeCustomers,
            visits = visits
        )

        return requireNotNull(getRoute(date, zoneId)) {
            "La tournée n’a pas pu être créée."
        }.copy(zoneName = zone.name)
    }

    override suspend fun getRoute(
        date: LocalDate,
        zoneId: ZoneId
    ): DailyCollectionRoute? {
        val dao = database.routeDayDao()
        val routeDay = dao.getRouteDay(date.toString(), zoneId.value)
            ?: return null
        val zone = dao.getZone(zoneId.value)
            ?: return null

        return DailyCollectionRoute(
            routeDayId = RouteDayId(routeDay.id),
            date = date,
            zoneId = zoneId,
            zoneName = zone.name,
            stops = dao.stops(routeDay.id).map { row ->
                DailyCollectionStop(
                    visit = row.visit.toDomain(),
                    customerName = row.customerName,
                    phone = row.phone,
                    addressLabel = row.addressLabel
                )
            }
        )
    }

    override suspend fun recordOutcome(
        visitId: CollectionVisitId,
        outcome: CollectionVisitStatus,
        at: Instant
    ): CollectionVisit {
        require(outcome != CollectionVisitStatus.SCHEDULED) {
            "SCHEDULED n’est pas un résultat de collecte."
        }

        val dao = database.collectionVisitDao()
        val current = dao.get(visitId.value)?.toDomain()
            ?: error("Passage introuvable.")
        val updated = current.recordOutcome(outcome, at)

        if (updated == current) {
            return current
        }

        val changedAt = requireNotNull(updated.statusChangedAt)
        dao.recordOutcomeWithAudit(
            visit = updated.toEntity(),
            revision = CollectionVisitRevisionEntity(
                visitId = updated.id.value,
                revision = updated.revision,
                status = updated.status.name,
                changedAtEpochMs = changedAt.toEpochMilli()
            ),
            operation = OutboxOperationFactory.collectionVisit(updated)
        )
        return updated
    }

    override suspend fun revisions(
        visitId: CollectionVisitId
    ): List<CollectionVisitRevision> =
        database.collectionVisitDao()
            .revisionHistory(visitId.value)
            .map { revision ->
                CollectionVisitRevision(
                    visitId = CollectionVisitId(revision.visitId),
                    revision = revision.revision,
                    status = CollectionVisitStatus.valueOf(revision.status),
                    changedAt = Instant.ofEpochMilli(revision.changedAtEpochMs)
                )
            }

    private fun stableId(value: String): String =
        UUID.nameUUIDFromBytes(value.toByteArray(StandardCharsets.UTF_8)).toString()
}
