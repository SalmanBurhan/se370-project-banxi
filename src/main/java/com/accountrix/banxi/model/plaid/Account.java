package com.accountrix.banxi.model.plaid;

import com.plaid.client.model.AccountBalance;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.Institution;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Image;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.Nullable;
import java.util.Objects;

public class Account {

    private final AccountBase accountBase;

    public Account(AccountBase accountBase) {
        this.accountBase = accountBase;
    }

    @Nullable private String logo;
    @Nullable private String institutionName;
    @Nullable private String institutionId;
    @Nullable private String institutionColor;

    @Nullable private Institution institution;
    @Nullable private String accessToken;

    @Nullable
    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(@Nullable Institution institution) {
        this.institution = institution;
    }

    @Nullable
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(@Nullable String accessToken) {
        this.accessToken = accessToken;
    }

    public Component getLogo () {
        if (this.logo == null && !InstitutionLogo.exists(this.institutionId)) {
            return new Avatar(Objects.requireNonNullElse(this.institutionName, "?"));
        }
        else if (InstitutionLogo.exists(this.institutionId)) {
            Image icon = new Image();
            icon.setSrc(String.format("data:image/png;base64,%s", InstitutionLogo.valueOf(this.institutionId)));
            return icon;
        }
        else {
            Image icon = new Image();
            icon.setSrc(String.format("data:image/png;base64,%s", this.logo));
            return icon;
        }
    }

    public void setLogo(@Nullable String base64Logo) {
        this.logo = base64Logo;
    }

    @Nullable
    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(@Nullable String institutionName) {
        this.institutionName = institutionName;
    }

    public String getFullName() {
        return this.institutionName == null ? this.accountBase.getName() : String.format("%s %s", this.institutionName, this.accountBase.getName());
    }

    @Nullable
    public String getInstitutionColor() {
        return institutionColor;
    }

    public void setInstitutionColor(@Nullable String institutionColor) {
        this.institutionColor = institutionColor;
    }

    @Nullable
    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(@Nullable String institutionId) {
        this.institutionId = institutionId;
    }

    public String getName() { return this.accountBase.getName(); }

    public String getPersistentAccountId() { return this.accountBase.getPersistentAccountId(); }

    public String getAccountId() { return this.accountBase.getAccountId(); }

    public AccountBalance getBalances() { return this.accountBase.getBalances(); }

    public AccountBase getAccountBase() {
        return accountBase;
    }
}
