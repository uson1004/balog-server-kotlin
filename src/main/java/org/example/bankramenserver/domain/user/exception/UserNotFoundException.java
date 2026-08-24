package org.example.bankramenserver.domain.user.exception;

import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;

public class UserNotFoundException extends GlobalException {

    public static final GlobalException EXCEPTION = new UserNotFoundException();

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
