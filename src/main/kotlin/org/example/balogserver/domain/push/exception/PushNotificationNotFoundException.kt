package org.example.balogserver.domain.push.exception

import org.example.balogserver.global.error.exception.ErrorCode
import org.example.balogserver.global.error.exception.GlobalException

class PushNotificationNotFoundException : GlobalException(ErrorCode.PUSH_NOTIFICATION_NOT_FOUND) {
    companion object {
        @JvmField val EXCEPTION: GlobalException = PushNotificationNotFoundException()
    }
}
