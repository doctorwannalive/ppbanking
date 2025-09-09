package com.example.ppbanking.service;

import com.example.ppbanking.domain.User;
import com.example.ppbanking.exception.ApiException;
import com.example.ppbanking.repo.UserRepository;
import com.example.ppbanking.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountServiceTest {

    @Autowired private AccountService service;
    @Autowired private UserRepository users;

    private Long idA;
    private Long idB;

    @BeforeEach
    void setup() {
        service.register("maksym", "pass12345", "USER");
        service.register("kate", "pass12345", "USER");
        idA = users.findByUsername("maksym").map(User::getId).orElseThrow();
        idB = users.findByUsername("kate").map(User::getId).orElseThrow();
    }

    @Test
    void deposit_increasesBalance_andLogsTx() {
        service.deposit(idA, 200.0);
        var account = service.getAccount(idA);
        assertThat(account.balance()).isEqualTo(200.0);
        assertThat(account.history()).isNotEmpty();
        assertThat(account.history().get(0).getType()).isEqualTo("DEPOSIT");
    }

    @Test
    void withdraw_insufficientFunds_throwsApiException422() {
        var ex = assertThrows(ApiException.class, () -> service.withdraw(idA, 50.0));
        assertThat(ex.getStatus().value()).isEqualTo(422);
    }

    @Test
    void transfer_movesMoney_betweenAccounts() {
        service.deposit(idA, 300.0);
        service.transfer(idA, idB, 120.0);

        var a = service.getAccount(idA);
        var b = service.getAccount(idB);
        assertThat(a.balance()).isEqualTo(180.0);
        assertThat(b.balance()).isEqualTo(120.0);
    }

    @Test
    void transfer_toSelf_is400() {
        var ex = assertThrows(ApiException.class, () -> service.transfer(idA, idA, 10.0));
        assertThat(ex.getStatus().value()).isEqualTo(400);
    }
}
