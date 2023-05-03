package com.accountrix.banxi.model.plaid;

import com.accountrix.banxi.model.plaid.item.PlaidItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PlaidItemService {

    private final PlaidItemRepository repository;

    public PlaidItemService(PlaidItemRepository repository) {
        this.repository = repository;
    }

//    public Optional<PlaidItem> get(String ID) {
//        return this.repo.findPlaidItemByID(ID);
//    }

    public PlaidItem update(PlaidItem item) {
        return this.repository.save(item);
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
