package com.bank.microserviceTransaction.controller;


import com.bank.microserviceTransaction.Model.api.shared.ResponseDto;
import com.bank.microserviceTransaction.Model.api.shared.ResponseDtoBuilder;
import com.bank.microserviceTransaction.Model.api.trasaction.TransactionDto;
import com.bank.microserviceTransaction.Model.api.trasaction.TransactionRequest;
import com.bank.microserviceTransaction.business.service.ITransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    @Autowired
    private final ITransactionService transactionService;
    @Operation(summary = "Registrar una transacción", description = "Registra una nueva transacción en la cuenta proporcionada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transacción registrada con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public Mono<ResponseDto<TransactionDto>> registerTransaction(@RequestBody TransactionRequest request) {
        return transactionService.registerTransaction(request)
                .map(transaction -> ResponseDtoBuilder.success(transaction, "Transacción registrada con éxito"))
                .onErrorResume(error -> Mono.just(ResponseDtoBuilder.error(error.getMessage())));
    }
    @Operation(summary = "Obtener transacciones por ID de cuenta", description = "Recupera todas las transacciones asociadas con una cuenta específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transacción encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Transacción no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{accountId}")
    public Flux<ResponseDto<TransactionDto>> getTransactionsByAccountId(@PathVariable String accountId) {
        return transactionService.getTransactionsByAccountId(accountId)
                .map(transaction -> ResponseDtoBuilder.success(transaction, "Transacción encontrada"));
    }


    @PostMapping("/transfer")
    public Mono<ResponseDto<Void>> transferBetweenAccounts(@RequestBody TransactionRequest request) {
        return transactionService.transferBetweenAccounts(request)
                .then(Mono.just(ResponseDtoBuilder.success((Void) null, "Transferencia realizada con éxito")))
                .onErrorResume(error -> {
                    String errorMessage = error instanceof Throwable
                            ? error.getMessage()
                            : "Error desconocido";
                    return Mono.just(ResponseDtoBuilder.error(errorMessage));
                });
    }

}
