package ru.gorinych3.sweater.services;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.gorinych3.sweater.domain.User;

import java.util.List;

public interface UserService extends UserDetailsService {
    List<User> getAllUsers();

    User saveUser(User user);

}
