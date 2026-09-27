package com.trigenys.cleanroute.domain

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

enum class ContactChannel {
    CALL,
    WHATSAPP
}

data class ContactAction(
    val id: ContactActionId,
    val customerId: CustomerId,
    val channel: ContactChannel,
    val createdAt: Instant
)

enum class SyncOperationKind {
    UPSERT_CUSTOMER,
    UPSERT_COLLECTION_VISIT,
    UPSERT_PAYMENT,
    RECORD_CONTACT_ACTION
}

data class SyncOperation(
    val id: SyncOperationId,
    val kind: SyncOperationKind,
    val aggregateId: String,
    val occurredAt: Instant
) {
    init {
        require(aggregateId.isNotBlank()) { "Sync aggregate id must not be blank" }
    }
}

interface CustomerRepository {
    suspend fun get(id: CustomerId): Customer?
    suspend fun search(query: String): List<CustomerDirectoryEntry>
    suspend fun getProfile(
        id: CustomerId,
        servicePeriod: YearMonth,
        today: LocalDate
    ): CustomerProfile?
    suspend fun saveDraft(
        draft: CustomerDraft,
        now: Instant
    ): CustomerId
    suspend fun upsert(customer: Customer)
}

interface CollectionVisitRepository {
    suspend fun get(id: CollectionVisitId): CollectionVisit?
    suspend fun upsert(visit: CollectionVisit)
}

interface PaymentRepository {
    suspend fun get(id: PaymentId): Payment?
    suspend fun upsert(payment: Payment)
}

interface SyncGateway {
    suspend fun push(operations: List<SyncOperation>): SyncPushResult
}

data class SyncPushResult(
    val acknowledgedOperationIds: Set<SyncOperationId>
)
