package com.spendsnap.Configurations;

import com.spendsnap.Services.categoryService;
import com.spendsnap.Services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfig {

    @Autowired
    private categoryService categoryService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserServices userServices;

    @Bean
    public CommandLineRunner cmdLineRunner(){
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
//                User user = new User();
//                user.setName("Abhijeet Jha");
//                user.setGender("Male");
//                user.setRole("ADMIN");
//                user.setEmail("abhijha4324@gmail.com");
//                String rowPass = "Abhi@123";
//                user.setPassword(passwordEncoder.encode(rowPass));
//                userServices.register(user);

            }
        };
    }
}
