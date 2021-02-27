package ru.gorinych3.sweater.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.gorinych3.sweater.domain.Role;
import ru.gorinych3.sweater.domain.User;
import ru.gorinych3.sweater.services.UserService;

import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public String userList(Model model) {
        System.out.println("Get /user");
        model.addAttribute("userList", userService.getAllUsers());
        return "userList";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("{user}")
    public String userEditForm(@PathVariable User user, Model model) {
        System.out.println("Get /user/{user}");
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "userEdit";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public String userSave(
            @RequestParam String userName,
            @RequestParam("userId") User user,
            @RequestParam Map<String, String> form,
            Model model) {

        System.out.println("Post /user");
        userService.saveUser(user, userName, form);

        return "redirect:/user";
    }

    @GetMapping("profile")
    public String getProfile(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());

        return "profile";
    }

    @PostMapping("profile")
    public String updateProfile(@AuthenticationPrincipal User user,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String password) {
        userService.updateProfile(user, email, password);

        return "redirect:/user/profile";
    }

    @GetMapping("/subscribe/{user}")
    public String subscribe(@AuthenticationPrincipal User currentUser,
                            @PathVariable("user") User user) {

        userService.subscribe(currentUser, user);
        return "redirect:/user-messages/" + user.getId();
    }

    @GetMapping("/unsubscribe/{user}")
    public String unsubscribe(@AuthenticationPrincipal User currentUser,
                              @PathVariable("user") User user) {

        userService.unsubscribe(currentUser, user);
        return "redirect:/user-messages/" + user.getId();
    }

    @GetMapping("/{type}/{user}/list")
    public String userList(Model model,
                           @PathVariable User user,
                           @PathVariable String type) {

        model.addAttribute("userChannel", user);
        model.addAttribute("type", type);

        if ("subscriptions".equals(type)) {
            model.addAttribute("users", user.getSubscriptions());
        } else {
            model.addAttribute("users", user.getSubscribers());
        }

        return "subscriptions";
    }
}
