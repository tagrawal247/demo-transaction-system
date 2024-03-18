package demo.java.demotransactionsystem.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import demo.java.demotransactionsystem.exception.GlobalExceptionHandler;
import demo.java.demotransactionsystem.exception.ValidationException;
import demo.java.demotransactionsystem.model.LoginRequest;
import demo.java.demotransactionsystem.model.SignUpRequest;
import demo.java.demotransactionsystem.model.User;
import demo.java.demotransactionsystem.service.SessionService;
import demo.java.demotransactionsystem.service.UserService;
import demo.java.demotransactionsystem.util.JacksonConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static demo.java.demotransactionsystem.exception.ErrorMessage.USER_ALREADY_EXISTS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private SessionService sessionService;
    private UserService userService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setup() {
        userService = mock(UserService.class);
        sessionService= mock(SessionService.class);
        SessionController sessionController = new SessionController(sessionService, userService, passwordEncoder);
        objectMapper = JsonMapper.builder()
                .addModule(new Jdk8Module())
                .addModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(sessionController, new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new JacksonConfig().objectMapper(new Jackson2ObjectMapperBuilder())))
                .build();
    }
    @Test
    void testLoginUser_Success() throws Exception {
        String jwtToken = "mockedJWTToken";
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setName("test");
        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(sessionService.login("test@test.com")).thenReturn(jwtToken);
        var request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");
        var content = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .secure(false))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("\"mockedJWTToken\""));
    }

    @Test
    void testLoginUser_WrongPassword() throws Exception {
        User user = new User();
        user.setEmail("test1@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        when(userService.getUserByEmail("test1@test.com")).thenReturn(user);

        var request = new LoginRequest();
        request.setEmail("test1@test.com");
        request.setPassword("wrongpassword");
        var content = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Either email or password is incorrect"));;
    }

    @Test
    void testLoginUser_UserNotFound() throws Exception {
        when(userService.getUserByEmail("unknown@test.com")).thenReturn(null);
        var request = new LoginRequest();
        request.setEmail("unknown@test.com");
        request.setPassword("password");
        var content = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .secure(false))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Either email or password is incorrect"));
    }
}



