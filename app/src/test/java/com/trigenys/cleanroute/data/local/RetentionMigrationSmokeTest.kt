package com.trigenys.cleanroute.data.local

import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class RetentionMigrationSmokeTest {
    private val application
        get() = RuntimeEnvironment.getApplication()

    private val databaseName = "cleanroute-retention-migration.db"

    @Before
    fun before() {
        application.deleteDatabase(databaseName)
    }

    @After
    fun after() {
        application.deleteDatabase(databaseName)
    }

    @Test
    fun migrationTwoToThreeCreatesReferralTablesAndUniqueAttributionIndex() {
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(application)
            .name(databaseName)
            .callback(
                object : SupportSQLiteOpenHelper.Callback(2) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) = Unit

                    override fun onUpgrade(
                        db: androidx.sqlite.db.SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) = Unit
                }
            )
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        try {
            val database = helper.writableDatabase
            CleanRouteMigrations.MIGRATION_2_3.migrate(database)

            database.query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='referral_profiles'"
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
            }
            database.query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='referrals'"
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
            }
            database.query(
                "SELECT name FROM sqlite_master WHERE type='index' AND name='index_referrals_referredCustomerId'"
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
            }
        } finally {
            helper.close()
        }
    }
}
