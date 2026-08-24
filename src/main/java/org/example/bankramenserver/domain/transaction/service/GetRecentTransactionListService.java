package org.example.bankramenserver.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository;
import org.example.bankramenserver.domain.transaction.presentation.dto.RecentTransactionListResponse;
import org.example.bankramenserver.domain.transaction.presentation.dto.TransactionHistoryResponse;
import org.example.bankramenserver.domain.user.facade.UserFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetRecentTransactionListService {

    private final UserFacade userFacade;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public RecentTransactionListResponse execute(int limit) {
        return execute(userFacade.getCurrentUser().getId(), limit);
    }

    @Transactional(readOnly = true)
    public RecentTransactionListResponse execute(UUID userId, int limit) {
        return RecentTransactionListResponse.from(
                transactionRepository.findRecentTransactionHistories(userId, limit)
                        .stream()
                        .map(TransactionHistoryResponse::from)
                        .toList()
        );
    }
}
