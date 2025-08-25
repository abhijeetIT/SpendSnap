package com.spendsnap.Services.Implementation;

import com.spendsnap.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CoustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.spendsnap.Entities.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Error: user not found by " + email));

        if (Boolean.TRUE.equals(user.getActive())) { //if (user.getActive() != null && user.getActive()) { same
            return User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole())
                    .build();
        } else {
            throw new DisabledException("User account is inactive/Deleted.");
        }
    }
}
