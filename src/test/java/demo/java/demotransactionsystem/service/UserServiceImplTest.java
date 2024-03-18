package demo.java.demotransactionsystem.service;

import demo.java.demotransactionsystem.exception.UnauthorizedException;
import demo.java.demotransactionsystem.repository.UserRepository;
import demo.java.demotransactionsystem.security.JwtService;
import demo.java.demotransactionsystem.model.User;
import demo.java.demotransactionsystem.util.TestUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void testSignUp() {
        String name = "Test User1";
        String email = "test@test.com";
        String encryptedPassword = "$2a$10$w7xTzXo2P0zrwRnO4tE2XuwAmhYs7b1N.EuZxDZ6POMktASpK3C0K"; // Sample bcrypt encrypted password

        userService.signUp(name, email, encryptedPassword);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetUserByEmail() {
        String email = "john@test.com";
        User user = TestUtils.createNewUser("John Doe", email, "$2a$10$w7xTzXo2P0zrwRnO4tE2XuwAmhYs7b1N.EuZxDZ6POMktASpK3C0K");

        when(userRepository.findByEmail(email)).thenReturn(user);

        User foundUser = userService.getUserByEmail(email);

        assertNotNull(foundUser);
        assertEquals(user, foundUser);
    }

    @Test
    void testLoadUserByUsername() {
        String email = "test@test.com";
        User user = TestUtils.createNewUser("Test User1", email, "$2a$10$w7xTzXo2P0zrwRnO4tE2XuwAmhYs7b1N.EuZxDZ6POMktASpK3C0K");

        when(userRepository.findByEmail(email)).thenReturn(user);

        assertDoesNotThrow(() -> userService.loadUserByUsername(email));
    }

    @Test
    void testGetUserByEmailNotFound() {
        String email = "unknown@test.com";

        when(userRepository.findByEmail(email)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> userService.loadUserByUsername(email));
    }
}
