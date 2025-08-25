package com.spendsnap.Services;

import com.spendsnap.Entities.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface UserServices {
    boolean register(User user);
    User getUserObject(Long identity);

    Boolean updateUser(User user);

    Long totalUser();

    Long totalInactiveUser();

    Long totalActiveUser();

    List<User> allUser();

    //==========services for admin page========================================================
   boolean  toggleActive(Long id); //focus on this for update and version improve
   boolean toggleRole(Long id);

    public Page<User> searchUsers(String q, int page, int size);
    public Page<User> getUsers(int page, int size);
}
