package com.trigenys.cleanroute.data.local

import com.trigenys.cleanroute.data.local.entity.OutboxOperationEntity
import com.trigenys.cleanroute.domain.CollectionVisit
import com.trigenys.cleanroute.domain.ContactAction
import com.trigenys.cleanroute.domain.Customer
import com.trigenys.cleanroute.domain.Payment
import com.trigenys.cleanroute.domain.Referral
import com.trigenys.cleanroute.domain.SyncOperationKind
import java.time.ZoneOffset

internal object OutboxOperationFactory {
    fun customer(customer: Customer): OutboxOperationEntity =
        OutboxOperationEntity(
            id = stableId(
                kind = SyncOperationKind.UPSERT_CUSTOMER,
                aggregateId = customer.id.value,
                revision = customer.updatedAt.toEpochMilli().toString()
            ),
            kind = SyncOperationKind.UPSERT_CUSTOMER.name,
            aggregateId = customer.id.value,
            occurredAtEpochMs = customer.updatedAt.toEpochMilli()
        )

    fun collectionVisit(visit: CollectionVisit): OutboxOperationEntity =
        OutboxOperationEntity(
            id = stableId(
                kind = SyncOperationKind.UPSERT_COLLECTION_VISIT,
                aggregateId = visit.id.value,
                revision = visit.revision.toString()
            ),
            kind = SyncOperationKind.UPSERT_COLLECTION_VISIT.name,
            aggregateId = visit.id.value,
            occurredAtEpochMs = visit.statusChangedAt?.toEpochMilli()
                ?: visit.scheduledDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )

    fun contactAction(action: ContactAction): OutboxOperationEntity =
        OutboxOperationEntity(
            id = stableId(
                kind = SyncOperationKind.RECORD_CONTACT_ACTION,
                aggregateId = action.id.value,
                revision = action.createdAt.toEpochMilli().toString()
            ),
            kind = SyncOperationKind.RECORD_CONTACT_ACTION.name,
            aggregateId = action.id.value,
            occurredAtEpochMs = action.createdAt.toEpochMilli()
        )

    fun referral(referral: Referral): OutboxOperationEntity {
        val revisionTimestamp = referral.awardedAt
            ?: referral.qualifiedAt
            ?: referral.attributedAt
        return OutboxOperationEntity(
            id = stableId(
                kind = SyncOperationKind.UPSERT_REFERRAL,
                aggregateId = referral.id.value,
                revision = "${referral.rewardStatus.name}:${revisionTimestamp.toEpochMilli()}"
            ),
            kind = SyncOperationKind.UPSERT_REFERRAL.name,
            aggregateId = referral.id.value,
            occurredAtEpochMs = revisionTimestamp.toEpochMilli()
        )
    }

    fun payment(payment: Payment): OutboxOperationEntity {
        val revisionTimestamp = payment.reversedAt ?: payment.recordedAt
        return OutboxOperationEntity(
            id = stableId(
                kind = SyncOperationKind.UPSERT_PAYMENT,
                aggregateId = payment.id.value,
                revision = "${payment.state.name}:${revisionTimestamp.toEpochMilli()}"
            ),
            kind = SyncOperationKind.UPSERT_PAYMENT.name,
            aggregateId = payment.id.value,
            occurredAtEpochMs = revisionTimestamp.toEpochMilli()
        )
    }

    private fun stableId(
        kind: SyncOperationKind,
        aggregateId: String,
        revision: String
    ): String = "${kind.name.lowercase()}:$aggregateId:$revision"
}
