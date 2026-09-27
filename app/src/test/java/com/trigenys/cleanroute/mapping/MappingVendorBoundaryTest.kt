package com.trigenys.cleanroute.mapping

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class MappingVendorBoundaryTest {
    @Test
    fun mappingAndRoutingVendorsDoNotLeakIntoDomainLayer() {
        val domain = sourceRoot("src/main/java/com/trigenys/cleanroute/domain")
        val forbidden = listOf(
            "org.maplibre",
            "com.graphhopper"
        )

        val violations = domain.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                val text = file.readText()
                forbidden.asSequence()
                    .filter(text::contains)
                    .map { vendor -> "${file.relativeTo(domain).path}: $vendor" }
            }
            .toList()

        assertTrue(
            "Mapping/routing vendor types must stay outside domain code. " +
                "Violations: ${violations.joinToString()}",
            violations.isEmpty()
        )
    }

    @Test
    fun productionMappingContractIsVendorNeutral() {
        val mapping = sourceFile(
            "src/main/java/com/trigenys/cleanroute/mapping/MappingModels.kt"
        ).readText()

        assertTrue(!mapping.contains("org.maplibre"))
        assertTrue(!mapping.contains("com.graphhopper"))
    }

    private fun sourceRoot(relative: String): File =
        listOf(File(relative), File("app/$relative"))
            .firstOrNull(File::isDirectory)
            ?: error("Cannot locate source directory: $relative")

    private fun sourceFile(relative: String): File =
        listOf(File(relative), File("app/$relative"))
            .firstOrNull(File::isFile)
            ?: error("Cannot locate source file: $relative")
}
