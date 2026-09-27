package com.trigenys.cleanroute.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.security.MessageDigest
import java.util.Locale

enum class ReferralRewardStatus {
    PENDING,
    ELIGIBLE,
    AWARDED
}

data class ReferralRewardRule(
    val minCompletedCollections: Int,
    val rewardLabel: String
) {
    init {
        require(minCompletedCollections >= 1) {
            "Referral qualification must require at least one completed collection"
        }
        require(rewardLabel.isNotBlank()) {
            "Referral reward label must not be blank"
        }
    }
}

data class RetentionProgramConfig(
    val enabled: Boolean,
    val codePrefix: String,
    val referralBaseUrl: String?,
    val rewardRule: ReferralRewardRule
) {
    init {
        require(codePrefix.isNotBlank()) { "Referral code prefix must not be blank" }
        require(referralBaseUrl == null || referralBaseUrl.isNotBlank()) {
            "Referral base URL must be null or non-blank"
        }
    }
}

data class Referral(
    val id: ReferralId,
    val referrerCustomerId: CustomerId,
    val referredCustomerId: CustomerId,
    val referralCode: String,
    val rewardStatus: ReferralRewardStatus,
    val attributedAt: Instant,
    val qualifiedAt: Instant? = null,
    val awardedAt: Instant? = null
) {
    init {
        require(referrerCustomerId != referredCustomerId) {
            "A customer cannot refer themselves"
        }
        require(referralCode.isNotBlank()) { "Referral code must not be blank" }
        require(
            when (rewardStatus) {
                ReferralRewardStatus.PENDING ->
                    qualifiedAt == null && awardedAt == null
                ReferralRewardStatus.ELIGIBLE ->
                    qualifiedAt != null && awardedAt == null
                ReferralRewardStatus.AWARDED ->
                    qualifiedAt != null && awardedAt != null
            }
        ) {
            "Referral reward status and timestamps must agree"
        }
    }
}

data class ReferralSummary(
    val referral: Referral,
    val customerName: String
)

data class RetentionIndicators(
    val customerSince: LocalDate,
    val tenureDays: Long,
    val completedCollectionsLast90Days: Int,
    val recordedVisitsLast90Days: Int
) {
    init {
        require(tenureDays >= 0) { "Tenure days must not be negative" }
        require(completedCollectionsLast90Days >= 0) {
            "Completed collection count must not be negative"
        }
        require(recordedVisitsLast90Days >= completedCollectionsLast90Days) {
            "Recorded visit count must include completed collections"
        }
    }
}

data class RetentionCustomerProfile(
    val customerId: CustomerId,
    val referralCode: String,
    val referralLink: String?,
    val rewardLabel: String,
    val indicators: RetentionIndicators,
    val referredBy: ReferralSummary?,
    val referralsMade: List<ReferralSummary>
)

class ReferralCodeFactory(
    private val config: RetentionProgramConfig
) {
    fun codeFor(customerId: CustomerId): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(customerId.value.toByteArray())
            .joinToString("") { byte -> "%02x".format(byte) }
            .take(8)
            .uppercase(Locale.ROOT)
        return "${config.codePrefix.trim().uppercase(Locale.ROOT)}-$digest"
    }

    fun linkFor(code: String): String? =
        config.referralBaseUrl
            ?.trim()
            ?.trimEnd('/')
            ?.let { base -> "$base?ref=$code" }
}

class ReferralRewardPolicy(
    private val rule: ReferralRewardRule
) {
    fun qualify(
        referral: Referral,
        completedCollections: Int,
        at: Instant
    ): Referral {
        require(completedCollections >= 0) {
            "Completed collection count must not be negative"
        }
        if (referral.rewardStatus != ReferralRewardStatus.PENDING) {
            return referral
        }
        if (completedCollections < rule.minCompletedCollections) {
            return referral
        }
        return referral.copy(
            rewardStatus = ReferralRewardStatus.ELIGIBLE,
            qualifiedAt = at
        )
    }

    fun award(
        referral: Referral,
        at: Instant
    ): Referral {
        if (referral.rewardStatus == ReferralRewardStatus.AWARDED) {
            return referral
        }
        require(referral.rewardStatus == ReferralRewardStatus.ELIGIBLE) {
            "Only an eligible referral can be awarded"
        }
        return referral.copy(
            rewardStatus = ReferralRewardStatus.AWARDED,
            awardedAt = at
        )
    }
}

interface RetentionRepository {
    suspend fun getProfile(
        customerId: CustomerId,
        today: LocalDate,
        timeZone: ZoneId,
        at: Instant
    ): RetentionCustomerProfile

    suspend fun attribute(
        referredCustomerId: CustomerId,
        referralCode: String,
        at: Instant
    ): Referral

    suspend fun award(
        referralId: ReferralId,
        at: Instant
    ): Referral
}
