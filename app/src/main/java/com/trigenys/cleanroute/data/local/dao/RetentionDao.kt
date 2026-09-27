package com.trigenys.cleanroute.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.trigenys.cleanroute.data.local.ReferralSummaryRow
import com.trigenys.cleanroute.data.local.RetentionVisitCountsRow
import com.trigenys.cleanroute.data.local.entity.OutboxOperationEntity
import com.trigenys.cleanroute.data.local.entity.ReferralEntity
import com.trigenys.cleanroute.data.local.entity.ReferralProfileEntity

@Dao
abstract class RetentionDao {
    @Query("SELECT * FROM referral_profiles WHERE customerId = :customerId LIMIT 1")
    abstract suspend fun getProfile(customerId: String): ReferralProfileEntity?

    @Query("SELECT * FROM referral_profiles WHERE code = :code LIMIT 1")
    abstract suspend fun getProfileByCode(code: String): ReferralProfileEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertProfile(profile: ReferralProfileEntity): Long

    @Query("SELECT * FROM referrals WHERE id = :id LIMIT 1")
    abstract suspend fun getReferral(id: String): ReferralEntity?

    @Query(
        """
        SELECT * FROM referrals
        WHERE referredCustomerId = :customerId
        LIMIT 1
        """
    )
    abstract suspend fun getInbound(customerId: String): ReferralEntity?

    @Query(
        """
        SELECT
            r.*,
            c.name AS customerName
        FROM referrals r
        INNER JOIN customers c ON c.id = r.referredCustomerId
        WHERE r.referrerCustomerId = :customerId
        ORDER BY r.attributedAtEpochMs DESC, r.id ASC
        """
    )
    abstract suspend fun getOutbound(customerId: String): List<ReferralSummaryRow>

    @Query(
        """
        SELECT
            r.*,
            c.name AS customerName
        FROM referrals r
        INNER JOIN customers c ON c.id = r.referrerCustomerId
        WHERE r.referredCustomerId = :customerId
        LIMIT 1
        """
    )
    abstract suspend fun getInboundSummary(customerId: String): ReferralSummaryRow?

    @Query(
        """
        SELECT
            COUNT(*) AS recordedVisits,
            COALESCE(SUM(CASE WHEN status = 'COLLECTED' THEN 1 ELSE 0 END), 0)
                AS completedCollections
        FROM collection_visits
        WHERE customerId = :customerId
          AND scheduledDateIso >= :fromDateIso
          AND scheduledDateIso <= :toDateIso
        """
    )
    abstract suspend fun visitCounts(
        customerId: String,
        fromDateIso: String,
        toDateIso: String
    ): RetentionVisitCountsRow

    @Query(
        """
        SELECT COUNT(*) FROM collection_visits
        WHERE customerId = :customerId
          AND status = 'COLLECTED'
        """
    )
    abstract suspend fun completedCollectionCount(customerId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertReferral(referral: ReferralEntity): Long

    @Query(
        """
        UPDATE referrals
        SET rewardStatus = 'ELIGIBLE',
            qualifiedAtEpochMs = :qualifiedAtEpochMs
        WHERE id = :id
          AND rewardStatus = 'PENDING'
        """
    )
    protected abstract suspend fun qualifyReferral(
        id: String,
        qualifiedAtEpochMs: Long
    ): Int

    @Query(
        """
        UPDATE referrals
        SET rewardStatus = 'AWARDED',
            awardedAtEpochMs = :awardedAtEpochMs
        WHERE id = :id
          AND rewardStatus = 'ELIGIBLE'
        """
    )
    protected abstract suspend fun awardReferral(
        id: String,
        awardedAtEpochMs: Long
    ): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertOutbox(operation: OutboxOperationEntity): Long

    @Transaction
    open suspend fun ensureProfile(profile: ReferralProfileEntity): ReferralProfileEntity {
        insertProfile(profile)
        return requireNotNull(getProfile(profile.customerId))
    }

    @Transaction
    open suspend fun insertReferralWithOutbox(
        referral: ReferralEntity,
        operation: OutboxOperationEntity
    ): Long {
        val inserted = insertReferral(referral)
        if (inserted != -1L) {
            insertOutbox(operation)
        }
        return inserted
    }

    @Transaction
    open suspend fun qualifyReferralWithOutbox(
        id: String,
        qualifiedAtEpochMs: Long,
        operation: OutboxOperationEntity
    ): Int {
        val updated = qualifyReferral(id, qualifiedAtEpochMs)
        if (updated > 0) {
            insertOutbox(operation)
        }
        return updated
    }

    @Transaction
    open suspend fun awardReferralWithOutbox(
        id: String,
        awardedAtEpochMs: Long,
        operation: OutboxOperationEntity
    ): Int {
        val updated = awardReferral(id, awardedAtEpochMs)
        if (updated > 0) {
            insertOutbox(operation)
        }
        return updated
    }

    @Query("SELECT COUNT(*) FROM referrals")
    abstract suspend fun referralCount(): Int
}
