package com.example.ppbanking.repo;

import com.example.ppbanking.domain.Transaction;
import com.example.ppbanking.domain.User;
import com.example.ppbanking.repo.TransactionRepository;
import com.example.ppbanking.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired private UserRepository users;
    @Autowired private TransactionRepository txs;

    @Test
    void findAllForUser_returnsHistory() {
        User a = new User(); a.setUsername("a"); a.setPasswordHash("x"); a.setRole("USER"); a.setBalance(0.0);
        User b = new User(); b.setUsername("b"); b.setPasswordHash("y"); b.setRole("USER"); b.setBalance(0.0);
        users.saveAll(List.of(a,b));

        Transaction t = new Transaction();
        t.setSenderId(a.getId());
        t.setReceiverId(a.getId());
        t.setAmount(100.0);
        t.setType("DEPOSIT");
        txs.save(t);

        var history = txs.findAllForUser(a.getId());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getType()).isEqualTo("DEPOSIT");
    }
}
