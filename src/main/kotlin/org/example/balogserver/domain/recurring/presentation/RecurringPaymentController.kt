package org.example.balogserver.domain.recurring.presentation

import jakarta.validation.Valid
import org.example.balogserver.domain.recurring.presentation.document.RecurringPaymentDocument
import org.example.balogserver.domain.recurring.presentation.dto.request.CreateRecurringPaymentRequest
import org.example.balogserver.domain.recurring.presentation.dto.response.ConfirmRecurringPaymentResponse
import org.example.balogserver.domain.recurring.presentation.dto.response.CreateRecurringPaymentResponse
import org.example.balogserver.domain.recurring.presentation.dto.response.DeleteRecurringPaymentResponse
import org.example.balogserver.domain.recurring.presentation.dto.response.RecurringPaymentListResponse
import org.example.balogserver.domain.recurring.service.ConfirmRecurringPaymentService
import org.example.balogserver.domain.recurring.service.CreateRecurringPaymentService
import org.example.balogserver.domain.recurring.service.DeleteRecurringPaymentService
import org.example.balogserver.domain.recurring.service.GetRecurringPaymentsService
import org.example.balogserver.domain.user.facade.UserFacade
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/recurring-payments")
class RecurringPaymentController(
    private val userFacade: UserFacade,
    private val createRecurringPaymentService: CreateRecurringPaymentService,
    private val confirmRecurringPaymentService: ConfirmRecurringPaymentService,
    private val getRecurringPaymentsService: GetRecurringPaymentsService,
    private val deleteRecurringPaymentService: DeleteRecurringPaymentService,
) : RecurringPaymentDocument {
    @GetMapping
    override fun getRecurringPayments(): RecurringPaymentListResponse = getRecurringPaymentsService.execute(userFacade.currentUser.id!!)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(@Valid @RequestBody request: CreateRecurringPaymentRequest): CreateRecurringPaymentResponse =
        createRecurringPaymentService.execute(userFacade.currentUser.id!!, request)

    @PatchMapping("/{recurringPaymentId}/confirm")
    override fun confirm(@PathVariable recurringPaymentId: UUID): ConfirmRecurringPaymentResponse =
        confirmRecurringPaymentService.execute(userFacade.currentUser.id!!, recurringPaymentId)

    @DeleteMapping("/{recurringPaymentId}")
    override fun delete(@PathVariable recurringPaymentId: UUID): DeleteRecurringPaymentResponse =
        deleteRecurringPaymentService.execute(userFacade.currentUser.id!!, recurringPaymentId)
}
