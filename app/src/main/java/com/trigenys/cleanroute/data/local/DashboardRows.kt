package com.trigenys.cleanroute.data.local

data class DashboardClientRow(
    val customerId: String,
    val name: String,
    val phone: String?,
    val zoneName: String
)

data class DashboardCollectionRow(
    val visitId: String,
    val customerId: String,
    val customerName: String,
    val zoneName: String,
    val status: String
)

data class DashboardReceiptRow(
    val paymentId: String,
    val customerId: String,
    val customerName: String,
    val amountXaf: Long,
    val methodCode: String,
    val recordedAtEpochMs: Long
)

data class DashboardZoneRow(
    val zoneId: String,
    val zoneName: String,
    val totalStops: Int,
    val completedStops: Int
)
