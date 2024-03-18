package demo.java.demotransactionsystem.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import demo.java.demotransactionsystem.exception.GlobalExceptionHandler;
import demo.java.demotransactionsystem.exception.ValidationException;
import demo.java.demotransactionsystem.model.SignUpRequest;
import demo.java.demotransactionsystem.model.User;
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
class UserControllerTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserService userService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setup() {
        userService = mock(UserService.class);
        UserController userController = new UserController(userService, passwordEncoder);
        objectMapper = JsonMapper.builder()
                .addModule(new Jdk8Module())
                .addModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(userController, new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new JacksonConfig().objectMapper(new Jackson2ObjectMapperBuilder())))
                .build();
    }
    @Test
    void testSignUpUser_Success() throws Exception {
        when(userService.signUp(any(), any(), any())).thenReturn(new User());
        var request = new SignUpRequest();
        request.setName("Test User");
        request.email("test@test.com");
        request.setPassword("password");
        var content = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testSignUpUser_DuplicateEmail() throws Exception {
        when(userService.signUp(any(), any(), any())).thenThrow(new ValidationException(USER_ALREADY_EXISTS));
        var request = new SignUpRequest();
        request.setName("Test User");
        request.email("test@test.com");
        request.setPassword("password");
        var content = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .secure(false))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("User already exists with this email Id"));
    }
}



