package org.example.bankramenserver.domain.user.domain.repository
import org.example.bankramenserver.domain.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID
@Repository interface UserRepository : JpaRepository<User, UUID> { fun findByKakaoId(kakaoId: String): Optional<User> }
