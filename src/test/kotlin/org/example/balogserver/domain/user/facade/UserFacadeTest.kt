package org.example.balogserver.domain.user.facade

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.example.balogserver.domain.user.domain.repository.UserRepository
import org.example.balogserver.global.error.exception.GlobalException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

class UserFacadeTest {
    private val facade = UserFacade(mock(UserRepository::class.java))

    @AfterEach fun clearContext() = SecurityContextHolder.clearContext()

    @Test
    fun authenticatedUuidIsTheCurrentUser() {
        val userId = UUID.randomUUID()
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(userId, null, emptyList())
        assertThat(facade.currentUserId).isEqualTo(userId)
    }

    @Test
    fun missingAuthenticationFailsClosed() {
        assertThatThrownBy { facade.currentUserId }.isInstanceOf(GlobalException::class.java)
    }

    @Test
    fun untrustedAuthenticationFailsClosed() {
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(UUID.randomUUID(), null)
        assertThatThrownBy { facade.currentUserId }.isInstanceOf(GlobalException::class.java)
    }

    @Test
    fun nonUserPrincipalFailsClosed() {
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken("anonymousUser", null, emptyList())
        assertThatThrownBy { facade.currentUserId }.isInstanceOf(GlobalException::class.java)
    }
}
