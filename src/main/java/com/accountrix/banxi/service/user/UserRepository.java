package com.accountrix.banxi.service.user;

import com.accountrix.banxi.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findOneByEmail(String email);
    //List<User> findAllBy(Pageable pageable);
}
