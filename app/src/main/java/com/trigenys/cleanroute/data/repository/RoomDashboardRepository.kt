package com.trigenys.cleanroute.data.repository

import com.trigenys.cleanroute.data.local.CleanRouteDatabase
import com.trigenys.cleanroute.domain.ArrearsEntry
import com.trigenys.cleanroute.domain.CollectionVisitId
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.DashboardClientRecord
import com.trigenys.cleanroute.domain.DashboardCollectionRecord
import com.trigenys.cleanroute.domain.DashboardReceiptRecord
import com.trigenys.cleanroute.domain.DashboardRepository
import com.trigenys.cleanroute.domain.DashboardZoneRecord
import com.trigenys.cleanroute.domain.OwnerDashboardSnapshot
import com.trigenys.cleanroute.domain.PaymentId
import com.trigenys.cleanroute.domain.PaymentMethod
import com.trigenys.cleanroute.domain.ZoneId
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

class RoomDashboardRepository(
    private val database: CleanRouteDatabase
) : DashboardRepository {
    override suspend fun snapshot(
        date: LocalDate,
        timeZone: java.time.ZoneId
    ): OwnerDashboardSnapshot {
        val start = date.atStartOfDay(timeZone).toInstant()
        val end = date.plusDays(1).atStartOfDay(timeZone).toInstant()
        val period = YearMonth.from(date)
        val dashboard = database.dashboardDao()

        val activeClients = dashboard.activeClients().map { row ->
            DashboardClientRecord(
                customerId = CustomerId(row.customerId),
                name = row.name,
                phone = row.phone,
                zoneName = row.zoneName
            )
        }

        val newClients = dashboard
            .newClients(start.toEpochMilli(), end.toEpochMilli())
            .map { row ->
                DashboardClientRecord(
                    customerId = CustomerId(row.customerId),
                    name = row.name,
                    phone = row.phone,
                    zoneName = row.zoneName
                )
            }

        val collectionRecords = dashboard.collections(date.toString()).map { row ->
            DashboardCollectionRecord(
                visitId = CollectionVisitId(row.visitId),
                customerId = CustomerId(row.customerId),
                customerName = row.customerName,
                zoneName = row.zoneName,
                status = CollectionVisitStatus.valueOf(row.status)
            )
        }

        val receipts = dashboard
            .receipts(start.toEpochMilli(), end.toEpochMilli())
            .map { row ->
                DashboardReceiptRecord(
                    paymentId = PaymentId(row.paymentId),
                    customerId = CustomerId(row.customerId),
                    customerName = row.customerName,
                    amountXaf = row.amountXaf,
                    method = PaymentMethod(row.methodCode),
                    recordedAt = Instant.ofEpochMilli(row.recordedAtEpochMs)
                )
            }

        val arrears = database.paymentDao()
            .arrears(servicePeriod = period.toString(), query = "")
            .map { row ->
                ArrearsEntry(
                    customerId = CustomerId(row.customerId),
                    customerName = row.customerName,
                    phone = row.phone,
                    zoneName = row.zoneName,
                    monthlyFeeXaf = row.monthlyFeeXaf,
                    paidXaf = row.paidXaf,
                    outstandingXaf = row.outstandingXaf
                )
            }

        val zones = dashboard.zones(date.toString()).map { row ->
            DashboardZoneRecord(
                zoneId = ZoneId(row.zoneId),
                zoneName = row.zoneName,
                totalStops = row.totalStops,
                completedStops = row.completedStops
            )
        }

        return OwnerDashboardSnapshot(
            date = date,
            servicePeriod = period,
            collectionRecords = collectionRecords,
            activeClients = activeClients,
            receipts = receipts,
            arrears = arrears,
            newClients = newClients,
            zones = zones
        )
    }
}
