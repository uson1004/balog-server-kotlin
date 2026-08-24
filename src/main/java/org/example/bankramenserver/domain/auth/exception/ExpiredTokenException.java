package org.example.bankramenserver.domain.auth.exception;

import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;

public class ExpiredTokenException extends GlobalException {

    public static final GlobalException EXCEPTION = new ExpiredTokenException();

    public ExpiredTokenException() { super(ErrorCode.EXPIRED_TOKEN); }

}
