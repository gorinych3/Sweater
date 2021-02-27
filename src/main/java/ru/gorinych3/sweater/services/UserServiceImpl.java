package ru.gorinych3.sweater.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.gorinych3.sweater.domain.Role;
import ru.gorinych3.sweater.domain.User;
import ru.gorinych3.sweater.repositories.UserRepo;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Value("${hostname}")
    private String hostName;

    private final UserRepo userRepo;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepo userRepo, MailService mailService, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userRepo.findUserByUsername(userName);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public boolean addUser(User user) {
        User actualUser = userRepo.findUserByUsername(user.getUsername());

        if (actualUser != null) {
            return false;
        }
        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);

        sendMessage(user);
        return true;
    }

    private void sendMessage(User user) {
        if (!Objects.isNull(user.getEmail()) && !user.getEmail().isEmpty()) {
            String message = String.format(
                    "Hello, %s! \n" +
                            "Welcome to Sweater. Please. visit this link: http://%s/activate/%s",
                    user.getUsername(),
                    hostName,
                    user.getActivationCode());
            mailService.send(user.getEmail(), "Activation code", message);
        }
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

    @Override
    public void saveUser(User user, String username, Map<String, String> form) {
        user.setUsername(username);
        Set<String> roles = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());

        user.getRoles().clear();

        for (String key : form.keySet()) {
            if (roles.contains(key)) {
                user.getRoles().add(Role.valueOf(key));
            }
        }
        userRepo.save(user);
    }

    @Override
    public void updateProfile(User user, String email, String password) {
        String userEmail = user.getEmail();

        boolean isEmailChanged = (email != null && !email.equals(userEmail)) ||
                (userEmail != null && !userEmail.equals(email));

        if (isEmailChanged) {
            user.setEmail(email);

            if (email != null && !email.isEmpty()) {
                user.setActivationCode(UUID.randomUUID().toString());
            }
        }

        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        userRepo.save(user);

        if (isEmailChanged) {
            sendMessage(user);
        }
    }

    @Override
    public User getUserById(long userId) {
        return userRepo.findUserById(userId);
    }

    @Override
    public void subscribe(User currentUser, User user) {
        user.getSubscribers().add(currentUser);
        userRepo.save(user);
    }

    @Override
    public void unsubscribe(User currentUser, User user) {
        user.getSubscribers().remove(currentUser);
        userRepo.save(user);
    }
}
