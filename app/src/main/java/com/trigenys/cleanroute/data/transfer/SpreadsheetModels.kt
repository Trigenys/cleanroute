package com.trigenys.cleanroute.data.transfer

import com.trigenys.cleanroute.data.local.entity.CustomerEntity
import com.trigenys.cleanroute.data.local.entity.OutboxOperationEntity
import com.trigenys.cleanroute.data.local.entity.ServicePlanEntity
import com.trigenys.cleanroute.data.local.entity.ZoneEntity

enum class ImportActionType {
    CREATE,
    UPDATE,
    UNCHANGED
}

data class ImportIssue(
    val rowNumber: Int?,
    val message: String
)

data class ImportSummary(
    val creates: Int,
    val updates: Int,
    val unchanged: Int,
    val invalid: Int
)

data class CustomerImportAction(
    val type: ImportActionType,
    val zone: ZoneEntity,
    val servicePlan: ServicePlanEntity,
    val customer: CustomerEntity,
    val outboxOperation: OutboxOperationEntity
)

data class CustomerImportPlan(
    val sourceName: String,
    val summary: ImportSummary,
    val unknownHeaders: List<String>,
    val fatalIssues: List<String>,
    val rowIssues: List<ImportIssue>,
    internal val actions: List<CustomerImportAction>
) {
    val canApply: Boolean
        get() = fatalIssues.isEmpty() && (summary.creates > 0 || summary.updates > 0)
}

data class ImportApplyResult(
    val created: Int,
    val updated: Int,
    val unchanged: Int,
    val invalid: Int
)

sealed interface SpreadsheetCell {
    data class Text(val value: String) : SpreadsheetCell
    data class Number(val value: Long) : SpreadsheetCell
    data object Blank : SpreadsheetCell
}

data class SpreadsheetSheet(
    val name: String,
    val rows: Sequence<List<SpreadsheetCell>>
)
