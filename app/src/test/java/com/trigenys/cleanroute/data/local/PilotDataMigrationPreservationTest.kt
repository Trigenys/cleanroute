package com.trigenys.cleanroute.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PilotDataMigrationPreservationTest {
    private val application
        get() = RuntimeEnvironment.getApplication()

    private val databaseName = "cleanroute-pilot-data-migration.db"

    @Before
    fun before() {
        application.deleteDatabase(databaseName)
    }

    @After
    fun after() {
        application.deleteDatabase(databaseName)
    }

    @Test
    fun migrationsOneToThreePreserveExistingPilotRecords() {
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(application)
            .name(databaseName)
            .callback(
                object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE customers (
                                id TEXT NOT NULL PRIMARY KEY,
                                name TEXT NOT NULL,
                                phone TEXT
                            )
                            """.trimIndent()
                        )
                        db.execSQL(
                            """
                            CREATE TABLE payments (
                                id TEXT NOT NULL PRIMARY KEY,
                                customerId TEXT NOT NULL,
                                amountXaf INTEGER NOT NULL,
                                state TEXT NOT NULL
                            )
                            """.trimIndent()
                        )
                        db.execSQL(
                            """
                            CREATE TABLE collection_visits (
                                id TEXT NOT NULL PRIMARY KEY,
                                customerId TEXT NOT NULL,
                                status TEXT NOT NULL
                            )
                            """.trimIndent()
                        )

                        db.execSQL(
                            "INSERT INTO customers(id, name, phone) VALUES('c-1', 'Amina', '+237690000001')"
                        )
                        db.execSQL(
                            "INSERT INTO payments(id, customerId, amountXaf, state) VALUES('p-1', 'c-1', 5000, 'RECORDED')"
                        )
                        db.execSQL(
                            "INSERT INTO collection_visits(id, customerId, status) VALUES('v-1', 'c-1', 'COLLECTED')"
                        )
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) = Unit
                }
            )
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        try {
            val db = helper.writableDatabase
            CleanRouteMigrations.MIGRATION_1_2.migrate(db)
            CleanRouteMigrations.MIGRATION_2_3.migrate(db)

            db.query("SELECT name, phone FROM customers WHERE id='c-1'").use { cursor ->
                cursor.moveToFirst()
                assertEquals("Amina", cursor.getString(0))
                assertEquals("+237690000001", cursor.getString(1))
            }

            db.query("SELECT amountXaf, state FROM payments WHERE id='p-1'").use { cursor ->
                cursor.moveToFirst()
                assertEquals(5_000L, cursor.getLong(0))
                assertEquals("RECORDED", cursor.getString(1))
            }

            db.query("SELECT status FROM collection_visits WHERE id='v-1'").use { cursor ->
                cursor.moveToFirst()
                assertEquals("COLLECTED", cursor.getString(0))
            }
        } finally {
            helper.close()
        }
    }
}
