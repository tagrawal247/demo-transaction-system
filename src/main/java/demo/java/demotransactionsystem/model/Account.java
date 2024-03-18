package demo.java.demotransactionsystem.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;


@Builder
@Data
public class Account {
    @Id
    private String id;
    private String iban;
    private String email;
    private String name;
    private BigDecimal balance;
}
