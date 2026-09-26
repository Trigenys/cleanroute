package com.trigenys.cleanroute.data.repository

import com.trigenys.cleanroute.data.local.CleanRouteDatabase
import com.trigenys.cleanroute.data.local.OutboxOperationFactory
import com.trigenys.cleanroute.data.local.toDomain
import com.trigenys.cleanroute.data.local.toEntity
import com.trigenys.cleanroute.domain.CollectionVisit
import com.trigenys.cleanroute.domain.CollectionVisitId
import com.trigenys.cleanroute.domain.CollectionVisitRepository
import com.trigenys.cleanroute.domain.Customer
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.CustomerRepository
import com.trigenys.cleanroute.domain.Payment
import com.trigenys.cleanroute.domain.PaymentId
import com.trigenys.cleanroute.domain.PaymentRepository

class RoomCustomerRepository(
    private val database: CleanRouteDatabase
) : CustomerRepository {
    override suspend fun get(id: CustomerId): Customer? =
        database.customerDao().get(id.value)?.toDomain()

    override suspend fun upsert(customer: Customer) {
        database.customerDao().upsertWithOutbox(
            customer = customer.toEntity(),
            operation = OutboxOperationFactory.customer(customer)
        )
    }
}

class RoomCollectionVisitRepository(
    private val database: CleanRouteDatabase
) : CollectionVisitRepository {
    override suspend fun get(id: CollectionVisitId): CollectionVisit? =
        database.collectionVisitDao().get(id.value)?.toDomain()

    override suspend fun upsert(visit: CollectionVisit) {
        database.collectionVisitDao().upsertWithOutbox(
            visit = visit.toEntity(),
            operation = OutboxOperationFactory.collectionVisit(visit)
        )
    }
}

class RoomPaymentRepository(
    private val database: CleanRouteDatabase
) : PaymentRepository {
    override suspend fun get(id: PaymentId): Payment? =
        database.paymentDao().get(id.value)?.toDomain()

    override suspend fun upsert(payment: Payment) {
        database.paymentDao().upsertWithOutbox(
            payment = payment.toEntity(),
            operation = OutboxOperationFactory.payment(payment)
        )
    }
}
