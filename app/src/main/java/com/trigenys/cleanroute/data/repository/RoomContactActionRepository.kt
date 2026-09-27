package com.trigenys.cleanroute.data.repository

import com.trigenys.cleanroute.data.local.CleanRouteDatabase
import com.trigenys.cleanroute.data.local.OutboxOperationFactory
import com.trigenys.cleanroute.data.local.entity.ContactActionEntity
import com.trigenys.cleanroute.domain.ContactAction
import com.trigenys.cleanroute.domain.ContactActionId
import com.trigenys.cleanroute.domain.ContactActionRepository
import com.trigenys.cleanroute.domain.ContactChannel
import com.trigenys.cleanroute.domain.CustomerId
import java.time.Instant
import java.util.UUID

class RoomContactActionRepository(
    private val database: CleanRouteDatabase
) : ContactActionRepository {
    override suspend fun record(
        customerId: CustomerId,
        channel: ContactChannel,
        at: Instant
    ): ContactAction {
        val action = ContactAction(
            id = ContactActionId(UUID.randomUUID().toString()),
            customerId = customerId,
            channel = channel,
            createdAt = at
        )

        database.contactActionDao().record(
            action = ContactActionEntity(
                id = action.id.value,
                customerId = action.customerId.value,
                channel = action.channel.name,
                createdAtEpochMs = action.createdAt.toEpochMilli()
            ),
            operation = OutboxOperationFactory.contactAction(action)
        )
        return action
    }
}
