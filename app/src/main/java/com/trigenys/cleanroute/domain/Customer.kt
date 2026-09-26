package com.trigenys.cleanroute.domain

import java.time.Instant

enum class CustomerStatus {
    ACTIVE,
    SUSPENDED
}

enum class CollectionCadence {
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    CUSTOM
}

data class Zone(
    val id: ZoneId,
    val name: String
) {
    init {
        require(name.isNotBlank()) { "Zone name must not be blank" }
    }
}

data class ServicePlan(
    val id: ServicePlanId,
    val label: String,
    val cadence: CollectionCadence,
    val monthlyFeeXaf: Long
) {
    init {
        require(label.isNotBlank()) { "Service plan label must not be blank" }
        require(monthlyFeeXaf >= 0) { "Monthly fee must not be negative" }
    }
}

data class Customer(
    val id: CustomerId,
    val externalId: String? = null,
    val name: String,
    val phone: String? = null,
    val zoneId: ZoneId,
    val addressLabel: String? = null,
    val servicePlanId: ServicePlanId,
    val status: CustomerStatus = CustomerStatus.ACTIVE,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(name.isNotBlank()) { "Customer name must not be blank" }
        require(externalId == null || externalId.isNotBlank()) {
            "External id must be null or non-blank"
        }
        require(phone == null || phone.isNotBlank()) {
            "Phone must be null or non-blank"
        }
        require(!updatedAt.isBefore(createdAt)) {
            "Customer updatedAt must not precede createdAt"
        }
    }
}
