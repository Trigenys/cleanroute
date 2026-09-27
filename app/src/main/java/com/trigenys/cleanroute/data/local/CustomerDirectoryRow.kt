package com.trigenys.cleanroute.data.local

import androidx.room.Embedded
import com.trigenys.cleanroute.data.local.entity.CustomerEntity

data class CustomerDirectoryRow(
    @Embedded val customer: CustomerEntity,
    val zoneName: String
)
