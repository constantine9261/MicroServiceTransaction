package com.bank.microserviceTransaction.Model.api.trasaction;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDto {
    private String id;
    private String accountId;
    private String type;
    private Double amount;
    private Double balanceAfterTransaction;
    private LocalDateTime transactionDate;
}
