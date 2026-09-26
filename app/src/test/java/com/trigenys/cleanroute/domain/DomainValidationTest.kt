package com.trigenys.cleanroute.domain

import java.time.Instant
import org.junit.Test

class DomainValidationTest {
    @Test(expected = IllegalArgumentException::class)
    fun customerNameCannotBeBlank() {
        Customer(
            id = CustomerId("customer-1"),
            name = " ",
            zoneId = ZoneId("zone-1"),
            servicePlanId = ServicePlanId("plan-1"),
            createdAt = Instant.parse("2026-09-26T08:00:00Z"),
            updatedAt = Instant.parse("2026-09-26T08:00:00Z")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun routeDayCannotContainDuplicateCustomerStops() {
        RouteDay(
            id = RouteDayId("route-1"),
            date = java.time.LocalDate.of(2026, 9, 26),
            zoneId = ZoneId("zone-1"),
            customerIds = listOf(CustomerId("customer-1"), CustomerId("customer-1"))
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun servicePlanFeeCannotBeNegative() {
        ServicePlan(
            id = ServicePlanId("plan-1"),
            label = "Weekly",
            cadence = CollectionCadence.WEEKLY,
            monthlyFeeXaf = -1
        )
    }
}
