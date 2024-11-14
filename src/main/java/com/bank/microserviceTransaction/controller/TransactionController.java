package com.bank.microserviceTransaction.controller;


import com.bank.microserviceTransaction.Model.api.shared.ResponseDto;
import com.bank.microserviceTransaction.Model.api.shared.ResponseDtoBuilder;
import com.bank.microserviceTransaction.Model.api.trasaction.TransactionDto;
import com.bank.microserviceTransaction.Model.api.trasaction.TransactionRequest;
import com.bank.microserviceTransaction.business.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    @Autowired
    private final ITransactionService transactionService;

    @PostMapping
    public Mono<ResponseDto<TransactionDto>> registerTransaction(@RequestBody TransactionRequest request) {
        return transactionService.registerTransaction(request)
                .map(transaction -> ResponseDtoBuilder.success(transaction, "Transacción registrada con éxito"))
                .onErrorResume(error -> Mono.just(ResponseDtoBuilder.error(error.getMessage())));
    }

    @GetMapping("/{accountId}")
    public Flux<ResponseDto<TransactionDto>> getTransactionsByAccountId(@PathVariable String accountId) {
        return transactionService.getTransactionsByAccountId(accountId)
                .map(transaction -> ResponseDtoBuilder.success(transaction, "Transacción encontrada"));
    }
}
