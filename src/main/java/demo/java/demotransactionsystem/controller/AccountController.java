package demo.java.demotransactionsystem.controller;


import demo.java.demotransactionsystem.exception.ValidationException;
import demo.java.demotransactionsystem.model.AccountDetails;
import demo.java.demotransactionsystem.model.CreateAccountRequest;
import demo.java.demotransactionsystem.model.TransactionDetails;
import demo.java.demotransactionsystem.model.TransactionRequest;
import demo.java.demotransactionsystem.security.JwtService;
import demo.java.demotransactionsystem.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import demo.java.demotransactionsystem.exception.UnauthorizedException;
import java.util.List;
import java.util.Optional;

import static demo.java.demotransactionsystem.exception.ErrorMessage.*;

@RestController
@RequestMapping("/api/v1")
public class AccountController implements AccountsApi {
    private AccountService accountService;
    private JwtService jwtService;

    public AccountController(AccountService accountService, JwtService jwtService){
        this.accountService = accountService;
        this.jwtService = jwtService;
    }

    @Override
    @PostMapping("/accounts")
    public ResponseEntity<AccountDetails> createAccount(CreateAccountRequest request) {
        var requesterEmail = jwtService.getUserNameFromJWT();
        // Validate if IBAN belongs to the user extracted from the JWT token
        if (!requesterEmail.equals(request.getEmail())) {
            throw new UnauthorizedException(USER_UNAUTHORIZED_TO_CREATE_ACCOUNT);
        }
        var accountDetails = accountService.createAccount(request.getEmail(), request.getName(), request.getOpeningBalance());
        return ResponseEntity.of(Optional.of(accountDetails));
    }

    @Override
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDetails>> retrieveAllAccount() {
        return ResponseEntity.of(Optional.of(accountService.retrieveAllAccounts(jwtService.getUserNameFromJWT())));
    }

    @Override
    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<AccountDetails> retrieveAccount(@PathVariable String accountId) {
        var email = jwtService.getUserNameFromJWT();
        var accountDetails = accountService.retrieveAccountDetails(accountId, email);
        return ResponseEntity.of(Optional.of(accountDetails));
    }

    @Override
    @PostMapping("/accounts/transactions")
    public ResponseEntity<TransactionDetails> transferAmount(TransactionRequest transactionRequest){
        var requesterEmail = jwtService.getUserNameFromJWT();
        var transactionDetails = accountService.transferAmount(transactionRequest.getSenderIban(), transactionRequest.getReceiverIban(), transactionRequest.getAmount(), requesterEmail);
        return ResponseEntity.of(Optional.of(transactionDetails));
    }
}