package com.trigenys.cleanroute.data.local

import androidx.room.Embedded
import com.trigenys.cleanroute.data.local.entity.CustomerEntity

data class CustomerExportRow(
    @Embedded val customer: CustomerEntity,
    val zoneName: String,
    val planLabel: String,
    val cadence: String,
    val monthlyFeeXaf: Long
)
