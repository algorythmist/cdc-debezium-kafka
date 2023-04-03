package com.tecacet.cdc.controller;

import com.tecacet.cdc.model.Account;
import com.tecacet.cdc.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api")
@Service
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Create acount")
    @PostMapping("/account")
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        log.info("Add new account = " + account.toString());
        return ResponseEntity.ok(accountService.createAccount(account));
    }

    @Operation(summary = "Update account")
    @PutMapping
    public ResponseEntity<Account> update(@RequestBody Account customerDto) {
        var result = accountService.updateAccount(customerDto);
        return ResponseEntity.ok(result);
    }


    @Operation(summary = "Find all accouts")
    @GetMapping
    public ResponseEntity<List<Account>> findAll() {
        return ResponseEntity.ok(accountService.findAll());
    }

}
