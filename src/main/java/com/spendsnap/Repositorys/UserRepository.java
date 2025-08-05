package com.spendsnap.Repositorys;

import com.spendsnap.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,String> {

}
