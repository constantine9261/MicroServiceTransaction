package com.bank.microserviceTransaction.business.service;


import com.bank.microserviceTransaction.Model.api.trasaction.TransactionDto;
import com.bank.microserviceTransaction.Model.api.trasaction.TransactionRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ITransactionService {
    /**
     * Registra una nueva transacción para una cuenta.
     *
     * @param request La solicitud de transacción que contiene el ID de la cuenta, el tipo de transacción (DEPOSIT o WITHDRAWAL), y el monto.
     * @return Un Mono que emite el DTO de la transacción registrada.
     */
    Mono<TransactionDto> registerTransaction(TransactionRequest request);

    /**
     * Obtiene todas las transacciones para una cuenta específica.
     *
     * @param accountId El ID de la cuenta para la cual se desean obtener las transacciones.
     * @return Un Flux que emite los DTOs de las transacciones asociadas con la cuenta especificada.
     */
    Flux<TransactionDto> getTransactionsByAccountId(String accountId);

    Mono<Void> transferBetweenAccounts(TransactionRequest request);
}