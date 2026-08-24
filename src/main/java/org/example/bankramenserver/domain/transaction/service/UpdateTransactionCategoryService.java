package org.example.bankramenserver.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.transaction.domain.Transaction;
import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository;
import org.example.bankramenserver.domain.transaction.exception.TransactionNotFoundException;
import org.example.bankramenserver.domain.transaction.presentation.dto.TransactionHistoryResponse;
import org.example.bankramenserver.domain.transaction.presentation.dto.UpdateTransactionCategoryRequest;
import org.example.bankramenserver.domain.user.facade.UserFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateTransactionCategoryService {

    private final UserFacade userFacade;
    private final TransactionRepository transactionRepository;

    @Transactional
    public TransactionHistoryResponse execute(UUID transactionId, UpdateTransactionCategoryRequest request) {
        UUID currentUserId = userFacade.getCurrentUser().getId();
        Transaction transaction = transactionRepository.findByIdAndUser_Id(transactionId, currentUserId)
                .orElseThrow(() -> TransactionNotFoundException.EXCEPTION);

        transaction.updateCategory(request.category());

        return TransactionHistoryResponse.from(transaction);
    }
}
