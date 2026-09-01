package org.example.balogserver.domain.user.domain.repository
import org.example.balogserver.domain.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID
@Repository interface UserRepository : JpaRepository<User, UUID>
