package com.trigenys.cleanroute.data.local

data class ArrearsRow(
    val customerId: String,
    val customerName: String,
    val phone: String?,
    val zoneName: String,
    val monthlyFeeXaf: Long,
    val paidXaf: Long,
    val outstandingXaf: Long
)
