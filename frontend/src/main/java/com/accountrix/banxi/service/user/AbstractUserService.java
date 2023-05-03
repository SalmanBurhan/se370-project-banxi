package com.accountrix.banxi.service.user;

import com.accountrix.banxi.model.user.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collection;
import java.util.Optional;

public interface AbstractUserService extends UserDetailsService  {
    Optional<User> getUserById(long id);
    Optional<User> getUserByEmail(String email);
    Collection<User> getAll();
    User create(User user);
}
