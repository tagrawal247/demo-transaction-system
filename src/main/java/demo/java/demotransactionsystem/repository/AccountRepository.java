package demo.java.demotransactionsystem.repository;

import demo.java.demotransactionsystem.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    boolean existsByEmail(String email);
    Account findByEmailAndId(String email, String id);
    Account findByEmailAndIban(String email, String iban);
    List<Account> findAllByEmail(String email);
    Account findByIban(String iban);
}