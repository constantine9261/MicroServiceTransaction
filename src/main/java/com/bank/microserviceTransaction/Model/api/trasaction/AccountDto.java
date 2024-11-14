package com.bank.microserviceTransaction.Model.api.trasaction;

import lombok.Data;

@Data
public class AccountDto {
    private String id;
    private String accountNumber;
    private String customerId;
    private String type; // "SAVINGS", "CURRENT", "FIXED", etc.
    private Double balance = 0.0; // Valor predeterminado
    private Integer maxTransactions;
    private Double monthlyFee;
    private String allowedWithdrawalDate;
}
