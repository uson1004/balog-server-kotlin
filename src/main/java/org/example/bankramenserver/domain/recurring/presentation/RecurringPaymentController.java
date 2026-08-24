package org.example.bankramenserver.domain.recurring.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.recurring.presentation.document.RecurringPaymentDocument;
import org.example.bankramenserver.domain.recurring.presentation.dto.request.CreateRecurringPaymentRequest;
import org.example.bankramenserver.domain.recurring.presentation.dto.response.ConfirmRecurringPaymentResponse;
import org.example.bankramenserver.domain.recurring.presentation.dto.response.CreateRecurringPaymentResponse;
import org.example.bankramenserver.domain.recurring.presentation.dto.response.DeleteRecurringPaymentResponse;
import org.example.bankramenserver.domain.recurring.presentation.dto.response.RecurringPaymentListResponse;
import org.example.bankramenserver.domain.recurring.service.ConfirmRecurringPaymentService;
import org.example.bankramenserver.domain.recurring.service.CreateRecurringPaymentService;
import org.example.bankramenserver.domain.recurring.service.DeleteRecurringPaymentService;
import org.example.bankramenserver.domain.recurring.service.GetRecurringPaymentsService;
import org.example.bankramenserver.domain.user.facade.UserFacade;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recurring-payments")
public class RecurringPaymentController implements RecurringPaymentDocument {

    private final UserFacade userFacade;

    private final CreateRecurringPaymentService createRecurringPaymentService;
    private final ConfirmRecurringPaymentService confirmRecurringPaymentService;
    private final GetRecurringPaymentsService getRecurringPaymentsService;
    private final DeleteRecurringPaymentService deleteRecurringPaymentService;

    @Override
    @GetMapping
    public RecurringPaymentListResponse getRecurringPayments() {
        return getRecurringPaymentsService.execute(
                userFacade.getCurrentUser().getId()
        );
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateRecurringPaymentResponse create(
            @Valid @RequestBody CreateRecurringPaymentRequest request
    ) {
        return createRecurringPaymentService.execute(
                userFacade.getCurrentUser().getId(),
                request
        );
    }

    @Override
    @PatchMapping("/{recurringPaymentId}/confirm")
    public ConfirmRecurringPaymentResponse confirm(
            @PathVariable UUID recurringPaymentId
    ) {
        return confirmRecurringPaymentService.execute(
                userFacade.getCurrentUser().getId(),
                recurringPaymentId
        );
    }

    @Override
    @DeleteMapping("/{recurringPaymentId}")
    public DeleteRecurringPaymentResponse delete(
            @PathVariable UUID recurringPaymentId
    ) {
        return deleteRecurringPaymentService.execute(
                userFacade.getCurrentUser().getId(),
                recurringPaymentId
        );
    }
}