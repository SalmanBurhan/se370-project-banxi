package com.accountrix.banxi.service.user;

import com.accountrix.banxi.model.user.User;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
@Configurable
public class UserService implements AbstractUserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> getUserById(long id) {
        return Optional.ofNullable(this.userRepository.getReferenceById(id));
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return this.userRepository.findOneByEmail(email);
    }

    @Override
    public Collection<User> getAll() {
        return userRepository.findAll(Sort.by("email_address"));
    }

    @Override
    public User create(User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        return this.userRepository.save(user);
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.getUserByEmail(username).orElseGet(null);
    }
}
