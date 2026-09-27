package com.trigenys.cleanroute.security

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SensitiveLoggingGuardTest {
    @Test
    fun productionSourcesDoNotUseDirectDebugLogging() {
        val root = sourceRoot("src/main/java")
        val forbidden = listOf(
            Regex("""\bandroid\.util\.Log\b""") to "android.util.Log",
            Regex("""\bLog\.(v|d|i|w|e|wtf)\s*\(""") to "Log.*",
            Regex("""\bTimber\.""") to "Timber",
            Regex("""\bprintln\s*\(""") to "println",
            Regex("""\bprintStackTrace\s*\(""") to "printStackTrace",
            Regex("""\bSystem\.(out|err)\b""") to "System.out/System.err"
        )

        val violations = root.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                val text = file.readText()
                forbidden.asSequence()
                    .filter { (pattern, _) -> pattern.containsMatchIn(text) }
                    .map { (_, label) -> "${file.relativeTo(root).path}: $label" }
            }
            .toList()

        assertTrue(
            "Production logging is forbidden until a redacting logger is introduced. " +
                "Violations: ${violations.joinToString()}",
            violations.isEmpty()
        )
    }

    @Test
    fun domainDoesNotEmbedAndroidOrApiCredentialAssumptions() {
        val domain = sourceRoot("src/main/java/com/trigenys/cleanroute/domain")
        val forbidden = listOf(
            Regex("""\bandroid\.""") to "Android dependency",
            Regex("""\bAuthorization\b""", RegexOption.IGNORE_CASE) to "Authorization header",
            Regex("""\bBearer\b""", RegexOption.IGNORE_CASE) to "Bearer scheme",
            Regex("""\baccessToken\b""", RegexOption.IGNORE_CASE) to "access token",
            Regex("""\brefreshToken\b""", RegexOption.IGNORE_CASE) to "refresh token",
            Regex("""\bSharedPreferences\b""") to "SharedPreferences credential storage"
        )

        val violations = domain.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                val text = file.readText()
                forbidden.asSequence()
                    .filter { (pattern, _) -> pattern.containsMatchIn(text) }
                    .map { (_, label) -> "${file.relativeTo(domain).path}: $label" }
            }
            .toList()

        assertTrue(
            "Android/API credential assumptions must stay outside the domain layer. " +
                "Violations: ${violations.joinToString()}",
            violations.isEmpty()
        )
    }

    @Test
    fun androidBackupRemainsDisabled() {
        val manifest = sourceFile("src/main/AndroidManifest.xml").readText()

        assertTrue(
            "Customer/payment data must not enter Android app backups.",
            manifest.contains("""android:allowBackup="false"""")
        )
    }

    private fun sourceRoot(relative: String): File {
        val candidates = listOf(
            File(relative),
            File("app/$relative")
        )
        return candidates.firstOrNull(File::isDirectory)
            ?: error("Cannot locate source directory: $relative")
    }

    private fun sourceFile(relative: String): File {
        val candidates = listOf(
            File(relative),
            File("app/$relative")
        )
        return candidates.firstOrNull(File::isFile)
            ?: error("Cannot locate source file: $relative")
    }
}
