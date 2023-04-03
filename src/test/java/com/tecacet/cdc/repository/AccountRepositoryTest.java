package com.tecacet.cdc.repository;

import com.tecacet.cdc.entity.AccountEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void saveAccountTest() {
        AccountEntity account = createEntity();
        assertNotNull(account.getId());
    }

    @Test
    void findByIdTest() {
        AccountEntity account = createEntity();
        UUID accountId = account.getId();
        Optional<AccountEntity> foundAccount = accountRepository.findById(accountId);
        assertTrue(foundAccount.isPresent());
        assertEquals(account.getName(), foundAccount.get().getName());
        assertEquals(account.getBankName(), foundAccount.get().getBankName());
        assertEquals(account.getAccountNumber(), foundAccount.get().getAccountNumber());
        assertEquals(account.getBalance(), foundAccount.get().getBalance());
        assertEquals(account.getType(), foundAccount.get().getType());
    }

    @Test
    void findByTest() {
        AccountEntity account = createEntity();
        Optional<AccountEntity> foundAccount = accountRepository.findByBankNameAndAccountNumber(account.getBankName(), account.getAccountNumber());
        assertTrue(foundAccount.isPresent());
        assertEquals(account.getName(), foundAccount.get().getName());
        assertEquals(account.getBankName(), foundAccount.get().getBankName());
        assertEquals(account.getAccountNumber(), foundAccount.get().getAccountNumber());
        assertEquals(account.getBalance(), foundAccount.get().getBalance());
        assertEquals(account.getType(), foundAccount.get().getType());
    }

    @Test
    void updateAccountTest() {
        AccountEntity account = createEntity();
        account.setBalance(BigDecimal.valueOf(2000));
        account = accountRepository.save(account);
        assertEquals(BigDecimal.valueOf(2000), accountRepository.findById(account.getId()).get().getBalance());
    }

    @Test
    void deleteAccountTest() {
        AccountEntity account = createEntity();
        UUID accountId = account.getId();
        accountRepository.deleteById(accountId);
        assertFalse(accountRepository.findById(accountId).isPresent());
    }

    private AccountEntity createEntity() {
        AccountEntity account = new AccountEntity();
        account.setName("John Doe");
        account.setBankName("ABC Bank");
        account.setAccountNumber("1234567890");
        account.setBalance(new BigDecimal("1000.00"));
        account.setType(AccountEntity.Type.CHECKING);
        accountRepository.save(account);
        return account;
    }
}
