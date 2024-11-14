package com.bank.microserviceTransaction.Model.api.trasaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceUpdateRequest {

    private Double newBalance; // Nuevo saldo de la cuenta

}
