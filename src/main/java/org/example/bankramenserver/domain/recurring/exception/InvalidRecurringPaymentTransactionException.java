package org.example.bankramenserver.domain.recurring.exception;

import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;

public class InvalidRecurringPaymentTransactionException extends GlobalException {

    public InvalidRecurringPaymentTransactionException() {
        super(ErrorCode.INVALID_RECURRING_PAYMENT_TRANSACTION);
    }
}