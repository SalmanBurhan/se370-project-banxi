package com.accountrix.banxi.model.plaid.enrichedtransaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CachedEnrichedTransactionRepository extends JpaRepository<CachedEnrichedTransaction, String>, JpaSpecificationExecutor<CachedEnrichedTransaction> {
    @Query("select t from CachedEnrichedTransaction t where t.accountId = ?1")
    List<CachedEnrichedTransaction> findAllByAccountId(String accountId);
}
