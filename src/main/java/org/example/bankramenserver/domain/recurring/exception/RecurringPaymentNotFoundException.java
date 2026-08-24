package org.example.bankramenserver.domain.recurring.exception;

import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;

public class RecurringPaymentNotFoundException extends GlobalException {

    public RecurringPaymentNotFoundException() {
        super(ErrorCode.RECURRING_PAYMENT_NOT_FOUND);
    }
}