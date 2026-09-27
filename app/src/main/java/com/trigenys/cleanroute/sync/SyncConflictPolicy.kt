package com.trigenys.cleanroute.sync

import com.trigenys.cleanroute.domain.SyncOperationId

enum class ConflictDecision {
    APPLY,
    DUPLICATE,
    CONFLICT,
    STALE
}

object SyncConflictPolicy {
    fun customer(
        baseServerRevision: Long?,
        currentServerRevision: Long?,
        samePayload: Boolean
    ): ConflictDecision {
        if (samePayload) return ConflictDecision.DUPLICATE
        if (currentServerRevision == null) return ConflictDecision.APPLY
        return if (baseServerRevision == currentServerRevision) {
            ConflictDecision.APPLY
        } else {
            ConflictDecision.CONFLICT
        }
    }

    fun collectionVisit(
        incomingRevision: Int,
        currentRevision: Int?,
        samePayload: Boolean
    ): ConflictDecision {
        require(incomingRevision >= 0)
        require(currentRevision == null || currentRevision >= 0)

        if (currentRevision == null) return ConflictDecision.APPLY
        return when {
            incomingRevision > currentRevision -> ConflictDecision.APPLY
            incomingRevision < currentRevision -> ConflictDecision.STALE
            samePayload -> ConflictDecision.DUPLICATE
            else -> ConflictDecision.CONFLICT
        }
    }

    fun payment(
        incoming: PaymentSyncPayload,
        current: PaymentSyncPayload?
    ): ConflictDecision {
        if (current == null) return ConflictDecision.APPLY
        if (!sameImmutablePayment(incoming, current)) return ConflictDecision.CONFLICT
        if (incoming == current) return ConflictDecision.DUPLICATE

        return when {
            current.state == PaymentSyncState.RECORDED &&
                incoming.state == PaymentSyncState.REVERSED ->
                ConflictDecision.APPLY

            current.state == PaymentSyncState.REVERSED &&
                incoming.state == PaymentSyncState.RECORDED ->
                ConflictDecision.STALE

            else -> ConflictDecision.CONFLICT
        }
    }

    private fun sameImmutablePayment(
        left: PaymentSyncPayload,
        right: PaymentSyncPayload
    ): Boolean =
        left.aggregateId == right.aggregateId &&
            left.customerId == right.customerId &&
            left.servicePeriod == right.servicePeriod &&
            left.amountXaf == right.amountXaf &&
            left.methodCode == right.methodCode &&
            left.recordedAt == right.recordedAt
}

data class SyncOperationReceipt(
    val operationId: SyncOperationId,
    val requestFingerprint: String,
    val ack: SyncOperationAck
) {
    init {
        require(requestFingerprint.isNotBlank()) {
            "Request fingerprint must not be blank"
        }
    }
}

sealed interface SyncReplayDecision {
    data object NewOperation : SyncReplayDecision
    data class ReturnRecordedAck(
        val ack: SyncOperationAck
    ) : SyncReplayDecision
    data object RejectOperationIdReuse : SyncReplayDecision
}

object SyncIdempotencyPolicy {
    fun replay(
        receipt: SyncOperationReceipt?,
        operationId: SyncOperationId,
        requestFingerprint: String
    ): SyncReplayDecision {
        require(requestFingerprint.isNotBlank())

        if (receipt == null) return SyncReplayDecision.NewOperation
        if (receipt.operationId != operationId) return SyncReplayDecision.NewOperation

        return if (receipt.requestFingerprint == requestFingerprint) {
            SyncReplayDecision.ReturnRecordedAck(receipt.ack)
        } else {
            SyncReplayDecision.RejectOperationIdReuse
        }
    }
}

object SyncDeletionPolicy {
    fun clientDeleteAllowed(aggregateType: SyncAggregateType): Boolean = false

    fun serverTombstoneSupported(aggregateType: SyncAggregateType): Boolean =
        when (aggregateType) {
            SyncAggregateType.CUSTOMER,
            SyncAggregateType.COLLECTION_VISIT,
            SyncAggregateType.PAYMENT,
            SyncAggregateType.CONTACT_ACTION,
            SyncAggregateType.REFERRAL -> true
        }
}
