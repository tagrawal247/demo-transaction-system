package demo.java.demotransactionsystem.service;


import demo.java.demotransactionsystem.exception.UnauthorizedException;
import demo.java.demotransactionsystem.exception.ValidationException;
import demo.java.demotransactionsystem.model.User;
import demo.java.demotransactionsystem.repository.UserRepository;
import demo.java.demotransactionsystem.security.AuthUserDetails;
import demo.java.demotransactionsystem.security.JwtService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static demo.java.demotransactionsystem.exception.ErrorMessage.USER_ALREADY_EXISTS;


@Service
public class SessionServiceImpl implements SessionService{

    private JwtService jwtService;


    public SessionServiceImpl(JwtService jwtService){;
        this.jwtService = jwtService;
    }
    @Override
    public String login(String email) {
        return jwtService.generateToken(email);
    }
}
