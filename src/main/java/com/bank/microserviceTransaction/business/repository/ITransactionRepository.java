package com.bank.microserviceTransaction.business.repository;


import com.bank.microserviceTransaction.Model.entity.TransactionEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ITransactionRepository extends
        ReactiveMongoRepository<TransactionEntity, Long> {

}
