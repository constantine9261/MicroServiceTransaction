package com.bank.microserviceTransaction.Model.api.trasaction;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountDto {
    private String id;
    private String accountNumber;
    private String customerId;
    private String type;
    private Double balance;
    private Integer maxTransactions;
    private Double monthlyFee;
    private String allowedWithdrawalDate;  // Asegúrate de que el formato de la fecha sea compatible
    private Boolean debitCardLinked;
}
