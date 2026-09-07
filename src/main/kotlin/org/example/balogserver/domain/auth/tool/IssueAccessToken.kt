package org.example.balogserver.domain.auth.tool

import org.example.balogserver.domain.auth.service.JwtService
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.nio.file.StandardOpenOption.WRITE
import java.nio.file.attribute.PosixFilePermissions
import java.util.UUID

object IssueAccessToken {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.contentEquals(arrayOf("--help"))) {
            println("Set JWT_SECRET, BALOG_USER_ID, BALOG_TOKEN_FILE and ACCESS_EXP (default 900 seconds).")
            println("Writes an access JWT to a new owner-only file. No server or database is started.")
            return
        }
        require(args.isEmpty()) { "Use --help for usage." }
        writeToken(System.getenv())
        println("Access token written to the requested file. Keep it private and reissue after expiry.")
    }

    fun writeToken(environment: Map<String, String>): Path {
        val secret = requireNotNull(environment["JWT_SECRET"]) { "JWT_SECRET is required." }
        val userId = UUID.fromString(requireNotNull(environment["BALOG_USER_ID"]) { "BALOG_USER_ID is required." })
        val output = Path.of(requireNotNull(environment["BALOG_TOKEN_FILE"]) { "BALOG_TOKEN_FILE is required." })
        val seconds = environment["ACCESS_EXP"]?.toLong() ?: 900L
        require(seconds in 1..86400) { "ACCESS_EXP must be between 1 and 86400 seconds." }
        val token = JwtService(secret, seconds, seconds).generateAccessToken(userId)
        val permissions = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))
        Files.newByteChannel(output, setOf(CREATE_NEW, WRITE), permissions).use { channel ->
            val bytes = ByteBuffer.wrap(token.toByteArray(UTF_8))
            while (bytes.hasRemaining()) channel.write(bytes)
        }
        return output
    }
}
