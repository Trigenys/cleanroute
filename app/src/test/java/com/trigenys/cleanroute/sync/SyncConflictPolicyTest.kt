package com.trigenys.cleanroute.sync

import com.trigenys.cleanroute.domain.SyncOperationId
import java.time.Instant
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncConflictPolicyTest {
    @Test
    fun customerRequiresMatchingBaseRevisionInsteadOfSilentLastWriteWins() {
        assertEquals(
            ConflictDecision.APPLY,
            SyncConflictPolicy.customer(
                baseServerRevision = 12,
                currentServerRevision = 12,
                samePayload = false
            )
        )
        assertEquals(
            ConflictDecision.CONFLICT,
            SyncConflictPolicy.customer(
                baseServerRevision = 11,
                currentServerRevision = 12,
                samePayload = false
            )
        )
        assertEquals(
            ConflictDecision.DUPLICATE,
            SyncConflictPolicy.customer(
                baseServerRevision = 11,
                currentServerRevision = 12,
                samePayload = true
            )
        )
    }

    @Test
    fun collectionVisitUsesBusinessRevision() {
        assertEquals(
            ConflictDecision.APPLY,
            SyncConflictPolicy.collectionVisit(3, 2, samePayload = false)
        )
        assertEquals(
            ConflictDecision.STALE,
            SyncConflictPolicy.collectionVisit(1, 2, samePayload = false)
        )
        assertEquals(
            ConflictDecision.DUPLICATE,
            SyncConflictPolicy.collectionVisit(2, 2, samePayload = true)
        )
        assertEquals(
            ConflictDecision.CONFLICT,
            SyncConflictPolicy.collectionVisit(2, 2, samePayload = false)
        )
    }

    @Test
    fun paymentCanOnlyMoveForwardToReversed() {
        val recorded = payment(PaymentSyncState.RECORDED, null)
        val reversed = payment(
            PaymentSyncState.REVERSED,
            Instant.parse("2026-09-27T12:00:00Z")
        )

        assertEquals(
            ConflictDecision.APPLY,
            SyncConflictPolicy.payment(reversed, recorded)
        )
        assertEquals(
            ConflictDecision.STALE,
            SyncConflictPolicy.payment(recorded, reversed)
        )
        assertEquals(
            ConflictDecision.DUPLICATE,
            SyncConflictPolicy.payment(recorded, recorded)
        )

        assertEquals(
            ConflictDecision.CONFLICT,
            SyncConflictPolicy.payment(
                recorded.copy(amountXaf = 9_999),
                recorded
            )
        )
    }

    @Test
    fun operationReplayReturnsSameAckButRejectsChangedPayload() {
        val operationId = SyncOperationId("op-1")
        val ack = SyncOperationAck(
            operationId = operationId,
            status = SyncAckStatus.APPLIED,
            serverRevision = 42
        )
        val receipt = SyncOperationReceipt(
            operationId = operationId,
            requestFingerprint = "sha256:a",
            ack = ack
        )

        val replay = SyncIdempotencyPolicy.replay(
            receipt,
            operationId,
            "sha256:a"
        )
        assertTrue(replay is SyncReplayDecision.ReturnRecordedAck)
        assertEquals(
            ack,
            (replay as SyncReplayDecision.ReturnRecordedAck).ack
        )

        assertEquals(
            SyncReplayDecision.RejectOperationIdReuse,
            SyncIdempotencyPolicy.replay(
                receipt,
                operationId,
                "sha256:b"
            )
        )
    }

    @Test
    fun onlyAppliedOrDuplicateAckCanDeleteOutboxEntry() {
        val id = SyncOperationId("op-2")
        assertTrue(
            SyncOperationAck(
                id,
                SyncAckStatus.APPLIED,
                serverRevision = 1
            ).safeToDeleteFromOutbox
        )
        assertTrue(
            SyncOperationAck(
                id,
                SyncAckStatus.DUPLICATE,
                serverRevision = 1
            ).safeToDeleteFromOutbox
        )
        assertFalse(
            SyncOperationAck(
                id,
                SyncAckStatus.CONFLICT,
                serverRevision = null,
                reasonCode = "stale_customer"
            ).safeToDeleteFromOutbox
        )
        assertFalse(
            SyncOperationAck(
                id,
                SyncAckStatus.REJECTED,
                serverRevision = null,
                reasonCode = "operation_id_reused"
            ).safeToDeleteFromOutbox
        )
    }

    @Test
    fun clientDeletesAreDisabledInProtocolV1ButServerTombstonesAreSupported() {
        SyncAggregateType.entries.forEach { type ->
            assertFalse(SyncDeletionPolicy.clientDeleteAllowed(type))
            assertTrue(SyncDeletionPolicy.serverTombstoneSupported(type))
        }
    }

    private fun payment(
        state: PaymentSyncState,
        reversedAt: Instant?
    ) = PaymentSyncPayload(
        aggregateId = "payment-1",
        customerId = "customer-1",
        servicePeriod = YearMonth.of(2026, 9),
        amountXaf = 5_000,
        methodCode = "mtn_momo",
        state = state,
        recordedAt = Instant.parse("2026-09-27T10:00:00Z"),
        reversedAt = reversedAt
    )
}
