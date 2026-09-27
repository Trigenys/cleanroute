package com.trigenys.cleanroute.domain

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

enum class DashboardMetric {
    COLLECTION,
    ACTIVE_CLIENTS,
    RECEIPTS,
    ARREARS,
    NEW_CLIENTS,
    ZONES
}

data class DashboardClientRecord(
    val customerId: CustomerId,
    val name: String,
    val phone: String?,
    val zoneName: String
)

data class DashboardCollectionRecord(
    val visitId: CollectionVisitId,
    val customerId: CustomerId,
    val customerName: String,
    val zoneName: String,
    val status: CollectionVisitStatus
)

data class DashboardReceiptRecord(
    val paymentId: PaymentId,
    val customerId: CustomerId,
    val customerName: String,
    val amountXaf: Long,
    val method: PaymentMethod,
    val recordedAt: Instant
)

data class DashboardZoneRecord(
    val zoneId: ZoneId,
    val zoneName: String,
    val totalStops: Int,
    val completedStops: Int
) {
    val remainingStops: Int
        get() = (totalStops - completedStops).coerceAtLeast(0)
}

data class OwnerDashboardSnapshot(
    val date: LocalDate,
    val servicePeriod: YearMonth,
    val collectionRecords: List<DashboardCollectionRecord>,
    val activeClients: List<DashboardClientRecord>,
    val receipts: List<DashboardReceiptRecord>,
    val arrears: List<ArrearsEntry>,
    val newClients: List<DashboardClientRecord>,
    val zones: List<DashboardZoneRecord>
) {
    val collectionTotal: Int
        get() = collectionRecords.size

    val collectionCompleted: Int
        get() = collectionRecords.count { it.status != CollectionVisitStatus.SCHEDULED }

    val receiptsXaf: Long
        get() = receipts.sumOf { it.amountXaf }

    val arrearsXaf: Long
        get() = arrears.sumOf { it.outstandingXaf }
}

interface DashboardRepository {
    suspend fun snapshot(
        date: LocalDate,
        timeZone: java.time.ZoneId
    ): OwnerDashboardSnapshot
}
