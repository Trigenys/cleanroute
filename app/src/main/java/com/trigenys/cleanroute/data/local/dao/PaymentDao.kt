package com.trigenys.cleanroute.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.trigenys.cleanroute.data.local.entity.OutboxOperationEntity
import com.trigenys.cleanroute.data.local.entity.PaymentEntity

@Dao
abstract class PaymentDao {
    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    abstract suspend fun get(id: String): PaymentEntity?

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
