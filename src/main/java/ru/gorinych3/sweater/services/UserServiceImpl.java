package ru.gorinych3.sweater.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.gorinych3.sweater.domain.Role;
import ru.gorinych3.sweater.domain.User;
import ru.gorinych3.sweater.repositories.UserRepo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final MailService mailService;

    @Autowired
    public UserServiceImpl(UserRepo userRepo, MailService mailService) {
        this.userRepo = userRepo;
        this.mailService = mailService;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        return userRepo.findUserByUsername(userName);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public boolean saveUser(User user) {
        User actualUser = userRepo.findUserByUsername(user.getUsername());

        if (actualUser != null) {
            return false;
        }
        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        userRepo.save(user);

        if (!Objects.isNull(user.getEmail()) && !user.getEmail().isEmpty()) {
            String message = String.format(
                    "Hello, %s! \n" +
                            "Welcome to Sweater. Please. visit this link: http://localhost:8080/activate/%s",
                    user.getUsername(),
                    user.getActivationCode());
            mailService.send(user.getEmail(), "Activation code", message);
        }
        return true;
    }

    @Override
    public boolean activateUser(String code) {
        User user = userRepo.findByActivationCode(code);

        if (user == null) {
            return false;
        }

        user.setActivationCode(null);
        userRepo.save(user);

        return true;
    }
}
