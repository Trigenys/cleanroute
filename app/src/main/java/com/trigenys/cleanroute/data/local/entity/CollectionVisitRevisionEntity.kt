package com.trigenys.cleanroute.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "collection_visit_revisions",
    primaryKeys = ["visitId", "revision"],
    indices = [
        Index(value = ["changedAtEpochMs"])
    ]
)
data class CollectionVisitRevisionEntity(
    val visitId: String,
    val revision: Int,
    val status: String,
    val changedAtEpochMs: Long
)
