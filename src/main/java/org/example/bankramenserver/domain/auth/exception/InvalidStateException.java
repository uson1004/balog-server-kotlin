package org.example.bankramenserver.domain.auth.exception;

import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;

public class InvalidStateException extends GlobalException {

    public static final GlobalException EXCEPTION = new InvalidStateException();

    public InvalidStateException() { super(ErrorCode.INVALID_STATE); }
}
