package com.trigenys.cleanroute.data.repository

import com.trigenys.cleanroute.data.local.CleanRouteDatabase
import com.trigenys.cleanroute.data.local.OutboxOperationFactory
import com.trigenys.cleanroute.data.local.entity.ReferralEntity
import com.trigenys.cleanroute.data.local.entity.ReferralProfileEntity
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.Referral
import com.trigenys.cleanroute.domain.ReferralCodeFactory
import com.trigenys.cleanroute.domain.ReferralId
import com.trigenys.cleanroute.domain.ReferralRewardPolicy
import com.trigenys.cleanroute.domain.ReferralRewardStatus
import com.trigenys.cleanroute.domain.ReferralSummary
import com.trigenys.cleanroute.domain.RetentionCustomerProfile
import com.trigenys.cleanroute.domain.RetentionIndicators
import com.trigenys.cleanroute.domain.RetentionProgramConfig
import com.trigenys.cleanroute.domain.RetentionRepository
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

class RoomRetentionRepository(
    private val database: CleanRouteDatabase,
    private val config: RetentionProgramConfig
) : RetentionRepository {
    private val policy = ReferralRewardPolicy(config.rewardRule)
    private val codeFactory = ReferralCodeFactory(config)

    init {
        require(config.enabled) {
            "RoomRetentionRepository must not be created when retention is disabled"
        }
    }

    override suspend fun getProfile(
        customerId: CustomerId,
        today: LocalDate,
        timeZone: ZoneId,
        at: Instant
    ): RetentionCustomerProfile {
        val customer = database.customerDao().get(customerId.value)
            ?: error("Client introuvable.")
        val dao = database.retentionDao()
        val referralProfile = dao.ensureProfile(
            ReferralProfileEntity(
                customerId = customerId.value,
                code = codeFactory.codeFor(customerId),
                createdAtEpochMs = at.toEpochMilli()
            )
        )

        val inboundSummary = dao.getInboundSummary(customerId.value)?.let { row ->
            ReferralSummary(
                referral = refreshReferral(row.referral.toDomain(), at),
                customerName = row.customerName
            )
        }

        val outbound = dao.getOutbound(customerId.value).map { row ->
            ReferralSummary(
                referral = refreshReferral(row.referral.toDomain(), at),
                customerName = row.customerName
            )
        }

        val since = Instant.ofEpochMilli(customer.createdAtEpochMs)
            .atZone(timeZone)
            .toLocalDate()
        val fromDate = today.minusDays(89)
        val counts = dao.visitCounts(
            customerId = customerId.value,
            fromDateIso = fromDate.toString(),
            toDateIso = today.toString()
        )

        return RetentionCustomerProfile(
            customerId = customerId,
            referralCode = referralProfile.code,
            referralLink = codeFactory.linkFor(referralProfile.code),
            rewardLabel = config.rewardRule.rewardLabel,
            indicators = RetentionIndicators(
                customerSince = since,
                tenureDays = ChronoUnit.DAYS.between(since, today).coerceAtLeast(0),
                completedCollectionsLast90Days = counts.completedCollections,
                recordedVisitsLast90Days = counts.recordedVisits
            ),
            referredBy = inboundSummary,
            referralsMade = outbound
        )
    }

    override suspend fun attribute(
        referredCustomerId: CustomerId,
        referralCode: String,
        at: Instant
    ): Referral {
        val normalizedCode = referralCode.trim().uppercase(Locale.ROOT)
        require(normalizedCode.isNotBlank()) { "Le code de parrainage est obligatoire." }

        val dao = database.retentionDao()
        val source = dao.getProfileByCode(normalizedCode)
            ?: error("Code de parrainage introuvable.")

        val referrerId = CustomerId(source.customerId)
        require(referrerId != referredCustomerId) {
            "Un client ne peut pas se parrainer lui-même."
        }
        require(database.customerDao().get(referredCustomerId.value) != null) {
            "Client filleul introuvable."
        }

        val existing = dao.getInbound(referredCustomerId.value)?.toDomain()
        if (existing != null) {
            require(existing.referrerCustomerId == referrerId) {
                "Ce client est déjà attribué à un autre parrain."
            }
            return refreshReferral(existing, at)
        }

        val referral = Referral(
            id = ReferralId(stableReferralId(referredCustomerId)),
            referrerCustomerId = referrerId,
            referredCustomerId = referredCustomerId,
            referralCode = source.code,
            rewardStatus = ReferralRewardStatus.PENDING,
            attributedAt = at
        )

        val inserted = dao.insertReferralWithOutbox(
            referral = referral.toEntity(),
            operation = OutboxOperationFactory.referral(referral)
        )

        val effective = if (inserted == -1L) {
            val concurrent = dao.getInbound(referredCustomerId.value)?.toDomain()
                ?: error("Impossible d’attribuer ce parrainage.")
            require(concurrent.referrerCustomerId == referrerId) {
                "Ce client est déjà attribué à un autre parrain."
            }
            concurrent
        } else {
            referral
        }

        return refreshReferral(effective, at)
    }

    override suspend fun award(
        referralId: ReferralId,
        at: Instant
    ): Referral {
        val dao = database.retentionDao()
        val current = dao.getReferral(referralId.value)?.toDomain()
            ?: error("Parrainage introuvable.")
        val refreshed = refreshReferral(current, at)
        val awarded = policy.award(refreshed, at)

        if (awarded == refreshed) {
            return refreshed
        }

        val updated = dao.awardReferralWithOutbox(
            id = awarded.id.value,
            awardedAtEpochMs = requireNotNull(awarded.awardedAt).toEpochMilli(),
            operation = OutboxOperationFactory.referral(awarded)
        )
        return if (updated > 0) {
            awarded
        } else {
            requireNotNull(dao.getReferral(referralId.value)).toDomain()
        }
    }

    private suspend fun refreshReferral(
        referral: Referral,
        at: Instant
    ): Referral {
        if (referral.rewardStatus != ReferralRewardStatus.PENDING) {
            return referral
        }

        val completed = database.retentionDao()
            .completedCollectionCount(referral.referredCustomerId.value)
        val qualified = policy.qualify(
            referral = referral,
            completedCollections = completed,
            at = at
        )

        if (qualified != referral) {
            val updated = database.retentionDao().qualifyReferralWithOutbox(
                id = qualified.id.value,
                qualifiedAtEpochMs = requireNotNull(qualified.qualifiedAt).toEpochMilli(),
                operation = OutboxOperationFactory.referral(qualified)
            )
            if (updated == 0) {
                return requireNotNull(
                    database.retentionDao().getReferral(referral.id.value)
                ).toDomain()
            }
        }
        return qualified
    }

    private fun stableReferralId(referredCustomerId: CustomerId): String =
        UUID.nameUUIDFromBytes(
            "referral:${referredCustomerId.value}"
                .toByteArray(StandardCharsets.UTF_8)
        ).toString()

    private fun ReferralEntity.toDomain(): Referral =
        Referral(
            id = ReferralId(id),
            referrerCustomerId = CustomerId(referrerCustomerId),
            referredCustomerId = CustomerId(referredCustomerId),
            referralCode = referralCode,
            rewardStatus = ReferralRewardStatus.valueOf(rewardStatus),
            attributedAt = Instant.ofEpochMilli(attributedAtEpochMs),
            qualifiedAt = qualifiedAtEpochMs?.let { Instant.ofEpochMilli(it) },
            awardedAt = awardedAtEpochMs?.let { Instant.ofEpochMilli(it) }
        )

    private fun Referral.toEntity(): ReferralEntity =
        ReferralEntity(
            id = id.value,
            referrerCustomerId = referrerCustomerId.value,
            referredCustomerId = referredCustomerId.value,
            referralCode = referralCode,
            rewardStatus = rewardStatus.name,
            attributedAtEpochMs = attributedAt.toEpochMilli(),
            qualifiedAtEpochMs = qualifiedAt?.toEpochMilli(),
            awardedAtEpochMs = awardedAt?.toEpochMilli()
        )
}
