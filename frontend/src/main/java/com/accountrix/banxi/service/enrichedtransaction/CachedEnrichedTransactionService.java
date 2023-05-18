package com.accountrix.banxi.service.enrichedtransaction;

import com.accountrix.banxi.model.plaid.enrichedtransaction.CachedEnrichedTransaction;
import com.accountrix.banxi.model.plaid.enrichedtransaction.CachedEnrichedTransactionRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CachedEnrichedTransactionService {

    private final CachedEnrichedTransactionRepository repository;

    public CachedEnrichedTransactionService(CachedEnrichedTransactionRepository repository) {
        this.repository = repository;
    }

    public CachedEnrichedTransaction save(CachedEnrichedTransaction enrichedTransaction) {
        return this.repository.saveAndFlush(enrichedTransaction);
    }

    public List<CachedEnrichedTransaction> findAllByAccountId(String accountId) {
        return this.repository.findAllByAccountId(accountId);
    }

    public int count() {
        return (int) this.repository.count();
    }

}
