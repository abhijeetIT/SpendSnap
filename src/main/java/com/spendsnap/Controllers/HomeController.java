package com.spendsnap.Controllers;


import com.spendsnap.Entities.Category;
import com.spendsnap.Entities.User;
import com.spendsnap.Services.categoryService;
import com.spendsnap.Services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {

    @Autowired
    private UserServices userServices;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home() {
        return "index"; // refers to templates/index.html
    }

    @GetMapping("/signUp")
    public String signUpPage(Model model){
       model.addAttribute("user",new User());
        return "Authentication/signup";
    }

    @PostMapping("/signUp")
    public String registration(@ModelAttribute("user") User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (userServices.register(user)) {
            return "redirect:/signIn?created";
        } else {
            return "error/500";
        }
    }

    @GetMapping("/signIn")
    public String loginPage(){
        return "Authentication/signIn";
    }

    @Autowired
    private categoryService categoryService;


    @GetMapping("/test")
    public String test(Model model) {
        Category category = categoryService.getCategory(4L)
                .orElse(null); // Convert Optional to Category or null
        model.addAttribute("category", category);
        return "test";
    }
}

