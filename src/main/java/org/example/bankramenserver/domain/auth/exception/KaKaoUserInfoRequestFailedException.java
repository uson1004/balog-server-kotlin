package org.example.bankramenserver.domain.auth.exception;

import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;

public class KaKaoUserInfoRequestFailedException extends GlobalException {

    public static final GlobalException EXCEPTION = new KaKaoUserInfoRequestFailedException();

    public KaKaoUserInfoRequestFailedException() { super(ErrorCode.KAKAO_USER_INFO_REQUEST_FAILED); }

}
