package com.example.ppbanking.repo;

import com.example.ppbanking.domain.User;
import com.example.ppbanking.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository users;

    @Test
    void findByUsername_returnsUser() {
        User u = new User();
        u.setUsername("maksym");
        u.setPasswordHash("hash");
        u.setBalance(0.0);
        u.setRole("USER");
        users.save(u);

        var found = users.findByUsername("maksym");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("maksym");
    }
}
