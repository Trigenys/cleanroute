package com.trigenys.cleanroute.sync

import com.trigenys.cleanroute.domain.SyncOperationId
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

const val SYNC_PROTOCOL_VERSION: Int = 1
const val MAX_SYNC_BATCH_SIZE: Int = 100

enum class SyncAggregateType {
    CUSTOMER,
    COLLECTION_VISIT,
    PAYMENT,
    CONTACT_ACTION,
    REFERRAL
}

enum class SyncMutationType {
    UPSERT,
    DELETE
}

sealed interface SyncPayload {
    val aggregateId: String
}

enum class CustomerSyncStatus {
    ACTIVE,
    SUSPENDED
}

data class CustomerSyncPayload(
    override val aggregateId: String,
    val externalId: String?,
    val name: String,
    val phone: String?,
    val zoneId: String,
    val addressLabel: String?,
    val servicePlanId: String,
    val status: CustomerSyncStatus,
    val createdAt: Instant,
    val updatedAt: Instant
) : SyncPayload

enum class CollectionVisitSyncStatus {
    SCHEDULED,
    COLLECTED,
    ABSENT,
    NO_WASTE
}

data class CollectionVisitSyncPayload(
    override val aggregateId: String,
    val routeDayId: String,
    val customerId: String,
    val scheduledDate: LocalDate,
    val status: CollectionVisitSyncStatus,
    val statusChangedAt: Instant?,
    val revision: Int
) : SyncPayload {
    init {
        require(revision >= 0) { "Visit revision must not be negative" }
    }
}

enum class PaymentSyncState {
    RECORDED,
    REVERSED
}

data class PaymentSyncPayload(
    override val aggregateId: String,
    val customerId: String,
    val servicePeriod: YearMonth,
    val amountXaf: Long,
    val methodCode: String,
    val state: PaymentSyncState,
    val recordedAt: Instant,
    val reversedAt: Instant?
) : SyncPayload {
    init {
        require(amountXaf > 0) { "Payment amount must be positive" }
        require(methodCode.isNotBlank()) { "Payment method must not be blank" }
        require(
            when (state) {
                PaymentSyncState.RECORDED -> reversedAt == null
                PaymentSyncState.REVERSED -> reversedAt != null
            }
        ) { "Payment state and reversal timestamp must agree" }
    }
}

enum class ContactChannelSync {
    CALL,
    WHATSAPP
}

data class ContactActionSyncPayload(
    override val aggregateId: String,
    val customerId: String,
    val channel: ContactChannelSync,
    val createdAt: Instant
) : SyncPayload

enum class ReferralRewardSyncStatus {
    PENDING,
    ELIGIBLE,
    AWARDED
}

data class ReferralSyncPayload(
    override val aggregateId: String,
    val referrerCustomerId: String,
    val referredCustomerId: String,
    val referralCode: String,
    val rewardStatus: ReferralRewardSyncStatus,
    val attributedAt: Instant,
    val qualifiedAt: Instant?,
    val awardedAt: Instant?
) : SyncPayload

data class SyncTombstone(
    val aggregateType: SyncAggregateType,
    val aggregateId: String,
    val deletedAt: Instant,
    val serverRevision: Long
) {
    init {
        require(aggregateId.isNotBlank()) { "Tombstone aggregate id must not be blank" }
        require(serverRevision >= 1) { "Server revision must be positive" }
    }
}

data class SyncMutationEnvelope(
    val protocolVersion: Int = SYNC_PROTOCOL_VERSION,
    val operationId: SyncOperationId,
    val aggregateType: SyncAggregateType,
    val aggregateId: String,
    val mutation: SyncMutationType,
    val clientRevision: String,
    val baseServerRevision: Long?,
    val occurredAt: Instant,
    val payload: SyncPayload?,
    val tombstone: SyncTombstone? = null
) {
    init {
        require(protocolVersion == SYNC_PROTOCOL_VERSION) {
            "Unsupported sync protocol version: $protocolVersion"
        }
        require(aggregateId.isNotBlank()) { "Aggregate id must not be blank" }
        require(clientRevision.isNotBlank()) { "Client revision must not be blank" }
        require(baseServerRevision == null || baseServerRevision >= 1) {
            "Base server revision must be null or positive"
        }

        when (mutation) {
            SyncMutationType.UPSERT -> {
                require(payload != null) { "UPSERT requires a payload" }
                require(tombstone == null) { "UPSERT cannot carry a tombstone" }
                require(payload.aggregateId == aggregateId) {
                    "Envelope and payload aggregate IDs must match"
                }
            }
            SyncMutationType.DELETE -> {
                require(payload == null) { "DELETE cannot carry a payload" }
                require(tombstone != null) { "DELETE requires a tombstone" }
                require(tombstone.aggregateType == aggregateType) {
                    "Envelope and tombstone aggregate types must match"
                }
                require(tombstone.aggregateId == aggregateId) {
                    "Envelope and tombstone aggregate IDs must match"
                }
            }
        }
    }
}

