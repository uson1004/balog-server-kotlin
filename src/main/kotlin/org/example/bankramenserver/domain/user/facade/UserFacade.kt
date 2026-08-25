package org.example.bankramenserver.domain.user.facade
import org.example.bankramenserver.domain.auth.exception.InvalidTokenException
import org.example.bankramenserver.domain.user.domain.User
import org.example.bankramenserver.domain.user.domain.repository.UserRepository
import org.example.bankramenserver.domain.user.exception.UserNotFoundException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.UUID
@Component class UserFacade(private val userRepository: UserRepository) {
    val currentUser: User get() = userRepository.findById(currentUserId).orElseThrow { UserNotFoundException.EXCEPTION }
    val currentUserId: UUID get() { val authentication = SecurityContextHolder.getContext().authentication ?: throw InvalidTokenException.EXCEPTION; if (!authentication.isAuthenticated || authentication.principal !is UUID) throw InvalidTokenException.EXCEPTION; return authentication.principal as UUID }
}
