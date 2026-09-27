package com.trigenys.cleanroute.data.local

import androidx.room.Embedded
import com.trigenys.cleanroute.data.local.entity.ReferralEntity

data class ReferralSummaryRow(
    @Embedded val referral: ReferralEntity,
    val customerName: String
)

data class RetentionVisitCountsRow(
    val recordedVisits: Int,
    val completedCollections: Int
)