data class SyncPushRequest(
    val protocolVersion: Int = SYNC_PROTOCOL_VERSION,
    val workspaceId: String,
    val deviceId: String,
    val operations: List<SyncMutationEnvelope>
) {
    init {
        require(protocolVersion == SYNC_PROTOCOL_VERSION) {
            "Unsupported sync protocol version: $protocolVersion"
        }
        require(workspaceId.isNotBlank()) { "Workspace id must not be blank" }
        require(deviceId.isNotBlank()) { "Device id must not be blank" }
        require(operations.isNotEmpty()) { "Push batch must not be empty" }
        require(operations.size <= MAX_SYNC_BATCH_SIZE) {
            "Push batch exceeds $MAX_SYNC_BATCH_SIZE operations"
        }
    }
}

enum class SyncAckStatus {
    APPLIED,
    DUPLICATE,
    CONFLICT,
    REJECTED
}

data class SyncOperationAck(
    val operationId: SyncOperationId,
    val status: SyncAckStatus,
    val serverRevision: Long?,
    val reasonCode: String? = null
) {
    val safeToDeleteFromOutbox: Boolean
        get() = status == SyncAckStatus.APPLIED || status == SyncAckStatus.DUPLICATE

    init {
        require(serverRevision == null || serverRevision >= 1) {
            "Server revision must be null or positive"
        }
        require(
            when (status) {
                SyncAckStatus.APPLIED,
                SyncAckStatus.DUPLICATE -> serverRevision != null
                SyncAckStatus.CONFLICT,
                SyncAckStatus.REJECTED -> !reasonCode.isNullOrBlank()
            }
        ) { "Ack status requires a compatible revision/reason" }
    }
}

data class SyncPushResponse(
    val protocolVersion: Int = SYNC_PROTOCOL_VERSION,
    val acks: List<SyncOperationAck>
)

data class SyncPullRequest(
    val protocolVersion: Int = SYNC_PROTOCOL_VERSION,
    val workspaceId: String,
    val deviceId: String,
    val cursor: String?,
    val limit: Int = MAX_SYNC_BATCH_SIZE
) {
    init {
        require(protocolVersion == SYNC_PROTOCOL_VERSION)
        require(workspaceId.isNotBlank())
        require(deviceId.isNotBlank())
        require(limit in 1..MAX_SYNC_BATCH_SIZE)
    }
}

data class SyncRemoteChange(
    val serverRevision: Long,
    val aggregateType: SyncAggregateType,
    val aggregateId: String,
    val mutation: SyncMutationType,
    val payload: SyncPayload?,
    val tombstone: SyncTombstone?
) {
    init {
        require(serverRevision >= 1) { "Server revision must be positive" }
        require(aggregateId.isNotBlank()) { "Aggregate id must not be blank" }
        when (mutation) {
            SyncMutationType.UPSERT -> {
                require(payload != null)
                require(tombstone == null)
                require(payload.aggregateId == aggregateId)
            }
            SyncMutationType.DELETE -> {
                require(payload == null)
                require(tombstone != null)
                require(tombstone.serverRevision == serverRevision)
            }
        }
    }
}

data class SyncPullPage(
    val protocolVersion: Int = SYNC_PROTOCOL_VERSION,
    val nextCursor: String,
    val hasMore: Boolean,
    val changes: List<SyncRemoteChange>
) {
    init {
        require(protocolVersion == SYNC_PROTOCOL_VERSION)
        require(nextCursor.isNotBlank())
        require(changes.size <= MAX_SYNC_BATCH_SIZE)
    }
}

interface SyncTransport {
    suspend fun push(request: SyncPushRequest): SyncPushResponse
    suspend fun pull(request: SyncPullRequest): SyncPullPage
}
