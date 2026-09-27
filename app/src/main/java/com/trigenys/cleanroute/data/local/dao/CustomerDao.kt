package com.trigenys.cleanroute.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.trigenys.cleanroute.data.local.CustomerDirectoryRow
import com.trigenys.cleanroute.data.local.entity.CustomerEntity
import com.trigenys.cleanroute.data.local.entity.OutboxOperationEntity
import com.trigenys.cleanroute.data.local.entity.ServicePlanEntity
import com.trigenys.cleanroute.data.local.entity.ZoneEntity

@Dao
abstract class CustomerDao {
    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    abstract suspend fun get(id: String): CustomerEntity?

    @Query(
        """
        SELECT c.*, COALESCE(z.name, '') AS zoneName
        FROM customers c
        LEFT JOIN zones z ON z.id = c.zoneId
        WHERE :query = ''
           OR lower(c.name) LIKE '%' || lower(:query) || '%'
           OR lower(COALESCE(c.phone, '')) LIKE '%' || lower(:query) || '%'
           OR lower(COALESCE(z.name, '')) LIKE '%' || lower(:query) || '%'
        ORDER BY c.name COLLATE NOCASE ASC
        """
    )
    abstract suspend fun search(query: String): List<CustomerDirectoryRow>

    @Upsert
    protected abstract suspend fun upsertEntity(customer: CustomerEntity)

    @Upsert
    protected abstract suspend fun upsertZone(zone: ZoneEntity)

    @Upsert
    protected abstract suspend fun upsertServicePlan(servicePlan: ServicePlanEntity)

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

    @Transaction
    open suspend fun saveCustomerWithCatalog(
        zone: ZoneEntity,
        servicePlan: ServicePlanEntity,
        customer: CustomerEntity,
        operation: OutboxOperationEntity
    ) {
        upsertZone(zone)
        upsertServicePlan(servicePlan)
        upsertEntity(customer)
        insertOutbox(operation)
    }
}
