package com.trigenys.cleanroute.domain

private fun requireIdentifier(value: String, label: String): String {
    val normalized = value.trim()
    require(normalized.isNotEmpty()) { "$label must not be blank" }
    return normalized
}

data class CustomerId(val value: String) {
    init { requireIdentifier(value, "CustomerId") }
}

data class ZoneId(val value: String) {
    init { requireIdentifier(value, "ZoneId") }
}

data class ServicePlanId(val value: String) {
    init { requireIdentifier(value, "ServicePlanId") }
}

data class RouteDayId(val value: String) {
    init { requireIdentifier(value, "RouteDayId") }
}

data class CollectionVisitId(val value: String) {
    init { requireIdentifier(value, "CollectionVisitId") }
}

data class PaymentId(val value: String) {
    init { requireIdentifier(value, "PaymentId") }
}

data class ContactActionId(val value: String) {
    init { requireIdentifier(value, "ContactActionId") }
}

data class ReferralId(val value: String) {
    init { requireIdentifier(value, "ReferralId") }
}

data class SyncOperationId(val value: String) {
    init { requireIdentifier(value, "SyncOperationId") }
}
