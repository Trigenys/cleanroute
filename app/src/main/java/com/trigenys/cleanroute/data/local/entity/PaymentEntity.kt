package com.trigenys.cleanroute.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["servicePeriod"])
    ]
)
data class PaymentEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val servicePeriod: String,
    val amountXaf: Long,
    val methodCode: String,
    val recordedAtEpochMs: Long,
    val state: String,
    val reversedAtEpochMs: Long?
)
