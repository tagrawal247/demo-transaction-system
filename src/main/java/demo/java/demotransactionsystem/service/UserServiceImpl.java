package demo.java.demotransactionsystem.service;


import demo.java.demotransactionsystem.exception.UnauthorizedException;
import demo.java.demotransactionsystem.exception.ValidationException;
import demo.java.demotransactionsystem.repository.UserRepository;
import demo.java.demotransactionsystem.security.AuthUserDetails;
import demo.java.demotransactionsystem.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static demo.java.demotransactionsystem.exception.ErrorMessage.USER_ALREADY_EXISTS;


@Service
public class UserServiceImpl implements UserService{

    private UserRepository userRepository;


    public UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public User signUp(String name, String email, String encryptedPassword) {
        if(getUserByEmail(email)!=null){
            throw new ValidationException(USER_ALREADY_EXISTS);
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(encryptedPassword);
        userRepository.save(user);
        return user;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = Optional.ofNullable(userRepository.findByEmail(username)).orElseThrow(()-> new UnauthorizedException("User not found with email: " + username));
        return new AuthUserDetails(user);
    }
}
