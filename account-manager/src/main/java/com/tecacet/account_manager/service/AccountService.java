package com.tecacet.account_manager.service;

import com.tecacet.account_manager.mapper.AccountMapper;
import com.tecacet.account_manager.model.Account;
import com.tecacet.account_manager.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public Account createAccount(Account account) {
        val entity = accountMapper.toEntity(account);
        accountRepository.save(entity);
        return accountMapper.toDto(entity);
    }

    public Account updateAccount(Account account) {
        val entity = accountRepository.findByBankNameAndAccountNumber(account.getBankName(), account.getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        accountMapper.updateCustomerFromDto(account, entity);
        return accountMapper.toDto(accountRepository.save(entity));
    }

    public void deleteAccount(String bankName, String accountNumber) {
        var entity = accountRepository.findByBankNameAndAccountNumber(bankName, accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        accountRepository.delete(entity);
    }

    public List<Account> findAll() {
        return accountRepository.findAll().stream().map(accountMapper::toDto)
                .collect(Collectors.toList());
    }
}
