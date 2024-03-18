package demo.java.demotransactionsystem.service;

import demo.java.demotransactionsystem.exception.ValidationException;
import demo.java.demotransactionsystem.model.Account;
import demo.java.demotransactionsystem.model.AccountDetails;
import demo.java.demotransactionsystem.model.TransactionDetails;
import demo.java.demotransactionsystem.repository.AccountRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import demo.java.demotransactionsystem.util.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {


    private AccountRepository accountRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        accountService = new AccountServiceImpl(accountRepository);
    }


    @Test
    void testCreateAccount() {
        String email = "test@test.com";
        String name = "test user1";
        BigDecimal openingBalance = BigDecimal.valueOf(1000);
        String iban = "generated_iban";

        when(accountRepository.save(any())).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setIban(iban);
            return account;
        });

        AccountDetails accountDetails = accountService.createAccount(email, name, openingBalance);

        assertNotNull(accountDetails);
        assertEquals(name, accountDetails.getName());
        assertEquals(email, accountDetails.getEmailId());
        assertEquals(openingBalance, accountDetails.getBalance());
        assertEquals(iban, accountDetails.getIban());
    }



    @Test
    void testRetrieveAllAccounts() {
        String email = "test@test.com";
        List<Account> accounts = new ArrayList<>();
        accounts.add(TestUtils.createNewAccount("test user1", email, BigDecimal.valueOf(1000), "iban1"));
        accounts.add(TestUtils.createNewAccount("test user2", email, BigDecimal.valueOf(2000), "iban2"));

        when(accountRepository.findAllByEmail(email)).thenReturn(accounts);

        List<AccountDetails> accountDetailsList = accountService.retrieveAllAccounts(email);

        assertNotNull(accountDetailsList);
        assertEquals(accounts.size(), accountDetailsList.size());
    }

    @Test
    void testRetrieveAccountDetails() {
        String email = "test@test.com";
        String iban = "iban1";
        Account account = TestUtils.createNewAccount("test user1", email, BigDecimal.valueOf(1000), iban);
        var accountId = account.getId();
        when(accountRepository.findByEmailAndId(email, accountId)).thenReturn(account);
        AccountDetails accountDetails = accountService.retrieveAccountDetails(accountId, email);
        assertNotNull(accountDetails);
        assertEquals("test user1", accountDetails.getName());
        assertEquals(email, accountDetails.getEmailId());
        assertEquals(BigDecimal.valueOf(1000), accountDetails.getBalance());
        assertEquals(iban, accountDetails.getIban());
    }

    @Test
    void testTransferAmountSuccess() {
        String senderIban = "senderIban";
        String receiverIban = "receiverIban";
        BigDecimal transferAmount = BigDecimal.valueOf(500);
        String senderEmail = "test@test.com";

        Account senderAccount = TestUtils.createNewAccount("test user1", senderEmail, BigDecimal.valueOf(1000), senderIban);
        Account receiverAccount = TestUtils.createNewAccount("test user2", "receiver@test.com", BigDecimal.ZERO, receiverIban);

        when(accountRepository.findByEmailAndIban(senderEmail, senderIban)).thenReturn(senderAccount);
        when(accountRepository.findByIban(receiverIban)).thenReturn(receiverAccount);

        TransactionDetails transactionDetails = accountService.transferAmount(senderIban, receiverIban, transferAmount, senderEmail);
        assertEquals(BigDecimal.valueOf(500), senderAccount.getBalance());
        assertEquals(BigDecimal.valueOf(500), receiverAccount.getBalance());
        assertNotNull(transactionDetails);
        assertEquals(senderIban, transactionDetails.getSenderIban());
        assertEquals(receiverIban, transactionDetails.getReceiverIban());
        assertEquals(transferAmount, transactionDetails.getAmount());
        assertEquals("SUCCESS", transactionDetails.getStatus());
    }

    @Test
    void testTransferAmountInsufficientBalance() {
        String senderIban = "senderIban";
        String receiverIban = "receiverIban";
        BigDecimal transferAmount = BigDecimal.valueOf(1500);
        String senderEmail = "test@test.com";

        Account senderAccount = TestUtils.createNewAccount("test user1", senderEmail, BigDecimal.valueOf(1000), senderIban);
        Account receiverAccount = TestUtils.createNewAccount("test user2", "receiver@test.com", BigDecimal.ZERO, receiverIban);

        when(accountRepository.findByEmailAndIban(senderEmail, senderIban)).thenReturn(senderAccount);
        when(accountRepository.findByIban(receiverIban)).thenReturn(receiverAccount);

        assertThrows(ValidationException.class, () ->
                accountService.transferAmount(senderIban, receiverIban, transferAmount, senderEmail));
    }

    @Test
    void testTransferAmountInvalidTransferAmount() {
        String senderIban = "senderIban";
        String receiverIban = "receiverIban";
        BigDecimal transferAmount = BigDecimal.valueOf(-1500);
        String senderEmail = "test@test.com";

        assertThrows(ValidationException.class, () ->
                accountService.transferAmount(senderIban, receiverIban, transferAmount, senderEmail));
    }

    @Test
    void testTransferAmountInvalidSender() {
        String senderIban = "senderIban";
        String receiverIban = "receiverIban";
        BigDecimal transferAmount = BigDecimal.valueOf(1500);
        String senderEmail = "test@test.com";

        Account senderAccount = TestUtils.createNewAccount("test user1", "unknown@test.com", BigDecimal.valueOf(1000), senderIban);
        Account receiverAccount = TestUtils.createNewAccount("test user2", "receiver@test.com", BigDecimal.ZERO, receiverIban);

        when(accountRepository.findByEmailAndIban(senderEmail, senderIban)).thenReturn(senderAccount);
        when(accountRepository.findByIban(receiverIban)).thenReturn(receiverAccount);

        var exception =assertThrows(ValidationException.class, () ->
                accountService.transferAmount(senderIban, receiverIban, transferAmount, senderEmail));
        assertEquals("Sender IBAN does not belong to the requesting User",exception.getMessage());
    }

    @Test
    void testTransferAmountInvalidReceiver() {
        String senderIban = "senderIban";
        String receiverIban = "receiverIban";
        BigDecimal transferAmount = BigDecimal.valueOf(500);
        String senderEmail = "test@test.com";

        Account senderAccount = TestUtils.createNewAccount("test user1", senderEmail, BigDecimal.valueOf(1000), senderIban);

        when(accountRepository.findByEmailAndIban(senderEmail, senderIban)).thenReturn(senderAccount);
        when(accountRepository.findByIban(receiverIban)).thenReturn(null);

        var exception = assertThrows(ValidationException.class, () ->
                accountService.transferAmount(senderIban, receiverIban, transferAmount, senderEmail));
        assertEquals("Receiver's IBAN does not exists", exception.getMessage());
    }

}
