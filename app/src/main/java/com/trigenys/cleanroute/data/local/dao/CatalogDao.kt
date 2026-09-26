package com.trigenys.cleanroute.data.local.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.trigenys.cleanroute.data.local.entity.ServicePlanEntity
import com.trigenys.cleanroute.data.local.entity.ZoneEntity

@Dao
interface CatalogDao {
    @Upsert
    suspend fun upsertZone(zone: ZoneEntity)

    @Upsert
    suspend fun upsertServicePlan(servicePlan: ServicePlanEntity)
}
