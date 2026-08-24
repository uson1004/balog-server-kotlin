package org.example.bankramenserver.domain.recurring.exception;

import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;

public class RecurringPaymentAccessDeniedException extends GlobalException {

    public RecurringPaymentAccessDeniedException() {
        super(ErrorCode.RECURRING_PAYMENT_ACCESS_DENIED);
    }
}