package org.example.bankramenserver.domain.user.exception
import org.example.bankramenserver.global.error.exception.ErrorCode
import org.example.bankramenserver.global.error.exception.GlobalException
class UserInfoNotFound : GlobalException(ErrorCode.USER_INFO_NOT_FOUND) { companion object { @JvmField val EXCEPTION: GlobalException = UserInfoNotFound() } }
class UserNotFoundException : GlobalException(ErrorCode.USER_NOT_FOUND) { companion object { @JvmField val EXCEPTION: GlobalException = UserNotFoundException() } }
