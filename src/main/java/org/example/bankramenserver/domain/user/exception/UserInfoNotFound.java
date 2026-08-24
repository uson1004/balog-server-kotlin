package org.example.bankramenserver.domain.user.exception;

import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;

public class UserInfoNotFound extends GlobalException {

    public static final GlobalException EXCEPTION = new UserInfoNotFound();

    public UserInfoNotFound() { super(ErrorCode.USER_INFO_NOT_FOUND); }
}
