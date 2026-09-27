package com.trigenys.cleanroute.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["externalId"], unique = true),
        Index(value = ["zoneId"]),
        Index(value = ["servicePlanId"])
    ]
)
data class CustomerEntity(
    @PrimaryKey val id: String,
    val externalId: String?,
    val name: String,
    val phone: String?,
    val zoneId: String,
    val addressLabel: String?,
    val servicePlanId: String,
    val status: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)
