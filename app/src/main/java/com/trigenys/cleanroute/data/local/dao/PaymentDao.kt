package com.trigenys.cleanroute.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.trigenys.cleanroute.data.local.ArrearsRow
import com.trigenys.cleanroute.data.local.PeriodSummaryRow
import com.trigenys.cleanroute.data.local.entity.OutboxOperationEntity
import com.trigenys.cleanroute.data.local.entity.PaymentEntity

@Dao
abstract class PaymentDao {
    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    abstract suspend fun get(id: String): PaymentEntity?

    @Query("SELECT * FROM payments ORDER BY recordedAtEpochMs ASC, id ASC")
    abstract suspend fun getAllForExport(): List<PaymentEntity>

    @Query(
        """
        SELECT * FROM payments
        WHERE customerId = :customerId
          AND servicePeriod = :servicePeriod
        ORDER BY recordedAtEpochMs DESC
        """
    )
    abstract suspend fun getForPeriod(
        customerId: String,
        servicePeriod: String
    ): List<PaymentEntity>

    @Query(
        """
        SELECT * FROM payments
        WHERE customerId = :customerId
        ORDER BY recordedAtEpochMs DESC
        LIMIT :limit
        """
    )
    abstract suspend fun getRecent(
        customerId: String,
        limit: Int
    ): List<PaymentEntity>

    @Query(
        """
        SELECT
            c.id AS customerId,
            c.name AS customerName,
            c.phone AS phone,
            COALESCE(z.name, '') AS zoneName,
            COALESCE(sp.monthlyFeeXaf, 0) AS monthlyFeeXaf,
            COALESCE(
                SUM(
                    CASE
                        WHEN p.state = 'RECORDED'
                         AND p.servicePeriod = :servicePeriod
                        THEN p.amountXaf
                        ELSE 0
                    END
                ),
                0
            ) AS paidXaf,
            CASE
                WHEN COALESCE(sp.monthlyFeeXaf, 0) -
                     COALESCE(
                         SUM(
                             CASE
                                 WHEN p.state = 'RECORDED'
                                  AND p.servicePeriod = :servicePeriod
                                 THEN p.amountXaf
                                 ELSE 0
                             END
                         ),
                         0
                     ) > 0
                THEN COALESCE(sp.monthlyFeeXaf, 0) -
                     COALESCE(
                         SUM(
                             CASE
                                 WHEN p.state = 'RECORDED'
                                  AND p.servicePeriod = :servicePeriod
                                 THEN p.amountXaf
                                 ELSE 0
                             END
                         ),
                         0
                     )
                ELSE 0
            END AS outstandingXaf
        FROM customers c
        LEFT JOIN zones z ON z.id = c.zoneId
        LEFT JOIN service_plans sp ON sp.id = c.servicePlanId
        LEFT JOIN payments p ON p.customerId = c.id
        WHERE c.status = 'ACTIVE'
          AND (
              :query = ''
              OR lower(c.name) LIKE '%' || lower(:query) || '%'
              OR lower(COALESCE(c.phone, '')) LIKE '%' || lower(:query) || '%'
              OR lower(COALESCE(z.name, '')) LIKE '%' || lower(:query) || '%'
          )
        GROUP BY c.id, c.name, c.phone, z.name, sp.monthlyFeeXaf
        HAVING outstandingXaf > 0
        ORDER BY outstandingXaf DESC, c.name COLLATE NOCASE ASC
        """
    )
    abstract suspend fun arrears(
        servicePeriod: String,
        query: String
    ): List<ArrearsRow>

    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM customers WHERE status = 'ACTIVE') AS activeClients,
            COALESCE(
                (SELECT SUM(amountXaf) FROM payments
                 WHERE state = 'RECORDED' AND servicePeriod = :servicePeriod),
                0
            ) AS collectedXaf
        """
    )
    abstract suspend fun periodSummary(servicePeriod: String): PeriodSummaryRow

    @Upsert
    protected abstract suspend fun upsertEntity(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertOutbox(operation: OutboxOperationEntity): Long

    @Transaction
    open suspend fun upsertWithOutbox(
        payment: PaymentEntity,
        operation: OutboxOperationEntity
    ) {
        upsertEntity(payment)
        insertOutbox(operation)
    }
}
