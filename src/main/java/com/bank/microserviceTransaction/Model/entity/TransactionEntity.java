package com.bank.microserviceTransaction.Model.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "transactions")
public class TransactionEntity implements Serializable {

    @Id
    private String id;
    private String accountId;
    private String type; // Puede ser "DEPOSIT" o "WITHDRAWAL"
    private Double amount;
    private Double balanceAfterTransaction;
    private LocalDateTime transactionDate;


}
