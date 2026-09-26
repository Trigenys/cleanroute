package com.trigenys.cleanroute.data.local

import com.trigenys.cleanroute.data.local.entity.CollectionVisitEntity
import com.trigenys.cleanroute.data.local.entity.CustomerEntity
import com.trigenys.cleanroute.data.local.entity.PaymentEntity
import com.trigenys.cleanroute.domain.CollectionVisit
import com.trigenys.cleanroute.domain.CollectionVisitId
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.Customer
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.CustomerStatus
import com.trigenys.cleanroute.domain.Payment
import com.trigenys.cleanroute.domain.PaymentId
import com.trigenys.cleanroute.domain.PaymentMethod
import com.trigenys.cleanroute.domain.PaymentState
import com.trigenys.cleanroute.domain.RouteDayId
import com.trigenys.cleanroute.domain.ServicePlanId
import com.trigenys.cleanroute.domain.ZoneId
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

internal fun Customer.toEntity() = CustomerEntity(
    id = id.value,
    externalId = externalId,
    name = name,
    phone = phone,
    zoneId = zoneId.value,
    addressLabel = addressLabel,
    servicePlanId = servicePlanId.value,
    status = status.name,
    createdAtEpochMs = createdAt.toEpochMilli(),
    updatedAtEpochMs = updatedAt.toEpochMilli()
)

internal fun CustomerEntity.toDomain() = Customer(
    id = CustomerId(id),
    externalId = externalId,
    name = name,
    phone = phone,
    zoneId = ZoneId(zoneId),
    addressLabel = addressLabel,
    servicePlanId = ServicePlanId(servicePlanId),
    status = CustomerStatus.valueOf(status),
    createdAt = Instant.ofEpochMilli(createdAtEpochMs),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMs)
)

internal fun CollectionVisit.toEntity() = CollectionVisitEntity(
    id = id.value,
    routeDayId = routeDayId.value,
    customerId = customerId.value,
    scheduledDateIso = scheduledDate.toString(),
    status = status.name,
    statusChangedAtEpochMs = statusChangedAt?.toEpochMilli(),
    revision = revision
)

internal fun CollectionVisitEntity.toDomain() = CollectionVisit(
    id = CollectionVisitId(id),
    routeDayId = RouteDayId(routeDayId),
    customerId = CustomerId(customerId),
    scheduledDate = LocalDate.parse(scheduledDateIso),
    status = CollectionVisitStatus.valueOf(status),
    statusChangedAt = statusChangedAtEpochMs?.let(Instant::ofEpochMilli),
    revision = revision
)

internal fun Payment.toEntity() = PaymentEntity(
    id = id.value,
    customerId = customerId.value,
    servicePeriod = servicePeriod.toString(),
    amountXaf = amountXaf,
    methodCode = method.code,
    recordedAtEpochMs = recordedAt.toEpochMilli(),
    state = state.name,
    reversedAtEpochMs = reversedAt?.toEpochMilli()
)

internal fun PaymentEntity.toDomain() = Payment(
    id = PaymentId(id),
    customerId = CustomerId(customerId),
    servicePeriod = YearMonth.parse(servicePeriod),
    amountXaf = amountXaf,
    method = PaymentMethod(methodCode),
    recordedAt = Instant.ofEpochMilli(recordedAtEpochMs),
    state = PaymentState.valueOf(state),
    reversedAt = reversedAtEpochMs?.let(Instant::ofEpochMilli)
)
