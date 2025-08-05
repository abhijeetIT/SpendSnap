package com.spendsnap.Controllers;


import com.spendsnap.Entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index"; // refers to templates/index.html
    }

    @GetMapping("/signUp")
    public String signUpPage(Model model){
       model.addAttribute("user",new User());
        return "Authentication/signup";
    }

    @PostMapping("/register")
    public String registration(){


        return "index";
    }


    @GetMapping("/signIn")
    public String loginPage(){
        return "Authentication/signIn";
    }
}

