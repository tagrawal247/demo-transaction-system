package demo.java.demotransactionsystem.service;

import demo.java.demotransactionsystem.exception.UnauthorizedException;
import demo.java.demotransactionsystem.model.User;
import demo.java.demotransactionsystem.repository.UserRepository;
import demo.java.demotransactionsystem.security.JwtService;
import demo.java.demotransactionsystem.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {


    private JwtService jwtService;

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        sessionService = new SessionServiceImpl(jwtService);
    }

    @Test
    void testLogin() {
        String email = "test@test.com";
        String token = "sampleJWTToken";

        when(jwtService.generateToken(email)).thenReturn(token);

        String generatedToken = sessionService.login(email);

        assertNotNull(generatedToken);
        assertEquals(token, generatedToken);
    }
}
