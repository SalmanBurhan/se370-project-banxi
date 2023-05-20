package com.accountrix.banxi.model.plaid.enrichedtransaction;

import com.google.gson.Gson;
import com.plaid.client.model.ClientProvidedEnrichedTransaction;
import com.plaid.client.model.EnrichTransactionDirection;
import com.plaid.client.model.Enrichments;
import jakarta.persistence.*;

@Entity
@Table(name = "enriched_transaction")
public class CachedEnrichedTransaction {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "direction", nullable = false)
    @Enumerated(EnumType.STRING)
    private EnrichTransactionDirection direction;

    @Column(name = "iso_currency_code", nullable = false)
    private String isoCurrencyCode;

    @Column(name = "enrichments", columnDefinition = "JSON", nullable = false)
    private String enrichmentsJSON;

    @Transient
    private Enrichments enrichments;

    @PostLoad
    void filleTransient() {
        Gson gson = new Gson();
        enrichments = gson.fromJson(this.enrichmentsJSON, Enrichments.class);
    }

    @PrePersist
    void fillPersistent() {
        if (enrichments != null) {
            Gson gson = new Gson();
            enrichmentsJSON = gson.toJson(enrichments);
        }
    }

    public CachedEnrichedTransaction() {}

    public CachedEnrichedTransaction(ClientProvidedEnrichedTransaction enrichedTransaction, String accountId) {
        setId(enrichedTransaction.getId());
        setDescription(enrichedTransaction.getDescription());
        setAmount(enrichedTransaction.getAmount());
        setDirection(enrichedTransaction.getDirection());
        setIsoCurrencyCode(enrichedTransaction.getIsoCurrencyCode());
        setEnrichments(enrichedTransaction.getEnrichments());
        setAccountId(accountId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public EnrichTransactionDirection getDirection() {
        return direction;
    }

    public void setDirection(EnrichTransactionDirection direction) {
        this.direction = direction;
    }

    public String getIsoCurrencyCode() {
        return isoCurrencyCode;
    }

    public void setIsoCurrencyCode(String isoCurrencyCode) {
        this.isoCurrencyCode = isoCurrencyCode;
    }

    public Enrichments getEnrichments() {
        return enrichments;
    }

    public void setEnrichments(Enrichments enrichments) {
        this.enrichments = enrichments;
        fillPersistent();
    }
}
