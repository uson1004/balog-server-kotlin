package org.example.bankramenserver.domain.user.service
import org.example.bankramenserver.domain.auth.dto.response.KakaoUserResponse
import org.example.bankramenserver.domain.user.domain.User
import org.example.bankramenserver.domain.user.domain.repository.UserRepository
import org.example.bankramenserver.domain.user.exception.UserInfoNotFound
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
@Service class UserService(private val userRepository: UserRepository) { @Transactional fun saveOrUpdate(kakaoUser: KakaoUserResponse?): User { val id = kakaoUser?.id ?: throw UserInfoNotFound.EXCEPTION; val account = kakaoUser.kakaoAccount; val nickname = account?.profile?.nickname; if (nickname.isNullOrBlank()) throw UserInfoNotFound.EXCEPTION; val kakaoId = id.toString(); return userRepository.findByKakaoId(kakaoId).map { it.apply { updateProfile(nickname, account.profile?.profileImageUrl) } }.orElseGet { userRepository.save(User.builder().kakaoId(kakaoId).email(account?.email).nickname(nickname).profileImageUrl(account.profile?.profileImageUrl).build()) } } }
