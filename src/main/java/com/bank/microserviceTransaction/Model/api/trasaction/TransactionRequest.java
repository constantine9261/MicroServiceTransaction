package com.bank.microserviceTransaction.Model.api.trasaction;

import lombok.Data;

@Data
public class TransactionRequest {
    private String accountId;       // ID de la cuenta origen
    private String type;            // "DEPOSIT", "WITHDRAWAL", o "TRANSFER"
    private Double amount;          // Monto de la transacción
    private String targetAccountId; // (Opcional) ID de la cuenta destino en caso de transferencia
}
