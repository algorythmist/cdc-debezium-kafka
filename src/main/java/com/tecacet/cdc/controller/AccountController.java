package com.tecacet.cdc.controller;

import com.tecacet.cdc.mapper.AccountMapper;
import com.tecacet.cdc.model.Account;
import com.tecacet.cdc.repository.AccountRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api")
@Service
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Operation()
    @PostMapping("/account")
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        log.info("Add new account = " + account.toString());
        var entity = accountMapper.toEntity(account);
        entity = accountRepository.save(entity);
        return ResponseEntity.ok(accountMapper.toDto(entity));
    }
}
