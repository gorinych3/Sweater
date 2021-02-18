package ru.gorinych3.sweater.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import ru.gorinych3.sweater.domain.Role;
import ru.gorinych3.sweater.domain.User;
import ru.gorinych3.sweater.repositories.UserRepo;

import java.util.Collections;
import java.util.Map;

@Controller
public class RegistrationController {

    private final UserRepo userRepo;

    public RegistrationController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/registration")
    public String registration(){
        System.out.println("Get registration");
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(User user, Map<String, Object> model){
        System.out.println("Post registration");
        User actualUser = userRepo.findUserByUsername(user.getUsername());

        if (actualUser != null){
            System.out.println("User exists!");
            model.put("message", "User exists!");
            return "registration";
        }

        System.out.println(user.getUsername() + " " + user.getPassword());

        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        userRepo.save(user);
        return "redirect:/login";
    }
}
