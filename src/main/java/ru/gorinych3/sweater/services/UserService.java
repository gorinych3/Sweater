package ru.gorinych3.sweater.services;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.gorinych3.sweater.domain.User;

import java.util.List;
import java.util.Map;

public interface UserService extends UserDetailsService {
    List<User> getAllUsers();

    boolean addUser(User user);

    boolean activateUser(String code);

    void saveUser(User user, String username, Map<String, String> form);

    void updateProfile(User user, String email, String password);
}
