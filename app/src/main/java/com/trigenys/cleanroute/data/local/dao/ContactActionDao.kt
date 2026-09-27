package com.trigenys.cleanroute.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.trigenys.cleanroute.data.local.entity.ContactActionEntity
import com.trigenys.cleanroute.data.local.entity.OutboxOperationEntity

@Dao
abstract class ContactActionDao {
    @Query("SELECT COUNT(*) FROM contact_actions")
    abstract suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertAction(action: ContactActionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertOutbox(operation: OutboxOperationEntity): Long

    @Transaction
    open suspend fun record(
        action: ContactActionEntity,
        operation: OutboxOperationEntity
    ) {
        insertAction(action)
        insertOutbox(operation)
    }
}
