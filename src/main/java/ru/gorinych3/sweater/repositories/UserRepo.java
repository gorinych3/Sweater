package ru.gorinych3.sweater.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gorinych3.sweater.domain.User;

public interface UserRepo extends JpaRepository<User, Long> {

    User findUserByUsername(String username);

    User findByActivationCode(String code);

    User findUserById(long userId);
}
