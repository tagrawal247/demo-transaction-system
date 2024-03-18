package demo.java.demotransactionsystem.service;


import demo.java.demotransactionsystem.model.AccountDetails;
import demo.java.demotransactionsystem.model.TransactionDetails;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    AccountDetails createAccount(String email, String name, BigDecimal openingBalance);
    List<AccountDetails> retrieveAllAccounts(String email);
    AccountDetails retrieveAccountDetails(String accountId, String email);
    TransactionDetails transferAmount(String senderIban, String receiverIban, BigDecimal transferAmount, String email);

}