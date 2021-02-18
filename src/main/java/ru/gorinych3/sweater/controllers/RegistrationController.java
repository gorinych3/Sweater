package ru.gorinych3.sweater.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.gorinych3.sweater.domain.User;
import ru.gorinych3.sweater.services.UserService;

import java.util.Map;

@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/registration")
    public String registration() {
        System.out.println("Get registration");
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(User user, Map<String, Object> model) {
        System.out.println("Post registration");

        if (!userService.saveUser(user)) {
            System.out.println("User exists!");
            model.put("message", "User exists!");
            return "registration";
        }
        return "redirect:/login";
    }

    @GetMapping("/activate/{code}")
    public String activate(Model model, @PathVariable String code){
        System.out.println("Get /activate/{code}");

        boolean isActivate =  userService.activateUser(code);
        String message = "";
        if (isActivate){
            message = "User successfully activated!";
        } else {
            message = "Activation code is not found!";
        }
        model.addAttribute("message", message);

        return "/login";
    }
}
