package demo.java.demotransactionsystem.service;

import demo.java.demotransactionsystem.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface SessionService {
    String login(String email);
}
