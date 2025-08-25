package com.spendsnap.Services.Implementation;

import com.spendsnap.Entities.User;
import com.spendsnap.Repositories.UserRepository;
import com.spendsnap.Services.UserServices;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImp implements UserServices {

    @Autowired
    private UserRepository userRepo;

    @Override
    public boolean register(User user) {
        if (userRepo.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        } else {
            userRepo.save(user);
            return true;
        }
    }

    //for getting a user
    @Override
    public User getUserObject(Long identity) {
        return userRepo.findById(identity)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + identity));
    }

    @Override
    public Boolean updateUser(User user) {
        try{
            userRepo.save(user);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public Page<User> getUsers(int page, int size) {
        return userRepo.findAll(PageRequest.of(page, size));
    }

    public Page<User> searchUsers(String q, int page, int size) {
        return userRepo.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, PageRequest.of(page, size));
    }

    @Override
    public Long totalUser() {
        return userRepo.count();
    }

    @Override
    public Long totalInactiveUser() {
        return userRepo.countInactiveUsers();
    }

    @Override
    public Long totalActiveUser() {
        return userRepo.countActiveUsers();
    }

    @Override
    public List<User> allUser() {
        return userRepo.findAll();
    }

    @Override
    public boolean toggleActive(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        try {
            user.setActive(!user.getActive());
            userRepo.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean toggleRole(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
       try {
           user.setRole(user.getRole().equals("ADMIN") ? "USER" : "ADMIN");
           userRepo.save(user);
           return true;
       } catch (Exception e) {
           return false;
       }
    }

}
