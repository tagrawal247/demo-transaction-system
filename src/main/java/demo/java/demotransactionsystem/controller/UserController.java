package demo.java.demotransactionsystem.controller;

import demo.java.demotransactionsystem.model.SignUpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import demo.java.demotransactionsystem.service.UserService;

@RestController
@RequestMapping("/api/v1")
public class UserController implements UsersApi{

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    @PostMapping("/users")
    public ResponseEntity<Void> signUpUser(SignUpRequest signUpRequest){
        userService.signUp(signUpRequest.getName(), signUpRequest.getEmail(), passwordEncoder.encode(signUpRequest.getPassword()));
        return ResponseEntity.ok().build();
    }
}
