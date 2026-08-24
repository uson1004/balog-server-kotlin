package org.example.bankramenserver.domain.recurring.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.example.bankramenserver.domain.recurring.domain.RecurringPayment;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "정기결제 직접 등록 요청")
public record CreateRecurringPaymentRequest(

        @Schema(description = "정기결제로 등록할 거래 ID")
        @NotNull
        UUID transactionId,

        @Schema(description = "정기결제 주기", example = "MONTHLY")
        @NotNull
        RecurringPayment.Cycle cycle,

        @Schema(description = "다음 결제 예정일", example = "2026-06-15")
        @NotNull
        LocalDate nextBillingDate
) {
}