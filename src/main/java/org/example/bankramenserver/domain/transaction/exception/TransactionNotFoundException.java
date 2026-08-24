package org.example.bankramenserver.domain.transaction.exception;

import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;

public class TransactionNotFoundException extends GlobalException {

    public static final GlobalException EXCEPTION = new TransactionNotFoundException();

    public TransactionNotFoundException() {
        super(ErrorCode.TRANSACTION_NOT_FOUND);
    }
}
