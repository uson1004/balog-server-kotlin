package org.example.bankramenserver.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.transaction.domain.Transaction;
import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository;
import org.example.bankramenserver.domain.transaction.presentation.dto.CreateTransactionRequest;
import org.example.bankramenserver.domain.user.domain.User;
import org.example.bankramenserver.domain.user.facade.UserFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateTransactionService {

    private final UserFacade userFacade;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void execute(CreateTransactionRequest request) {
        User currentUser = userFacade.getCurrentUser();
        Transaction transaction = Transaction.builder()
                .user(currentUser)
                .category(request.category())
                .type(request.type())
                .amount(request.amount())
                .description(request.title())
                .source(Transaction.TransactionSource.MANUAL)
                .transactionDate(request.transactionDate())
                .build();

        transactionRepository.save(transaction);
    }
}
