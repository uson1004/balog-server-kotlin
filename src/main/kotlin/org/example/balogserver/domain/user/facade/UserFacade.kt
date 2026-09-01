package org.example.balogserver.domain.user.facade
import org.example.balogserver.domain.user.config.SingleUserProperties
import org.example.balogserver.domain.user.domain.User
import org.example.balogserver.domain.user.domain.repository.UserRepository
import org.example.balogserver.domain.user.exception.UserNotFoundException
import org.springframework.stereotype.Component
import java.util.UUID
@Component class UserFacade(private val userRepository: UserRepository, private val properties: SingleUserProperties) {
    val currentUser: User get() = userRepository.findById(currentUserId).orElseThrow { UserNotFoundException.EXCEPTION }
    val currentUserId: UUID get() = properties.id
}
