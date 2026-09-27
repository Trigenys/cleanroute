package com.trigenys.cleanroute.domain

import java.time.LocalDate

data class CustomerDirectoryEntry(
    val customer: Customer,
    val zoneName: String
)

data class CustomerProfile(
    val customer: Customer,
    val zone: Zone,
    val servicePlan: ServicePlan,
    val nextCollectionDate: LocalDate?,
    val paidThisPeriodXaf: Long,
    val outstandingThisPeriodXaf: Long,
    val recentVisits: List<CollectionVisit>,
    val recentPayments: List<Payment>
)

data class CustomerDraft(
    val customerId: CustomerId? = null,
    val name: String,
    val phone: String? = null,
    val zoneName: String,
    val addressLabel: String? = null,
    val monthlyFeeXaf: Long,
    val status: CustomerStatus = CustomerStatus.ACTIVE
) {
    init {
        require(name.isNotBlank()) { "Customer name must not be blank" }
        require(zoneName.isNotBlank()) { "Zone name must not be blank" }
        require(phone == null || phone.isNotBlank()) { "Phone must be null or non-blank" }
        require(addressLabel == null || addressLabel.isNotBlank()) {
            "Address label must be null or non-blank"
        }
        require(monthlyFeeXaf >= 0) { "Monthly fee must not be negative" }
    }
}
