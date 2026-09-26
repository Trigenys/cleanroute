package com.trigenys.cleanroute.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.trigenys.cleanroute.data.local.dao.CatalogDao
import com.trigenys.cleanroute.data.local.dao.CollectionVisitDao
import com.trigenys.cleanroute.data.local.dao.CustomerDao
import com.trigenys.cleanroute.data.local.dao.OutboxDao
import com.trigenys.cleanroute.data.local.dao.PaymentDao
import com.trigenys.cleanroute.data.local.entity.CollectionVisitEntity
import com.trigenys.cleanroute.data.local.entity.ContactActionEntity
import com.trigenys.cleanroute.data.local.entity.CustomerEntity
import com.trigenys.cleanroute.data.local.entity.OutboxOperationEntity
import com.trigenys.cleanroute.data.local.entity.PaymentEntity
import com.trigenys.cleanroute.data.local.entity.RouteDayCustomerEntity
import com.trigenys.cleanroute.data.local.entity.RouteDayEntity
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
        PaymentEntity::class,
        ContactActionEntity::class,
        OutboxOperationEntity::class
    ],
    version = CleanRouteDatabase.VERSION,
    exportSchema = true
)
abstract class CleanRouteDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun customerDao(): CustomerDao
    abstract fun collectionVisitDao(): CollectionVisitDao
    abstract fun paymentDao(): PaymentDao
    abstract fun outboxDao(): OutboxDao

    companion object {
        const val VERSION = 1
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
    val ALL: Array<Migration> = emptyArray()
}
