package com.bank.microserviceTransaction.business.service.impl;

import com.bank.microserviceTransaction.Model.api.shared.ResponseDto;
import com.bank.microserviceTransaction.Model.api.trasaction.AccountBalanceUpdateRequest;
import com.bank.microserviceTransaction.Model.api.trasaction.AccountDto;
import com.bank.microserviceTransaction.Model.api.trasaction.TransactionDto;
import com.bank.microserviceTransaction.Model.api.trasaction.TransactionRequest;
import com.bank.microserviceTransaction.Model.entity.TransactionEntity;
import com.bank.microserviceTransaction.business.repository.ITransactionRepository;
import com.bank.microserviceTransaction.business.service.ITransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class TransactionServiceImpl implements ITransactionService {

    @Autowired
    private final ITransactionRepository transactionRepository;

    @Autowired
    private final WebClient accountWebClient;

    @Override
    public Mono<TransactionDto> registerTransaction(TransactionRequest request) {
        return accountWebClient.get()
                .uri("/{id}", request.getAccountId())  // Usamos el accountId desde la solicitud
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseDto<AccountDto>>() {}) // Deserializamos la respuesta en ResponseDto<AccountDto>
                .flatMap(responseDto -> {
                    // Obtenemos la cuenta desde el ResponseDto
                    AccountDto account = responseDto.getData();

                    // Verificamos si la cuenta existe
                    if (account == null) {
                        return Mono.error(new IllegalArgumentException("Cuenta no encontrada"));
                    }

                    Double accountBalance = account.getBalance();
                    if (accountBalance == null) {
                        return Mono.error(new IllegalArgumentException("Balance de la cuenta no disponible"));
                    }

                    // Validación de los límites de movimiento para una cuenta de ahorro
                    if ("SAVINGS".equals(account.getType()) && request.getType().equals("WITHDRAWAL")) {
                        if (accountBalance < request.getAmount()) {
                            return Mono.error(new IllegalArgumentException("Saldo insuficiente"));
                        }
                    }

                    // Calculamos el nuevo saldo dependiendo del tipo de transacción
                    double newBalance = request.getType().equals("DEPOSIT") ?
                            accountBalance + request.getAmount() :
                            accountBalance - request.getAmount();

                    // Creamos la transacción
                    TransactionEntity transaction = TransactionEntity.builder()
                            .accountId(request.getAccountId())
                            .type(request.getType())
                            .amount(request.getAmount())
                            .balanceAfterTransaction(newBalance)
                            .transactionDate(LocalDateTime.now())
                            .build();

                    // Actualizamos el saldo en la cuenta y guardamos la transacción
                    return updateAccountBalance(request.getAccountId(), newBalance)
                            .then(transactionRepository.save(transaction))
                            .map(this::convertToDto)  // Convertimos la entidad en un DTO
                            .map(transactionDto -> {
                                // Aquí seguimos devolviendo un TransactionDto, sin cambiar el contrato
                                return transactionDto;  // Se devuelve directamente el DTO, no el ResponseDto
                            });
                })
                // Si ocurre algún error (por ejemplo, la cuenta no se encuentra), devolver un error con el mensaje adecuado
                .onErrorResume(e -> Mono.error(new IllegalArgumentException("Error al registrar la transacción: " + e.getMessage())));
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


    @Override
    public Mono<Void> transferBetweenAccounts(TransactionRequest request) {
        // Validaciones
        if (!"TRANSFER".equalsIgnoreCase(request.getType()) || request.getAmount() <= 0) {
            return Mono.error(new IllegalArgumentException("Tipo de transacción debe ser 'TRANSFER' y monto mayor a cero"));
        }

        // Obtener cuenta origen y destino
        Mono<ResponseDto<AccountDto>> fromAccountMono = accountWebClient.get()
                .uri("/{id}", request.getAccountId())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseDto<AccountDto>>() {});

        Mono<ResponseDto<AccountDto>> toAccountMono = accountWebClient.get()
                .uri("/{id}", request.getTargetAccountId())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseDto<AccountDto>>() {});

        return Mono.zip(fromAccountMono, toAccountMono)
                .flatMap(tuple -> {
                    AccountDto fromAccount = tuple.getT1().getData();  // Extraemos los datos del objeto 'data'
                    AccountDto toAccount = tuple.getT2().getData();

                    // Verificar cuentas y saldo suficiente
                    if (fromAccount == null || toAccount == null || fromAccount.getBalance() < request.getAmount()) {
                        return Mono.error(new IllegalArgumentException("Cuenta origen o destino no encontrada, o saldo insuficiente"));
                    }

                    // Calcular nuevos saldos
                    double newFromBalance = fromAccount.getBalance() - request.getAmount();
                    double newToBalance = toAccount.getBalance() + request.getAmount();

                    // Actualizar cuentas
                    Mono<Void> updateFromAccount = accountWebClient.put()
                            .uri("/{id}", fromAccount.getId())
                            .bodyValue(new AccountBalanceUpdateRequest(newFromBalance))
                            .retrieve()
                            .bodyToMono(Void.class);

                    Mono<Void> updateToAccount = accountWebClient.put()
                            .uri("/{id}", toAccount.getId())
                            .bodyValue(new AccountBalanceUpdateRequest(newToBalance))
                            .retrieve()
                            .bodyToMono(Void.class);

                    // Crear la transacción usando el constructor generado por Lombok
                    TransactionEntity transaction = TransactionEntity.builder()
                            .accountId(request.getAccountId())
                            .type("TRANSFER")
                            .amount(request.getAmount())
                            .balanceAfterTransaction(newFromBalance)
                            .transactionDate(LocalDateTime.now())
                            .build();

                    // Guardar la transacción
                    Mono<Void> saveTransaction = transactionRepository.save(transaction).then();

                    // Ejecutar todo en secuencia
                    return updateFromAccount.then(updateToAccount).then(saveTransaction);
                })
                .onErrorResume(error -> Mono.error(new RuntimeException("Error en la transferencia", error)));
    }


}
