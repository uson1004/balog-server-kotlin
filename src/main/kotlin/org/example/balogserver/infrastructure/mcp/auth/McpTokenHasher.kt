package org.example.balogserver.infrastructure.mcp.auth

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.HexFormat

object McpTokenHasher {
    @JvmStatic
    fun hash(token: String): String = HexFormat.of().formatHex(
        MessageDigest.getInstance("SHA-256").digest(token.toByteArray(StandardCharsets.UTF_8)),
    )
}
