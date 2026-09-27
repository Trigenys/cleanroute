package com.trigenys.cleanroute.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.trigenys.cleanroute.data.local.DashboardClientRow
import com.trigenys.cleanroute.data.local.DashboardCollectionRow
import com.trigenys.cleanroute.data.local.DashboardReceiptRow
import com.trigenys.cleanroute.data.local.DashboardZoneRow

@Dao
interface DashboardDao {
    @Query(
        """
        SELECT
            c.id AS customerId,
            c.name AS name,
            c.phone AS phone,
            COALESCE(z.name, '') AS zoneName
        FROM customers c
        LEFT JOIN zones z ON z.id = c.zoneId
        WHERE c.status = 'ACTIVE'
        ORDER BY c.name COLLATE NOCASE ASC
        """
    )
    suspend fun activeClients(): List<DashboardClientRow>

    @Query(
        """
        SELECT
            c.id AS customerId,
            c.name AS name,
            c.phone AS phone,
            COALESCE(z.name, '') AS zoneName
        FROM customers c
        LEFT JOIN zones z ON z.id = c.zoneId
        WHERE c.createdAtEpochMs >= :startEpochMs
          AND c.createdAtEpochMs < :endEpochMs
        ORDER BY c.createdAtEpochMs DESC, c.name COLLATE NOCASE ASC
        """
    )
    suspend fun newClients(
        startEpochMs: Long,
        endEpochMs: Long
    ): List<DashboardClientRow>

    @Query(
        """
        SELECT
            v.id AS visitId,
            c.id AS customerId,
            c.name AS customerName,
            COALESCE(z.name, '') AS zoneName,
            v.status AS status
        FROM collection_visits v
        INNER JOIN customers c ON c.id = v.customerId
        LEFT JOIN zones z ON z.id = c.zoneId
        WHERE v.scheduledDateIso = :dateIso
        ORDER BY z.name COLLATE NOCASE ASC, c.name COLLATE NOCASE ASC
        """
    )
    suspend fun collections(dateIso: String): List<DashboardCollectionRow>

    @Query(
        """
        SELECT
            p.id AS paymentId,
            c.id AS customerId,
            c.name AS customerName,
            p.amountXaf AS amountXaf,
            p.methodCode AS methodCode,
            p.recordedAtEpochMs AS recordedAtEpochMs
        FROM payments p
        INNER JOIN customers c ON c.id = p.customerId
        WHERE p.state = 'RECORDED'
          AND p.recordedAtEpochMs >= :startEpochMs
          AND p.recordedAtEpochMs < :endEpochMs
        ORDER BY p.recordedAtEpochMs DESC, p.id ASC
        """
    )
    suspend fun receipts(
        startEpochMs: Long,
        endEpochMs: Long
    ): List<DashboardReceiptRow>

    @Query(
        """
        SELECT
            rd.zoneId AS zoneId,
            COALESCE(z.name, '') AS zoneName,
            COUNT(v.id) AS totalStops,
            SUM(
                CASE
                    WHEN v.status IS NOT NULL AND v.status != 'SCHEDULED' THEN 1
                    ELSE 0
                END
            ) AS completedStops
        FROM route_days rd
        LEFT JOIN zones z ON z.id = rd.zoneId
        LEFT JOIN collection_visits v ON v.routeDayId = rd.id
        WHERE rd.dateIso = :dateIso
        GROUP BY rd.zoneId, z.name
        ORDER BY z.name COLLATE NOCASE ASC
        """
    )
    suspend fun zones(dateIso: String): List<DashboardZoneRow>
}
