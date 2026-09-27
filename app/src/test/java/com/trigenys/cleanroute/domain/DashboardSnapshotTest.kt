package com.trigenys.cleanroute.domain

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardSnapshotTest {
    @Test
    fun kpisComeDirectlyFromUnderlyingRecords() {
        val snapshot = OwnerDashboardSnapshot(
            date = LocalDate.of(2026, 9, 27),
            servicePeriod = YearMonth.of(2026, 9),
            collectionRecords = listOf(
                DashboardCollectionRecord(
                    visitId = CollectionVisitId("v1"),
                    customerId = CustomerId("c1"),
                    customerName = "A",
                    zoneName = "Z",
                    status = CollectionVisitStatus.COLLECTED
                ),
                DashboardCollectionRecord(
                    visitId = CollectionVisitId("v2"),
                    customerId = CustomerId("c2"),
                    customerName = "B",
                    zoneName = "Z",
                    status = CollectionVisitStatus.SCHEDULED
                )
            ),
            activeClients = emptyList(),
            receipts = listOf(
                DashboardReceiptRecord(
                    paymentId = PaymentId("p1"),
                    customerId = CustomerId("c1"),
                    customerName = "A",
                    amountXaf = 2_500,
                    method = PaymentMethod("cash"),
                    recordedAt = Instant.parse("2026-09-27T08:00:00Z")
                ),
                DashboardReceiptRecord(
                    paymentId = PaymentId("p2"),
                    customerId = CustomerId("c2"),
                    customerName = "B",
                    amountXaf = 1_500,
                    method = PaymentMethod("cash"),
                    recordedAt = Instant.parse("2026-09-27T09:00:00Z")
                )
            ),
            arrears = listOf(
                ArrearsEntry(
                    customerId = CustomerId("c2"),
                    customerName = "B",
                    phone = null,
                    zoneName = "Z",
                    monthlyFeeXaf = 5_000,
                    paidXaf = 1_500,
                    outstandingXaf = 3_500
                )
            ),
            newClients = emptyList(),
            zones = emptyList()
        )

        assertEquals(2, snapshot.collectionTotal)
        assertEquals(1, snapshot.collectionCompleted)
        assertEquals(4_000L, snapshot.receiptsXaf)
        assertEquals(3_500L, snapshot.arrearsXaf)
    }
}
