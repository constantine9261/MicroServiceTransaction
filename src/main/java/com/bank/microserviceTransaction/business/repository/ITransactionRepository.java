package com.bank.microserviceTransaction.business.repository;


import com.bank.microserviceTransaction.Model.entity.TransactionEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ITransactionRepository extends
        ReactiveMongoRepository<TransactionEntity, String> {
    Flux<TransactionEntity> findByAccountId(String accountId);
}
