package ru.gorinych3.sweater.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import ru.gorinych3.sweater.domain.User;
import ru.gorinych3.sweater.domain.dto.CaptchaResponseDto;
import ru.gorinych3.sweater.services.UserService;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Controller
public class RegistrationController {

    private final static String CAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";

    private final UserService userService;
    private final RestTemplate restTemplate;

    @Value("${recaptcha.secret}")
    private String secret;

    public RegistrationController(UserService userService, RestTemplate restTemplate) {
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/registration")
    public String registration() {
        System.out.println("Get registration");
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(
            @RequestParam("password2") String passwordConfirm,
            @RequestParam("g-recaptcha-response") String captchaResponse,
            @Valid User user,
            BindingResult bindingResult,
            Model model) {
        System.out.println("Post registration");

        String url = String.format(CAPTCHA_URL, secret, captchaResponse);

        CaptchaResponseDto responseDto =
                restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponseDto.class);

        if (!(responseDto != null && responseDto.isSuccess())) {
            model.addAttribute("captchaError", "Fill captcha!");
        }

        boolean isConfirmEmpty = passwordConfirm == null || passwordConfirm.isEmpty();

        if (isConfirmEmpty) {
            model.addAttribute("password2Error", "Password confirmation cannot be empty");
        }
        if (user.getPassword() != null && !user.getPassword().equals(passwordConfirm)) {
            System.out.println("Пароли не равны");
            bindingResult.addError(new FieldError(
                    "user",
                    "password",
                    "Passwords are different!"));
        }

        if (isConfirmEmpty || !Objects.requireNonNull(responseDto).isSuccess() || bindingResult.hasErrors()) {
            System.out.println("Есть ошибки");
            Map<String, String> errors = ControllerUtils.getErrors(bindingResult);

            model.mergeAttributes(errors);
            return "registration";
        }
        if (!userService.addUser(user)) {
            System.out.println("User exists!");
            model.addAttribute("usernameError", "User exists!");
            return "registration";
        }
        return "redirect:/login";
    }

    @GetMapping("/activate/{code}")
    public String activate(Model model, @PathVariable String code) {
        System.out.println("Get /activate/{code}");

        boolean isActivate = userService.activateUser(code);
        String message = "";
        String messageType = "";
        if (isActivate) {
            message = "User successfully activated!";
            messageType = "success";
        } else {
            message = "Activation code is not found!";
            messageType = "danger";
        }
        model.addAttribute("message", message);
        model.addAttribute("messageType", messageType);

        return "/login";
    }
}
