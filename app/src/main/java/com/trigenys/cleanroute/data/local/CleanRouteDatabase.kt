package com.trigenys.cleanroute.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.trigenys.cleanroute.data.local.dao.CatalogDao
import com.trigenys.cleanroute.data.local.dao.CollectionVisitDao
import com.trigenys.cleanroute.data.local.dao.ContactActionDao
import com.trigenys.cleanroute.data.local.dao.CustomerDao
import com.trigenys.cleanroute.data.local.dao.DashboardDao
import com.trigenys.cleanroute.data.local.dao.OutboxDao
import com.trigenys.cleanroute.data.local.dao.PaymentDao
import com.trigenys.cleanroute.data.local.dao.RouteDayDao
import com.trigenys.cleanroute.data.local.dao.RetentionDao
import com.trigenys.cleanroute.data.local.entity.CollectionVisitEntity
import com.trigenys.cleanroute.data.local.entity.CollectionVisitRevisionEntity
import com.trigenys.cleanroute.data.local.entity.ContactActionEntity
import com.trigenys.cleanroute.data.local.entity.CustomerEntity
import com.trigenys.cleanroute.data.local.entity.OutboxOperationEntity
import com.trigenys.cleanroute.data.local.entity.PaymentEntity
import com.trigenys.cleanroute.data.local.entity.RouteDayCustomerEntity
import com.trigenys.cleanroute.data.local.entity.RouteDayEntity
import com.trigenys.cleanroute.data.local.entity.ReferralEntity
import com.trigenys.cleanroute.data.local.entity.ReferralProfileEntity
import com.trigenys.cleanroute.data.local.entity.ServicePlanEntity
import com.trigenys.cleanroute.data.local.entity.ZoneEntity

@Database(
    entities = [
        ZoneEntity::class,
        ServicePlanEntity::class,
        CustomerEntity::class,
        RouteDayEntity::class,
        RouteDayCustomerEntity::class,
        CollectionVisitEntity::class,
        CollectionVisitRevisionEntity::class,
        PaymentEntity::class,
        ContactActionEntity::class,
        ReferralProfileEntity::class,
        ReferralEntity::class,
        OutboxOperationEntity::class
    ],
    version = CleanRouteDatabase.VERSION,
    exportSchema = true
)
abstract class CleanRouteDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun customerDao(): CustomerDao
    abstract fun dashboardDao(): DashboardDao
    abstract fun collectionVisitDao(): CollectionVisitDao
    abstract fun contactActionDao(): ContactActionDao
    abstract fun routeDayDao(): RouteDayDao
    abstract fun paymentDao(): PaymentDao
    abstract fun retentionDao(): RetentionDao
    abstract fun outboxDao(): OutboxDao

    companion object {
        const val VERSION = 3
        const val DEFAULT_NAME = "cleanroute.db"

        fun open(
            context: Context,
            name: String = DEFAULT_NAME
        ): CleanRouteDatabase = Room.databaseBuilder(
            context.applicationContext,
            CleanRouteDatabase::class.java,
            name
        )
            .addMigrations(*CleanRouteMigrations.ALL)
            .build()
    }
}

object CleanRouteMigrations {
    val MIGRATION_1_2 = Migration(1, 2) { database ->
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS collection_visit_revisions (
                visitId TEXT NOT NULL,
                revision INTEGER NOT NULL,
                status TEXT NOT NULL,
                changedAtEpochMs INTEGER NOT NULL,
                PRIMARY KEY(visitId, revision)
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_collection_visit_revisions_changedAtEpochMs
            ON collection_visit_revisions(changedAtEpochMs)
            """.trimIndent()
        )
    }

    val MIGRATION_2_3 = Migration(2, 3) { database ->
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS referral_profiles (
                customerId TEXT NOT NULL,
                code TEXT NOT NULL,
                createdAtEpochMs INTEGER NOT NULL,
                PRIMARY KEY(customerId)
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_referral_profiles_code
            ON referral_profiles(code)
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS referrals (
                id TEXT NOT NULL,
                referrerCustomerId TEXT NOT NULL,
                referredCustomerId TEXT NOT NULL,
                referralCode TEXT NOT NULL,
                rewardStatus TEXT NOT NULL,
                attributedAtEpochMs INTEGER NOT NULL,
                qualifiedAtEpochMs INTEGER,
                awardedAtEpochMs INTEGER,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_referrals_referredCustomerId
            ON referrals(referredCustomerId)
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_referrals_referrerCustomerId
            ON referrals(referrerCustomerId)
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_referrals_rewardStatus
            ON referrals(rewardStatus)
            """.trimIndent()
        )
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3
    )
}
