package com.bank.microserviceTransaction.business.service.impl;

import com.bank.microserviceTransaction.Model.api.trasaction.AccountBalanceUpdateRequest;
import com.bank.microserviceTransaction.Model.api.trasaction.AccountDto;
import com.bank.microserviceTransaction.Model.api.trasaction.TransactionDto;
import com.bank.microserviceTransaction.Model.api.trasaction.TransactionRequest;
import com.bank.microserviceTransaction.Model.entity.TransactionEntity;
import com.bank.microserviceTransaction.business.repository.ITransactionRepository;
import com.bank.microserviceTransaction.business.service.ITransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements ITransactionService {


    private final ITransactionRepository transactionRepository;
    private final WebClient accountWebClient;

    @Override
    public Mono<TransactionDto> registerTransaction(TransactionRequest request) {
        return accountWebClient.get()
                .uri("/{id}", request.getAccountId())
                .retrieve()
                .bodyToMono(AccountDto.class)
                .flatMap(account -> {
                    Double accountBalance = account.getBalance();
                    if (accountBalance == null) {
                        return Mono.error(new IllegalArgumentException("Balance de la cuenta no disponible"));
                    }

                    // Validación de límites de movimiento para cuenta de ahorro
                    if ("SAVINGS".equals(account.getType()) && request.getType().equals("WITHDRAWAL")) {
                        if (accountBalance < request.getAmount()) {
                            return Mono.error(new IllegalArgumentException("Saldo insuficiente"));
                        }
                    }

                    // Cálculo del nuevo saldo
                    double newBalance = request.getType().equals("DEPOSIT") ?
                            accountBalance + request.getAmount() :
                            accountBalance - request.getAmount();

                    // Guardar la transacción
                    TransactionEntity transaction = TransactionEntity.builder()
                            .accountId(request.getAccountId())
                            .type(request.getType())
                            .amount(request.getAmount())
                            .balanceAfterTransaction(newBalance)
                            .transactionDate(LocalDateTime.now())
                            .build();

                    // Actualizar saldo en AccountService y guardar la transacción
                    return updateAccountBalance(request.getAccountId(), newBalance)
                            .then(transactionRepository.save(transaction))
                            .map(this::convertToDto);
                });
    }

    private TransactionDto convertToDto(TransactionEntity entity) {
        return TransactionDto.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .type(entity.getType())
                .amount(entity.getAmount())
                .balanceAfterTransaction(entity.getBalanceAfterTransaction())
                .transactionDate(entity.getTransactionDate())
                .build();
    }

    private Mono<Void> updateAccountBalance(String accountId, Double newBalance) {
        return accountWebClient.put()
                .uri("/{id}", accountId)
                .bodyValue(new AccountBalanceUpdateRequest(newBalance))
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Override
    public Flux<TransactionDto> getTransactionsByAccountId(String accountId) {
        return transactionRepository.findByAccountId(accountId)
                .map(this::convertToDto);
    }
}
