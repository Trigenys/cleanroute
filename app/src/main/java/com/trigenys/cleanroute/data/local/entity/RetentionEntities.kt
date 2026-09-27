package com.trigenys.cleanroute.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "referral_profiles",
    indices = [
        Index(value = ["code"], unique = true)
    ]
)
data class ReferralProfileEntity(
    @androidx.room.PrimaryKey val customerId: String,
    val code: String,
    val createdAtEpochMs: Long
)

@Entity(
    tableName = "referrals",
    indices = [
        Index(value = ["referredCustomerId"], unique = true),
        Index(value = ["referrerCustomerId"]),
        Index(value = ["rewardStatus"])
    ]
)
data class ReferralEntity(
    @androidx.room.PrimaryKey val id: String,
    val referrerCustomerId: String,
    val referredCustomerId: String,
    val referralCode: String,
    val rewardStatus: String,
    val attributedAtEpochMs: Long,
    val qualifiedAtEpochMs: Long?,
    val awardedAtEpochMs: Long?
)
