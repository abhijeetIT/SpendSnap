package com.spendsnap.Controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            model.addAttribute("code", statusCode);

            if (statusCode == 403) {
                model.addAttribute("message", "Access Denied: You are not authorized to access this page.");
                return "error/403_duplicate";
            } else if (statusCode == 404) {
                model.addAttribute("message", "Page not found. Please check the URL.");
                return "error/404";
            } else if (statusCode == 500) {
                model.addAttribute("message", "Oops! Something went wrong on the server.");
                return "error/500";
            } else if(statusCode == 400){
                model.addAttribute("message", "Oops! this is not correct Page not found. Please check the URL");
                return "error/400";
            }
        }

        model.addAttribute("message", "Unexpected error");
        return "error/default";
    }
}