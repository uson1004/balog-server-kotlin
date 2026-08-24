package org.example.bankramenserver.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.auth.dto.response.KakaoUserResponse;
import org.example.bankramenserver.domain.user.exception.UserInfoNotFound;
import org.example.bankramenserver.domain.user.domain.repository.UserRepository;
import org.example.bankramenserver.domain.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User saveOrUpdate(KakaoUserResponse kakaoUser) {
        if (kakaoUser == null || kakaoUser.id() == null) {
            throw UserInfoNotFound.EXCEPTION;
        }

        String kakaoId = kakaoUser.id().toString();
        String nickname = Optional.ofNullable(kakaoUser.kakaoAccount())
                .map(KakaoUserResponse.KakaoAccount::profile)
                .map(KakaoUserResponse.KakaoProfile::nickname)
                .orElse(null);
        String profileImageUrl = Optional.ofNullable(kakaoUser.kakaoAccount())
                .map(KakaoUserResponse.KakaoAccount::profile)
                .map(KakaoUserResponse.KakaoProfile::profileImageUrl)
                .orElse(null);
        String email = Optional.ofNullable(kakaoUser.kakaoAccount())
                .map(KakaoUserResponse.KakaoAccount::email)
                .orElse(null);

        if (nickname == null || nickname.isBlank()) {
            throw UserInfoNotFound.EXCEPTION;
        }

        return userRepository.findByKakaoId(kakaoId)
                .map(existing -> {
                    existing.updateProfile(nickname, profileImageUrl);
                    return existing;
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .kakaoId(kakaoId)
                                .email(email)
                                .nickname(nickname)
                                .profileImageUrl(profileImageUrl)
                                .build()
                ));
    }
}
