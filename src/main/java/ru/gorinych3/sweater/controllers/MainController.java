package ru.gorinych3.sweater.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.gorinych3.sweater.domain.Message;
import ru.gorinych3.sweater.domain.User;
import ru.gorinych3.sweater.repositories.MessageRepo;
import ru.gorinych3.sweater.services.UserService;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Controller
public class MainController {

    private final MessageRepo messageRepo;
    private final UserService userService;

    @Value("${upload-path}")
    private String uploadPath;

    @Autowired
    public MainController(MessageRepo messageRepo, UserService userService) {
        this.messageRepo = messageRepo;
        this.userService = userService;
    }

    @GetMapping("/")
    public String greeting(Map<String, Object> model) {
        System.out.println("Get /");
        return "greeting";
    }

    @GetMapping("/main")
    public String main(@RequestParam(required = false, defaultValue = "") String filter, Model model) {
        System.out.println("Get Main");
        Iterable<Message> messages;

        if (filter != null && !filter.isEmpty()) {
            messages = messageRepo.findByTag(filter);
        } else {
            messages = messageRepo.findAll();
        }
        model.addAttribute("messages", messages);
        model.addAttribute("filter", filter);
        return "main";
    }

    @PostMapping("/main")
    public String add(
            @AuthenticationPrincipal User user,
            @Valid Message message,
            BindingResult bindingResult,
            @RequestParam("file") MultipartFile file,
            Model model) throws IOException {

        System.out.println("Post Main");
        System.out.println(user.getUsername());
        System.out.println(message.getText());
        System.out.println(bindingResult.hasErrors());

        message.setAuthor(user);
        System.out.println("Post Main Step1");
        if (bindingResult.hasErrors()) {
            System.out.println("Post Main Step2");
            Map<String, String> errorsMap = ControllerUtils.getErrors(bindingResult);
            for (Map.Entry<String, String> entry : errorsMap.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            model.mergeAttributes(errorsMap);
            model.addAttribute("message", message);
        } else {
            System.out.println("Post Main Step3");
            saveFile(message, file);
            System.out.println("Post Main Step4");
            model.addAttribute("message", null);
            messageRepo.save(message);
        }
        Iterable<Message> messages = messageRepo.findAll();
        model.addAttribute("messages", messages);
        return "main";
    }

    private void saveFile(Message message, MultipartFile file) throws IOException {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            System.out.println("file.isEmpty() " + file.isEmpty());
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String fileName = uuidFile + "." + file.getOriginalFilename();
            file.transferTo(new File(uploadPath, fileName));
            message.setFileName(fileName);
        }
    }

    @GetMapping("/user-messages/{user}")
    public String userMessages(@AuthenticationPrincipal User currentUser,
                               @PathVariable User user,
                               @RequestParam(required = false) Message message,
                               Model model) {
        Set<Message> messages = user.getMessages();
        model.addAttribute("userChannel", user);
        model.addAttribute("subscriptionsCount", user.getSubscriptions().size());
        model.addAttribute("subscribersCount", user.getSubscribers().size());
        model.addAttribute("isSubscriber", user.getSubscribers().contains(currentUser));
        model.addAttribute("messages", messages);
        model.addAttribute("message", message);
        model.addAttribute("isCurrentUser", currentUser.equals(user));

        return "userMessages";
    }

    @PostMapping("/user-messages/{user}")
    public String updateMessage(@AuthenticationPrincipal User currentUser,
                                @PathVariable long user,
                                @RequestParam(value = "id") Message message,
                                @RequestParam("text") String text,
                                @RequestParam("tag") String tag,
                                @RequestParam("file") MultipartFile file,
                                Model model) throws IOException {
        System.out.println("Step1");
        if (message.getAuthor().equals(currentUser)) {
            if (text != null && !text.isEmpty()) {
                System.out.println("Step2");
                message.setText(text);
            }
            if (tag != null && !tag.isEmpty()) {
                System.out.println("Step3");
                message.setTag(tag);
            }
            System.out.println("Step4");
            saveFile(message, file);
            System.out.println("Step5");
            messageRepo.save(message);
        }
        return "redirect:/user-messages/" + user;
    }
}


























