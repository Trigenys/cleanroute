package com.trigenys.cleanroute.data.local

import androidx.room.Room
import com.trigenys.cleanroute.data.repository.RoomCollectionVisitRepository
import com.trigenys.cleanroute.data.repository.RoomCustomerRepository
import com.trigenys.cleanroute.data.repository.RoomRetentionRepository
import com.trigenys.cleanroute.domain.CollectionVisit
import com.trigenys.cleanroute.domain.CollectionVisitId
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.CustomerDraft
import com.trigenys.cleanroute.domain.CustomerStatus
import com.trigenys.cleanroute.domain.ReferralRewardRule
import com.trigenys.cleanroute.domain.ReferralRewardStatus
import com.trigenys.cleanroute.domain.RetentionProgramConfig
import com.trigenys.cleanroute.domain.RouteDayId
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class RetentionRepositoryTest {
    private lateinit var database: CleanRouteDatabase
    private lateinit var customers: RoomCustomerRepository
    private lateinit var retention: RoomRetentionRepository

    private val config = RetentionProgramConfig(
        enabled = true,
        codePrefix = "CR",
        referralBaseUrl = null,
        rewardRule = ReferralRewardRule(
            minCompletedCollections = 1,
            rewardLabel = "Avantage parrainage"
        )
    )

    @Before
    fun before() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            CleanRouteDatabase::class.java
        ).build()
        customers = RoomCustomerRepository(database)
        retention = RoomRetentionRepository(database, config)
    }

    @After
    fun after() {
        database.close()
    }

    @Test
    fun referralAttributionAndRewardAreIdempotent() = runBlocking {
        val referrer = createCustomer("Parrain")
        val referred = createCustomer("Filleul")
        val now = Instant.parse("2026-09-27T08:00:00Z")

        val referrerProfile = retention.getProfile(
            customerId = referrer,
            today = LocalDate.of(2026, 9, 27),
            timeZone = ZoneId.of("Africa/Douala"),
            at = now
        )

        val first = retention.attribute(
            referredCustomerId = referred,
            referralCode = referrerProfile.referralCode,
            at = now.plusSeconds(60)
        )
        val replay = retention.attribute(
            referredCustomerId = referred,
            referralCode = referrerProfile.referralCode,
            at = now.plusSeconds(120)
        )

        assertEquals(first.id, replay.id)
        assertEquals(1, database.retentionDao().referralCount())
        assertEquals(ReferralRewardStatus.PENDING, replay.rewardStatus)

        RoomCollectionVisitRepository(database).upsert(
            CollectionVisit(
                id = CollectionVisitId("visit-referred"),
                routeDayId = RouteDayId("route-test"),
                customerId = referred,
                scheduledDate = LocalDate.of(2026, 9, 27),
                status = CollectionVisitStatus.COLLECTED,
                statusChangedAt = now.plusSeconds(180),
                revision = 1
            )
        )

        val refreshed = retention.getProfile(
            customerId = referrer,
            today = LocalDate.of(2026, 9, 27),
            timeZone = ZoneId.of("Africa/Douala"),
            at = now.plusSeconds(240)
        )
        val eligible = refreshed.referralsMade.single().referral
        assertEquals(ReferralRewardStatus.ELIGIBLE, eligible.rewardStatus)

        val outboxBeforeAward = database.outboxDao().count()
        val awarded = retention.award(
            referralId = eligible.id,
            at = now.plusSeconds(300)
        )
        val awardReplay = retention.award(
            referralId = eligible.id,
            at = now.plusSeconds(360)
        )

        assertEquals(ReferralRewardStatus.AWARDED, awarded.rewardStatus)
        assertEquals(awarded, awardReplay)
        assertEquals(outboxBeforeAward + 1, database.outboxDao().count())
        assertEquals(1, database.retentionDao().referralCount())
    }

    @Test
    fun featureRepositoryCannotBeCreatedWhenDisabled() {
        val failed = runCatching {
            RoomRetentionRepository(
                database = database,
                config = config.copy(enabled = false)
            )
        }.exceptionOrNull()

        assertTrue(failed is IllegalArgumentException)
    }

    private suspend fun createCustomer(name: String) =
        customers.saveDraft(
            draft = CustomerDraft(
                name = name,
                phone = "+237690000001",
                zoneName = "Bonapriso",
                monthlyFeeXaf = 5_000,
                status = CustomerStatus.ACTIVE
            ),
            now = Instant.parse("2026-09-01T08:00:00Z")
        )
}
