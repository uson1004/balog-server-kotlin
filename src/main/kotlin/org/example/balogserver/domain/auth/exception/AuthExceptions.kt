package org.example.balogserver.domain.auth.exception
import org.example.balogserver.global.error.exception.ErrorCode
import org.example.balogserver.global.error.exception.GlobalException
class ExpiredTokenException : GlobalException(ErrorCode.EXPIRED_TOKEN) { companion object { @JvmField val EXCEPTION: GlobalException = ExpiredTokenException() } }
class InvalidTokenException : GlobalException(ErrorCode.INVALID_TOKEN) { companion object { @JvmField val EXCEPTION: GlobalException = InvalidTokenException() } }
class MissingTokenException : GlobalException(ErrorCode.MISSING_TOKEN) { companion object { @JvmField val EXCEPTION: GlobalException = MissingTokenException() } }
