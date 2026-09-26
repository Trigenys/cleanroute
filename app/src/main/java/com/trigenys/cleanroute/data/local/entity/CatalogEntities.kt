package com.trigenys.cleanroute.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zones")
data class ZoneEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(tableName = "service_plans")
data class ServicePlanEntity(
    @PrimaryKey val id: String,
    val label: String,
    val cadence: String,
    val monthlyFeeXaf: Long
)
