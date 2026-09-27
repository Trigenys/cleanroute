package com.trigenys.cleanroute.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.trigenys.cleanroute.data.local.entity.ServicePlanEntity
import com.trigenys.cleanroute.data.local.entity.ZoneEntity

@Dao
interface CatalogDao {
    @Query("SELECT * FROM zones WHERE id = :id LIMIT 1")
    suspend fun getZone(id: String): ZoneEntity?

    @Query("SELECT * FROM service_plans WHERE id = :id LIMIT 1")
    suspend fun getServicePlan(id: String): ServicePlanEntity?

    @Query("SELECT * FROM zones")
    suspend fun getAllZones(): List<ZoneEntity>

    @Query("SELECT * FROM service_plans")
    suspend fun getAllServicePlans(): List<ServicePlanEntity>

    @Upsert
    suspend fun upsertZone(zone: ZoneEntity)

    @Upsert
    suspend fun upsertServicePlan(servicePlan: ServicePlanEntity)
}
