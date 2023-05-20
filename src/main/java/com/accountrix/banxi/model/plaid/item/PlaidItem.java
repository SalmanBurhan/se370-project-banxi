package com.accountrix.banxi.model.plaid.item;

import com.accountrix.banxi.model.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "plaid_item")
public class PlaidItem {

    @Id
    @Column(name = "item_id", nullable = false, unique = true)
    private String itemID;

    @Column(name = "access_token", nullable = false, unique = true)
    private String accessToken;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "client_id", nullable = false)
//    @JsonIgnore
    private User linkedUser;

    public PlaidItem() {

    }

    public PlaidItem(String itemID, String accessToken) {
        this.itemID = itemID;
        this.accessToken = accessToken;
    }

    public PlaidItem(String itemID, String accessToken, User linkedUser) {
        this.itemID = itemID;
        this.accessToken = accessToken;
        this.linkedUser = linkedUser;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public User getLinkedUser() {
        return linkedUser;
    }

    public void setLinkedUser(User linkedUser) {
        this.linkedUser = linkedUser;
    }
}
