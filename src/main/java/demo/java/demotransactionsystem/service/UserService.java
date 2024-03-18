package demo.java.demotransactionsystem.service;

import demo.java.demotransactionsystem.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    User signUp(String name, String email, String password);

    User getUserByEmail(String email);

    UserDetails loadUserByUsername(String username);

}
