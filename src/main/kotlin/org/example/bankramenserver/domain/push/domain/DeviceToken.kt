package org.example.bankramenserver.domain.push.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.example.bankramenserver.global.common.BaseEntity
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "device_token")
class DeviceToken protected constructor() : BaseEntity() {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    final var id: Long? = null
        private set
    @field:Column(nullable = false, columnDefinition = "BINARY(16)")
    @field:JdbcTypeCode(SqlTypes.BINARY)
    final lateinit var memberId: UUID
        private set
    @field:Column(nullable = false, unique = true)
    final lateinit var token: String
        private set

    constructor(memberId: UUID, token: String) : this() {
        this.memberId = memberId
        this.token = token
    }

    fun updateMember(memberId: UUID) { this.memberId = memberId }
    fun updateToken(token: String) { this.token = token }

    companion object { @JvmStatic fun builder() = DeviceTokenBuilder() }
    class DeviceTokenBuilder {
        private var memberId: UUID? = null
        private var token: String? = null
        fun memberId(value: UUID?) = apply { memberId = value }
        fun token(value: String?) = apply { token = value }
        fun build() = DeviceToken(memberId!!, token!!)
    }
}
