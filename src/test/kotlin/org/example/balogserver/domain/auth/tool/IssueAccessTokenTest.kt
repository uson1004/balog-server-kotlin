package org.example.balogserver.domain.auth.tool

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.example.balogserver.domain.auth.service.JwtService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions
import java.util.UUID

class IssueAccessTokenTest {
    @TempDir lateinit var directory: Path
    private val secret = "issuer-test-only-signing-key-0123456789abcdef0123456789abcdef"
    private val userId = UUID.randomUUID()

    @Test
    fun issuedTokenMatchesServerAndFileIsPrivate() {
        val path = IssueAccessToken.writeToken(environment())
        val jwt = JwtService(secret, 900, 900)
        assertThat(jwt.validateAccessToken(Files.readString(path))).isEqualTo(userId)
        assertThat(Files.getPosixFilePermissions(path)).isEqualTo(PosixFilePermissions.fromString("rw-------"))
    }

    @Test
    fun existingTokenIsNeverOverwritten() {
        val env = environment()
        val path = IssueAccessToken.writeToken(env)
        val original = Files.readString(path)
        assertThatThrownBy { IssueAccessToken.writeToken(env) }.isInstanceOf(FileAlreadyExistsException::class.java)
        assertThat(Files.readString(path)).isEqualTo(original)
    }

    @Test
    fun invalidLifetimeDoesNotCreateTokenFile() {
        listOf("0", "-1", "86401", "invalid").forEach { value ->
            assertThatThrownBy { IssueAccessToken.writeToken(environment() + ("ACCESS_EXP" to value)) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }
        assertThat(directory.resolve("access.jwt")).doesNotExist()
    }

    private fun environment() = mapOf(
        "JWT_SECRET" to secret, "BALOG_USER_ID" to userId.toString(),
        "BALOG_TOKEN_FILE" to directory.resolve("access.jwt").toString(),
    )
}
