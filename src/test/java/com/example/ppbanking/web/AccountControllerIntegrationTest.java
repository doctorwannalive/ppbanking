package com.example.ppbanking.web;

import com.example.ppbanking.security.JwtService;
import com.example.ppbanking.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc // run with filters (JwtAuthFilter)
class AccountControllerIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper om;
    @Autowired private JwtService jwt;
    @Autowired private AccountService account;

    private String accessToken;

    @BeforeEach
    void setup() {
        account.register("maksym","pass12345","USER");
        // Generate a real access token for "kate"
        accessToken = jwt.generateAccess("maksym");
    }

    @Test
    void deposit_and_get_account() throws Exception {
        mvc.perform(post("/account/deposit")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("amount", 200))))
                .andExpect(status().isOk());

        mvc.perform(get("/account")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(200.0));
    }
}
