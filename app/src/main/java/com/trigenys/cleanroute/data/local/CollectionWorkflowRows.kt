package com.trigenys.cleanroute.data.local

import androidx.room.Embedded
import com.trigenys.cleanroute.data.local.entity.CollectionVisitEntity

data class ZoneWorkloadRow(
    val zoneId: String,
    val zoneName: String,
    val totalStops: Int,
    val completedStops: Int
)

data class DailyCollectionStopRow(
    @Embedded val visit: CollectionVisitEntity,
    val customerName: String,
    val phone: String?,
    val addressLabel: String?
)
