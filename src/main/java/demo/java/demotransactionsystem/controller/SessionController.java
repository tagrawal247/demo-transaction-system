package demo.java.demotransactionsystem.controller;

import demo.java.demotransactionsystem.exception.UnauthorizedException;
import demo.java.demotransactionsystem.model.LoginRequest;
import demo.java.demotransactionsystem.service.SessionService;
import demo.java.demotransactionsystem.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static demo.java.demotransactionsystem.exception.ErrorMessage.LOGIN_ERROR;

@RestController
@RequestMapping("/api/v1")
public class SessionController implements SessionsApi{
    private final SessionService sessionService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    public SessionController(SessionService sessionService, UserService userService, PasswordEncoder passwordEncoder) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }
    @PostMapping("/sessions")
    public ResponseEntity<String> loginUser(LoginRequest loginRequest){
        var user = userService.getUserByEmail(loginRequest.getEmail());
        if(user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new UnauthorizedException(LOGIN_ERROR);
        }
        return ResponseEntity.ok(sessionService.login(loginRequest.getEmail()));
    }
}
