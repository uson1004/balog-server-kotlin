package org.example.bankramenserver.domain.auth.exception;

import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;

public class InvalidTokenException extends GlobalException {

    public static final GlobalException EXCEPTION = new InvalidTokenException();

    public InvalidTokenException() { super(ErrorCode.INVALID_TOKEN); }

}
