package org.example.bankramenserver.domain.recurring.exception;

import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;

public class DuplicateRecurringPaymentException extends GlobalException {

    public DuplicateRecurringPaymentException() {
        super(ErrorCode.DUPLICATE_RECURRING_PAYMENT);
    }
}