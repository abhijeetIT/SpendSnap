package com.spendsnap.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class Home {

    @GetMapping("/")
    public String home() {
        return "index"; // refers to templates/index.html
    }

    @GetMapping("/terms")
    public String terms(){
        return "TermsofService";
    }

    @GetMapping("/privacy")
    public String privacyPage(){
        return "privacy";
    }

}

