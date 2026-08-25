package org.example.bankramenserver.domain.auth.exception
import org.example.bankramenserver.global.error.exception.ErrorCode
import org.example.bankramenserver.global.error.exception.GlobalException
class ExpiredTokenException : GlobalException(ErrorCode.EXPIRED_TOKEN) { companion object { @JvmField val EXCEPTION: GlobalException = ExpiredTokenException() } }
class InvalidStateException : GlobalException(ErrorCode.INVALID_STATE) { companion object { @JvmField val EXCEPTION: GlobalException = InvalidStateException() } }
class InvalidTokenException : GlobalException(ErrorCode.INVALID_TOKEN) { companion object { @JvmField val EXCEPTION: GlobalException = InvalidTokenException() } }
class KaKaoTokenRequestFailedException : GlobalException(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED) { companion object { @JvmField val EXCEPTION: GlobalException = KaKaoTokenRequestFailedException() } }
class KaKaoUserInfoRequestFailedException : GlobalException(ErrorCode.KAKAO_USER_INFO_REQUEST_FAILED) { companion object { @JvmField val EXCEPTION: GlobalException = KaKaoUserInfoRequestFailedException() } }
class MissingTokenException : GlobalException(ErrorCode.MISSING_TOKEN) { companion object { @JvmField val EXCEPTION: GlobalException = MissingTokenException() } }
