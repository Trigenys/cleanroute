package com.trigenys.cleanroute.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.trigenys.cleanroute.data.local.DailyCollectionStopRow
import com.trigenys.cleanroute.data.local.ZoneWorkloadRow
import com.trigenys.cleanroute.data.local.entity.CollectionVisitEntity
import com.trigenys.cleanroute.data.local.entity.CustomerEntity
import com.trigenys.cleanroute.data.local.entity.RouteDayCustomerEntity
import com.trigenys.cleanroute.data.local.entity.RouteDayEntity
import com.trigenys.cleanroute.data.local.entity.ZoneEntity

@Dao
abstract class RouteDayDao {
    @Query(
        """
        SELECT
            z.id AS zoneId,
            z.name AS zoneName,
            COUNT(c.id) AS totalStops,
            SUM(
                CASE
                    WHEN v.status IS NOT NULL AND v.status != 'SCHEDULED' THEN 1
                    ELSE 0
                END
            ) AS completedStops
        FROM zones z
        INNER JOIN customers c
            ON c.zoneId = z.id
           AND c.status = 'ACTIVE'
        LEFT JOIN collection_visits v
            ON v.customerId = c.id
           AND v.scheduledDateIso = :dateIso
        GROUP BY z.id, z.name
        ORDER BY z.name COLLATE NOCASE ASC
        """
    )
    abstract suspend fun zoneWorkloads(dateIso: String): List<ZoneWorkloadRow>

    @Query(
        """
        SELECT * FROM route_days
        WHERE dateIso = :dateIso
          AND zoneId = :zoneId
        LIMIT 1
        """
    )
    abstract suspend fun getRouteDay(
        dateIso: String,
        zoneId: String
    ): RouteDayEntity?

    @Query("SELECT * FROM zones WHERE id = :zoneId LIMIT 1")
    abstract suspend fun getZone(zoneId: String): ZoneEntity?

    @Query(
        """
        SELECT * FROM customers
        WHERE zoneId = :zoneId
          AND status = 'ACTIVE'
        ORDER BY name COLLATE NOCASE ASC, id ASC
        """
    )
    abstract suspend fun activeCustomersInZone(zoneId: String): List<CustomerEntity>

    @Query(
        """
        SELECT
            v.*,
            c.name AS customerName,
            c.phone AS phone,
            c.addressLabel AS addressLabel
        FROM collection_visits v
        INNER JOIN customers c ON c.id = v.customerId
        LEFT JOIN route_day_customers rdc
            ON rdc.routeDayId = v.routeDayId
           AND rdc.customerId = v.customerId
        WHERE v.routeDayId = :routeDayId
        ORDER BY COALESCE(rdc.position, 2147483647) ASC, c.name COLLATE NOCASE ASC
        """
    )
    abstract suspend fun stops(routeDayId: String): List<DailyCollectionStopRow>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertRouteDay(routeDay: RouteDayEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertRouteCustomers(
        rows: List<RouteDayCustomerEntity>
    ): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertVisits(
        visits: List<CollectionVisitEntity>
    ): List<Long>

    @Transaction
    open suspend fun ensureRoute(
        routeDay: RouteDayEntity,
        customers: List<RouteDayCustomerEntity>,
        visits: List<CollectionVisitEntity>
    ) {
        insertRouteDay(routeDay)
        if (customers.isNotEmpty()) {
            insertRouteCustomers(customers)
        }
        if (visits.isNotEmpty()) {
            insertVisits(visits)
        }
    }
}
