package com.trigenys.cleanroute.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.trigenys.cleanroute.data.local.entity.CustomerEntity
import com.trigenys.cleanroute.data.local.entity.OutboxOperationEntity

@Dao
abstract class CustomerDao {
    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    abstract suspend fun get(id: String): CustomerEntity?

    @Upsert
    protected abstract suspend fun upsertEntity(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertOutbox(operation: OutboxOperationEntity): Long

    @Transaction
    open suspend fun upsertWithOutbox(
        customer: CustomerEntity,
        operation: OutboxOperationEntity
    ) {
        upsertEntity(customer)
        insertOutbox(operation)
    }
}
