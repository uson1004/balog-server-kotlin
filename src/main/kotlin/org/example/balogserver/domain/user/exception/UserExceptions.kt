package org.example.balogserver.domain.user.exception

import org.example.balogserver.global.error.exception.ErrorCode
import org.example.balogserver.global.error.exception.GlobalException

class UserNotFoundException : GlobalException(ErrorCode.USER_NOT_FOUND) {
    companion object {
        @JvmField
        val EXCEPTION: GlobalException = UserNotFoundException()
    }
}
