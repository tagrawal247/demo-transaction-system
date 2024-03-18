package demo.java.demotransactionsystem.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import demo.java.demotransactionsystem.exception.GlobalExceptionHandler;
import demo.java.demotransactionsystem.exception.UnauthorizedException;
import demo.java.demotransactionsystem.exception.ValidationException;
import demo.java.demotransactionsystem.model.AccountDetails;
import demo.java.demotransactionsystem.model.CreateAccountRequest;
import demo.java.demotransactionsystem.model.TransactionDetails;
import demo.java.demotransactionsystem.model.TransactionRequest;
import demo.java.demotransactionsystem.security.JwtService;
import demo.java.demotransactionsystem.service.AccountService;
import demo.java.demotransactionsystem.service.AccountServiceImpl;
import demo.java.demotransactionsystem.util.JacksonConfig;
import demo.java.demotransactionsystem.util.Utility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static demo.java.demotransactionsystem.exception.ErrorMessage.USER_UNAUTHORIZED_TO_TRANSFER_AMOUNT_IBAN_MISMATCH;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private AccountService accountService;

    private JwtService jwtService;

    @BeforeEach
    void setup() {
        accountService = mock(AccountServiceImpl.class);
        jwtService = mock(JwtService.class);
        AccountController accountController = new AccountController(accountService, jwtService);
        objectMapper = JsonMapper.builder()
                .addModule(new Jdk8Module())
                .addModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(accountController, new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new JacksonConfig().objectMapper(new Jackson2ObjectMapperBuilder())))
                .build();
    }

    @Test
    void testCreateAccountSuccess() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail("test@test.com");
        request.setName("Test User");
        request.setOpeningBalance(BigDecimal.valueOf(1000));

        var content = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);
        AccountDetails expectedAccountDetails = new AccountDetails();
        expectedAccountDetails.setEmailId("test@test.com");
        expectedAccountDetails.setName("Test User");
        expectedAccountDetails.setBalance(BigDecimal.valueOf(1000));

        when(jwtService.getUserNameFromJWT()).thenReturn("test@test.com");
        when(accountService.createAccount("test@test.com", "Test User", BigDecimal.valueOf(1000))).thenReturn(expectedAccountDetails);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.emailId").value("test@test.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test User"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(1000));
    }

    @Test
    void testCreateAccountUserUnauthorized() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail("test@test.com");
        request.setName("test");
        request.setOpeningBalance(new BigDecimal(1000));
        var content = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);
        when(jwtService.getUserNameFromJWT()).thenReturn("test2@test.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testCreateAccountNegativeAmount() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail("test@test.com");
        request.setName("test");
        request.setOpeningBalance(new BigDecimal(-100));
        var content = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid Request - Field Name: openingBalance - Error Message: must be greater than or equal to 0"));
    }


    @Test
    void testRetrieveAccount_Success() throws Exception {
        AccountDetails expectedAccountDetails = new AccountDetails();
        expectedAccountDetails.setEmailId("test@test.com");

        when(jwtService.getUserNameFromJWT()).thenReturn("test@test.com");
        when(accountService.retrieveAccountDetails("testAccountId", "test@test.com")).thenReturn(expectedAccountDetails);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/accounts/testAccountId"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.emailId").value("test@test.com"));
    }

    @Test
    void testRetrieveAccountUnauthorized() throws Exception {
        when(jwtService.getUserNameFromJWT()).thenReturn("another@test.com");
        when(accountService.retrieveAccountDetails("testAccountId", "another@test.com")).thenThrow(new UnauthorizedException("AccountId does not belong to the requesting User"));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/accounts/testAccountId"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testRetrieveAllAccountSuccess() throws Exception {
        var account = new AccountDetails();
        account.setIban(Utility.generateIban());
        account.balance(BigDecimal.TEN);
        account.setEmailId("test@test.com");
        List<AccountDetails> expectedAccountList = Collections.singletonList(account);

        when(jwtService.getUserNameFromJWT()).thenReturn("test@test.com");
        when(accountService.retrieveAllAccounts("test@test.com")).thenReturn(expectedAccountList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/accounts")
                        .secure(false))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].emailId").value("test@test.com"));
    }

    @Test
    void testTransferAmountSuccess() throws Exception {
        TransactionRequest request = new TransactionRequest();
        var senderIBAN = Utility.generateIban();
        var receiverIBAN = Utility.generateIban();
        request.setSenderIban(senderIBAN);
        request.setReceiverIban(receiverIBAN);
        request.setAmount(BigDecimal.valueOf(500));
        var content = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);
        TransactionDetails expectedTransactionDetails = new TransactionDetails();
        expectedTransactionDetails.setStatus("SUCCESS");

        when(jwtService.getUserNameFromJWT()).thenReturn("test@test.com");
        when(accountService.transferAmount(senderIBAN, receiverIBAN, BigDecimal.valueOf(500), "test@test.com")).thenReturn(expectedTransactionDetails);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void testTransferAmountUnauthorized() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setSenderIban(Utility.generateIban());
        request.setReceiverIban(Utility.generateIban());
        request.setAmount(new BigDecimal("1000.0"));
        var content = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);
        when(jwtService.getUserNameFromJWT()).thenReturn("another@test.com");
        when(accountService.transferAmount(request.getSenderIban(), request.getReceiverIban(), request.getAmount(), "another@test.com"))
                .thenThrow(new ValidationException(USER_UNAUTHORIZED_TO_TRANSFER_AMOUNT_IBAN_MISMATCH));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Sender IBAN does not belong to the requesting User"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-100", "0"})
    void testTransferAmountIncorrectAmount(String amount) throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setSenderIban(Utility.generateIban());
        request.setReceiverIban(Utility.generateIban());
        request.setAmount(new BigDecimal(amount));
        var content = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid Request - Field Name: amount - Error Message: must be greater than 0"));
    }
    @Test
    void testTransferAmountIncorrectIban() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setSenderIban(Utility.generateIban());
        request.setReceiverIban("testIban");
        request.setAmount(BigDecimal.TEN);
        var content = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid Request - Field Name: receiverIban - Error Message: must match \"^[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{10}$\""));
    }
}





