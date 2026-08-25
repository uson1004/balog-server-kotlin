package org.example.bankramenserver.domain.user.domain

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
@Table(name = "users")
class User protected constructor() : BaseEntity() {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(columnDefinition = "BINARY(16)")
    @field:JdbcTypeCode(SqlTypes.BINARY)
    final var id: UUID? = null
        private set

    final var kakaoId: String? = null
        private set
    final var nickname: String? = null
        private set
    final var profileImageUrl: String? = null
        private set
    final var email: String? = null
        private set

    constructor(kakaoId: String?, nickname: String?, profileImageUrl: String?, email: String?) : this() {
        this.kakaoId = kakaoId
        this.nickname = nickname
        this.profileImageUrl = profileImageUrl
        this.email = email
    }

    fun updateProfile(nickname: String?, profileImageUrl: String?) {
        this.nickname = nickname
        this.profileImageUrl = profileImageUrl
    }

    companion object {
        @JvmStatic fun builder() = UserBuilder()
    }

    class UserBuilder {
        private var kakaoId: String? = null
        private var nickname: String? = null
        private var profileImageUrl: String? = null
        private var email: String? = null
        fun kakaoId(value: String?) = apply { kakaoId = value }
        fun nickname(value: String?) = apply { nickname = value }
        fun profileImageUrl(value: String?) = apply { profileImageUrl = value }
        fun email(value: String?) = apply { email = value }
        fun build() = User(kakaoId, nickname, profileImageUrl, email)
    }
}
