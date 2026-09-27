package com.trigenys.cleanroute.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contact_actions",
    indices = [Index(value = ["customerId"])]
)
data class ContactActionEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val channel: String,
    val createdAtEpochMs: Long
)

@Entity(
    tableName = "outbox_operations",
    indices = [
        Index(value = ["aggregateId"]),
        Index(value = ["occurredAtEpochMs"])
    ]
)
data class OutboxOperationEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val aggregateId: String,
    val occurredAtEpochMs: Long
)
