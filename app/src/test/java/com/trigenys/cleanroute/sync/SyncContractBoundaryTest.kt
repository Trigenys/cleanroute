package com.trigenys.cleanroute.sync

import com.trigenys.cleanroute.domain.SyncOperationId
import java.io.File
import java.time.Instant
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncContractBoundaryTest {
    @Test
    fun syncPackageHasNoAndroidComposeOrHttpClientDependency() {
        val root = sourceRoot("src/main/java/com/trigenys/cleanroute/sync")
        val forbidden = listOf(
            "android.",
            "androidx.",
            "retrofit2.",
            "okhttp3.",
            "ktor.client."
        )

        val violations = root.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                val text = file.readText()
                forbidden.asSequence()
                    .filter(text::contains)
                    .map { value -> "${file.name}: $value" }
            }
            .toList()

        assertTrue(
            "Sync contract must remain transport/UI neutral: $violations",
            violations.isEmpty()
        )
    }

    @Test
    fun stableClientIdIsTheCanonicalAggregateId() {
        val payload = CustomerSyncPayload(
            aggregateId = "customer-client-generated-123",
            externalId = null,
            name = "Amina",
            phone = "+237690000001",
            zoneId = "zone-1",
            addressLabel = null,
            servicePlanId = "plan-1",
            status = CustomerSyncStatus.ACTIVE,
            createdAt = Instant.parse("2026-09-27T08:00:00Z"),
            updatedAt = Instant.parse("2026-09-27T09:00:00Z")
        )
        val envelope = SyncMutationEnvelope(
            operationId = SyncOperationId("upsert_customer:customer-client-generated-123:1"),
            aggregateType = SyncAggregateType.CUSTOMER,
            aggregateId = payload.aggregateId,
            mutation = SyncMutationType.UPSERT,
            clientRevision = payload.updatedAt.toString(),
            baseServerRevision = null,
            occurredAt = payload.updatedAt,
            payload = payload
        )

        assertTrue(envelope.aggregateId == payload.aggregateId)
    }

    @Test
    fun wireFixturesContainVersionCursorAcksAndTombstone() {
        val push = fixture("sync/v1/push-request.json")
        val response = fixture("sync/v1/push-response.json")
        val pull = fixture("sync/v1/pull-response.json")

        listOf(push, response, pull).forEach {
            assertTrue(it.contains("\"protocolVersion\": 1"))
        }
        assertTrue(push.contains("\"operationId\""))
        assertTrue(response.contains("\"acks\""))
        assertTrue(response.contains("\"duplicate\""))
        assertTrue(pull.contains("\"nextCursor\""))
        assertTrue(pull.contains("\"tombstone\""))
    }

    private fun fixture(path: String): String =
        requireNotNull(javaClass.classLoader?.getResource(path))
            .readText()

    private fun sourceRoot(relative: String): File =
        listOf(File(relative), File("app/$relative"))
            .firstOrNull(File::isDirectory)
            ?: error("Cannot locate source directory: $relative")
}
