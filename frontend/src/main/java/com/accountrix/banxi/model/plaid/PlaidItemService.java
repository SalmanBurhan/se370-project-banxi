package com.accountrix.banxi.model.plaid;

import com.accountrix.banxi.model.plaid.item.PlaidItem;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PlaidItemService {

    private final PlaidItemRepository repository;

    private final EntityManager manager;

    public PlaidItemService(PlaidItemRepository repository, EntityManager manager) {
        this.repository = repository;
        this.manager = manager;
    }

//    public Optional<PlaidItem> get(String ID) {
//        return this.repo.findPlaidItemByID(ID);
//    }

    public PlaidItem update(PlaidItem item) {
        return this.create(item); // Create or Update, tbh.
    }

    public PlaidItem create(PlaidItem item) {
        PlaidItem newItem = this.repository.saveAndFlush(item);
        this.manager.refresh(newItem);
        return newItem;
    }

    public void delete(Long id) {
        this.repository.deleteById(id);
    }

    public Page<PlaidItem> list(Pageable pageable) {
        return this.repository.findAll(pageable);
    }

    public Page<PlaidItem> list(Pageable pageable, Specification<PlaidItem> filter) {
        return this.repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) this.repository.count();
    }
}
