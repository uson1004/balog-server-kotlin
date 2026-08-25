package org.example.bankramenserver.domain.auth.dto.response
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoUserResponse(val id: Long?, @JsonProperty("kakao_account") val kakaoAccount: KakaoAccount?) { fun id() = id; fun kakaoAccount() = kakaoAccount
    @JsonIgnoreProperties(ignoreUnknown = true) data class KakaoAccount(val email: String?, val profile: KakaoProfile?) { fun email() = email; fun profile() = profile }
    @JsonIgnoreProperties(ignoreUnknown = true) data class KakaoProfile(val nickname: String?, @JsonProperty("profile_image_url") val profileImageUrl: String?) { fun nickname() = nickname; fun profileImageUrl() = profileImageUrl }
}
