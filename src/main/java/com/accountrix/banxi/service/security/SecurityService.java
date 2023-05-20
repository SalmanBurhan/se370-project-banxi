package com.accountrix.banxi.service.security;

import com.accountrix.banxi.model.user.User;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {

    private final AuthenticationContext authenticationContext;

    public SecurityService(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public User getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(User.class).get();
    }

    public boolean isLoggedIn() { return authenticationContext.isAuthenticated(); }

    public void logout() {
        authenticationContext.logout();
    }
}