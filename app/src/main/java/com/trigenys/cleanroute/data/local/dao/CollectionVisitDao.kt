package com.trigenys.cleanroute.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.trigenys.cleanroute.data.local.entity.CollectionVisitEntity
import com.trigenys.cleanroute.data.local.entity.OutboxOperationEntity

@Dao
abstract class CollectionVisitDao {
    @Query("SELECT * FROM collection_visits WHERE id = :id LIMIT 1")
    abstract suspend fun get(id: String): CollectionVisitEntity?

    @Upsert
    protected abstract suspend fun upsertEntity(visit: CollectionVisitEntity)

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
}
