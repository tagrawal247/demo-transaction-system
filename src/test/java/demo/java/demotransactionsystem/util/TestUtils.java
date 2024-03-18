package demo.java.demotransactionsystem.util;

import demo.java.demotransactionsystem.model.Account;
import demo.java.demotransactionsystem.model.User;

import java.math.BigDecimal;

public class TestUtils {

    public static Account createNewAccount(String name, String email, BigDecimal balance, String iban) {
        return Account.builder()
                .name(name)
                .email(email)
                .iban(iban)
                .balance(balance)
                .build();
    }

    public static User createNewUser(String name, String email, String password) {
        return User.builder()
                .name(name)
                .email(email)
                .password(password)
                .build();
    }
}
