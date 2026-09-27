package com.trigenys.cleanroute.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "route_days",
    indices = [Index(value = ["dateIso", "zoneId"], unique = true)]
)
data class RouteDayEntity(
    @PrimaryKey val id: String,
    val dateIso: String,
    val zoneId: String
)

@Entity(
    tableName = "route_day_customers",
    primaryKeys = ["routeDayId", "customerId"],
    indices = [Index(value = ["customerId"])]
)
data class RouteDayCustomerEntity(
    val routeDayId: String,
    val customerId: String,
    val position: Int
)

@Entity(
    tableName = "collection_visits",
    indices = [
        Index(value = ["routeDayId"]),
        Index(value = ["customerId"]),
        Index(value = ["scheduledDateIso"])
    ]
)
data class CollectionVisitEntity(
    @PrimaryKey val id: String,
    val routeDayId: String,
    val customerId: String,
    val scheduledDateIso: String,
    val status: String,
    val statusChangedAtEpochMs: Long?,
    val revision: Int
)
