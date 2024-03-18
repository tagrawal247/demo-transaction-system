package demo.java.demotransactionsystem.service;

import demo.java.demotransactionsystem.exception.ValidationException;
import demo.java.demotransactionsystem.model.Account;
import demo.java.demotransactionsystem.model.AccountDetails;
import demo.java.demotransactionsystem.model.TransactionDetails;
import demo.java.demotransactionsystem.repository.AccountRepository;
import demo.java.demotransactionsystem.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import demo.java.demotransactionsystem.exception.UnauthorizedException;

import static demo.java.demotransactionsystem.exception.ErrorMessage.*;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService{
    private static final String TRANSACTION_SUCCESS = "SUCCESS";
    private AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }
    @Override
    public AccountDetails createAccount(String email, String name, BigDecimal openingBalance) {
        var iban = getUniqueIban();
        Account account = Account.builder()
                .email(email)
                .balance(openingBalance)
                .name(name)
                .iban(iban)
                .build();
        accountRepository.save(account);
        return mapToAccountDetailsResponse(account);
    }

    private String getUniqueIban() {
        return Stream.generate(() -> Utility.generateIban())
                .filter(randomIban -> accountRepository.findByIban(randomIban) == null)
                .findFirst()
                .get();
    }

    private AccountDetails mapToAccountDetailsResponse(Account account) {
        var accountDetails = new AccountDetails();
        accountDetails.setAccountId(account.getId());
        accountDetails.setName(account.getName());
        accountDetails.setEmailId(account.getEmail());
        accountDetails.balance(account.getBalance());
        accountDetails.setIban(account.getIban());
        return accountDetails;
    }

    @Override
    public List<AccountDetails> retrieveAllAccounts(String email) {
        var accountList = accountRepository.findAllByEmail(email);
        return accountList.stream()
                .map(this::mapToAccountDetailsResponse)
                .toList();
    }

    @Override
    public AccountDetails retrieveAccountDetails(String accountId, String email) {
        var account = Optional.ofNullable(accountRepository.findByEmailAndId(email, accountId)).orElseThrow(() -> new UnauthorizedException(USER_UNAUTHORIZED_TO_RETRIEVE_ACCOUNT_IBAN_MISMATCH));
        return mapToAccountDetailsResponse(account);
    }

    @Override
    @Transactional
    public TransactionDetails transferAmount(String senderIban, String receiverIban, BigDecimal amount, String senderEmail) {
        log.info("Starting amount transfer for sender IBAN {}, receiver Iban {}, transfer amount {}", senderIban, receiverIban, amount);

        // This check happens at API level however, we are redoing to secure transaction method as well.
        if(amount.compareTo(BigDecimal.ZERO)<=0)
            throw new ValidationException(VALIDATION_TRANSFER_AMOUNT_INCORRECT);

        var senderAccount= Optional.ofNullable(accountRepository.findByEmailAndIban(senderEmail, senderIban)).orElseThrow(()-> new UnauthorizedException(USER_UNAUTHORIZED_TO_TRANSFER_AMOUNT_IBAN_MISMATCH));
        var receiverAccount = Optional.ofNullable(accountRepository.findByIban(receiverIban)).orElseThrow(()-> new ValidationException(VALIDATION_TRANSFER_AMOUNT_INVALID_RECEIVER_IBAN));

        if(amount.compareTo(senderAccount.getBalance())>0)
            throw new ValidationException(VALIDATION_TRANSFER_AMOUNT_NOT_ENOUGH_MONEY);

        senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
        receiverAccount.setBalance(receiverAccount.getBalance().add(amount));
        accountRepository.saveAll(List.of(senderAccount,receiverAccount));

        log.info("Transaction completed for sender IBAN {}, receiver Iban {}, transfer amount {}", senderIban, receiverIban, amount);

        var transactionDetails =  new TransactionDetails();
        transactionDetails.setReceiverIban(receiverIban);
        transactionDetails.setSenderIban(senderIban);
        transactionDetails.setAmount(amount);
        transactionDetails.setStatus(TRANSACTION_SUCCESS);

        return transactionDetails;
    }

    

}
