package com.tecacet.cdc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecacet.cdc.entity.AccountEntity;
import com.tecacet.cdc.model.Account;
import com.tecacet.cdc.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AccountControllerTest {

    @Autowired
    private AccountController accountController;
    @Autowired
    private AccountRepository accountRepository;

    @Test
    void createAccount() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
        Account account = new Account();
        account.setAccountNumber("123");
        account.setName("name");
        account.setBankName("Bank");
        account.setType(Account.Type.SAVINGS);
        account.setBalance(BigDecimal.TEN);

        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(account)))
                .andExpect(status().isOk());

        List<AccountEntity> accountEntities = accountRepository.findAll();
        assertEquals(1, accountEntities.size());

        AccountEntity accountEntity = accountEntities.get(0);
        assertEquals("123", accountEntity.getAccountNumber());
        assertEquals("name", accountEntity.getName());
    }
}