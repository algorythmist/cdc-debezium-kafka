package com.tecacet.account_manager.controller;

import com.tecacet.account_manager.model.Account;
import com.tecacet.account_manager.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    @Operation(summary = "Create account")
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

    @Operation(summary = "Delete account")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(name = "bankName", description = "Bank Name", example = "Bank of Evil")
                       @RequestParam String bankName,
                       @Parameter(name = "accountNumber", description = "Account Number", example = "666")
                       @RequestParam String accountNumber) {
        accountService.deleteAccount(bankName, accountNumber);
    }

    @Operation(summary = "Find all accounts")
    @GetMapping
    public ResponseEntity<List<Account>> findAll() {
        return ResponseEntity.ok(accountService.findAll());
    }

}
