package org.example.balogserver.domain.user.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.example.balogserver.global.common.BaseEntity
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "users")
class User protected constructor() : BaseEntity() {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(columnDefinition = "BINARY(16)")
    @field:JdbcTypeCode(SqlTypes.BINARY)
    final var id: UUID? = null
        private set

    final var nickname: String? = null
        private set
    final var profileImageUrl: String? = null
        private set
    final var email: String? = null
        private set

    constructor(nickname: String?, profileImageUrl: String?, email: String?) : this() {
        this.nickname = nickname
        this.profileImageUrl = profileImageUrl
        this.email = email
    }

    companion object {
        fun singleUser(id: UUID) = User("Balog 사용자", null, null).also { it.id = id }
        @JvmStatic fun builder() = UserBuilder()
    }

    class UserBuilder {
        private var nickname: String? = null
        private var profileImageUrl: String? = null
        private var email: String? = null
        fun nickname(value: String?) = apply { nickname = value }
        fun profileImageUrl(value: String?) = apply { profileImageUrl = value }
        fun email(value: String?) = apply { email = value }
        fun build() = User(nickname, profileImageUrl, email)
    }
}
