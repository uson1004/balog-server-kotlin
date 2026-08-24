package org.example.bankramenserver.domain.auth.exception;

import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;

public class KaKaoTokenRequestFailedException extends GlobalException {

    public static final GlobalException EXCEPTION = new KaKaoTokenRequestFailedException();

    public KaKaoTokenRequestFailedException() { super(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED); }

}
