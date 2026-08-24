package org.example.bankramenserver.domain.user.facade;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.auth.exception.InvalidTokenException;
import org.example.bankramenserver.domain.user.domain.User;
import org.example.bankramenserver.domain.user.domain.repository.UserRepository;
import org.example.bankramenserver.domain.user.exception.UserNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        UUID currentUserId = getCurrentUserId();

        return userRepository.findById(currentUserId)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw InvalidTokenException.EXCEPTION;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UUID userId)) {
            throw InvalidTokenException.EXCEPTION;
        }

        return userId;
    }
}
