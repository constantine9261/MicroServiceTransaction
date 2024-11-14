package com.bank.microserviceTransaction.Model.api.trasaction;

import lombok.Data;

@Data
public class TransactionRequest {
    private String accountId;
    private String type; // "DEPOSIT" o "WITHDRAWAL"
    private Double amount;
}
