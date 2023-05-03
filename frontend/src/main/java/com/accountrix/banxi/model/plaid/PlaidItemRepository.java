package com.accountrix.banxi.model.plaid;

import com.accountrix.banxi.model.plaid.item.PlaidItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PlaidItemRepository extends JpaRepository<PlaidItem, Long>, JpaSpecificationExecutor<PlaidItem> {
    //Optional<List<PlaidItem>> findPlaidItemsByClientId(String clientID);
    //Optional<PlaidItem> findPlaidItemByID(String ID);
}
