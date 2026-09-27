package com.trigenys.cleanroute.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.trigenys.cleanroute.data.local.entity.CollectionVisitEntity
import com.trigenys.cleanroute.data.local.entity.CollectionVisitRevisionEntity
import com.trigenys.cleanroute.data.local.entity.OutboxOperationEntity

@Dao
abstract class CollectionVisitDao {
    @Query("SELECT * FROM collection_visits WHERE id = :id LIMIT 1")
    abstract suspend fun get(id: String): CollectionVisitEntity?

    @Query(
        """
        SELECT * FROM collection_visit_revisions
        WHERE visitId = :visitId
        ORDER BY revision ASC
        """
    )
    abstract suspend fun revisionHistory(
        visitId: String
    ): List<CollectionVisitRevisionEntity>

    @Query("SELECT * FROM collection_visits ORDER BY scheduledDateIso ASC, id ASC")
    abstract suspend fun getAllForExport(): List<CollectionVisitEntity>

    @Query(
        """
        SELECT * FROM collection_visits
        WHERE customerId = :customerId
          AND status = 'SCHEDULED'
          AND scheduledDateIso >= :todayIso
        ORDER BY scheduledDateIso ASC
        LIMIT 1
        """
    )
    abstract suspend fun getNextScheduled(
        customerId: String,
        todayIso: String
    ): CollectionVisitEntity?

    @Query(
        """
        SELECT * FROM collection_visits
        WHERE customerId = :customerId
          AND status != 'SCHEDULED'
        ORDER BY COALESCE(statusChangedAtEpochMs, 0) DESC, scheduledDateIso DESC
        LIMIT :limit
        """
    )
    abstract suspend fun getRecentCompleted(
        customerId: String,
        limit: Int
    ): List<CollectionVisitEntity>

    @Upsert
    protected abstract suspend fun upsertEntity(visit: CollectionVisitEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertRevision(
        revision: CollectionVisitRevisionEntity
    ): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertOutbox(operation: OutboxOperationEntity): Long

    @Transaction
    open suspend fun upsertWithOutbox(
        visit: CollectionVisitEntity,
        operation: OutboxOperationEntity
    ) {
        upsertEntity(visit)
        insertOutbox(operation)
    }

    @Transaction
    open suspend fun recordOutcomeWithAudit(
        visit: CollectionVisitEntity,
        revision: CollectionVisitRevisionEntity,
        operation: OutboxOperationEntity
    ) {
        upsertEntity(visit)
        insertRevision(revision)
        insertOutbox(operation)
    }
}
