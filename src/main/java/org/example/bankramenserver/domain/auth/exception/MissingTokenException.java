package org.example.bankramenserver.domain.auth.exception;

import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;

public class MissingTokenException extends GlobalException {

    public static final GlobalException EXCEPTION = new MissingTokenException();

    public MissingTokenException() { super(ErrorCode.MISSING_TOKEN); }

}
