package com.tecacet.cdc.service;

import com.tecacet.cdc.mapper.AccountMapper;
import com.tecacet.cdc.model.Account;
import com.tecacet.cdc.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        var entity = accountMapper.toEntity(account);
        entity = accountRepository.save(entity);
        return accountMapper.toDto(entity);
    }

    public Account updateAccount(Account account) {
        var entity = accountRepository.findByBankNameAndAccountNumber(account.getBankName(), account.getAccountNumber())
                .orElseThrow();
        accountMapper.updateCustomerFromDto(account, entity);
        return accountMapper.toDto(accountRepository.save(entity));
    }


    public List<Account> findAll() {
        return accountRepository.findAll().stream().map(accountMapper::toDto)
                .collect(Collectors.toList());
    }
}
