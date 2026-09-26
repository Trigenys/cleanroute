package com.trigenys.cleanroute.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.trigenys.cleanroute.data.local.entity.OutboxOperationEntity

@Dao
interface OutboxDao {
    @Query("SELECT * FROM outbox_operations ORDER BY occurredAtEpochMs ASC, id ASC")
    suspend fun getAll(): List<OutboxOperationEntity>

    @Query("SELECT COUNT(*) FROM outbox_operations")
    suspend fun count(): Int

    @Query("DELETE FROM outbox_operations WHERE id IN (:ids)")
    suspend fun deleteAcknowledged(ids: List<String>): Int
}
