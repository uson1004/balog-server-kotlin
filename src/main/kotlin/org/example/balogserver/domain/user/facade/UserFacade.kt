package org.example.balogserver.domain.user.facade
import org.example.balogserver.domain.auth.exception.InvalidTokenException
import org.springframework.security.core.context.SecurityContextHolder
import org.example.balogserver.domain.user.domain.User
import org.example.balogserver.domain.user.domain.repository.UserRepository
import org.example.balogserver.domain.user.exception.UserNotFoundException
import org.springframework.stereotype.Component
import java.util.UUID
@Component class UserFacade(private val userRepository: UserRepository) {
    val currentUser: User get() = userRepository.findById(currentUserId).orElseThrow { UserNotFoundException.EXCEPTION }
    val currentUserId: UUID
        get() {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication?.isAuthenticated != true) throw InvalidTokenException.EXCEPTION
            return authentication.principal as? UUID ?: throw InvalidTokenException.EXCEPTION
        }
}
