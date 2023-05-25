package com.tecacet.account_manager.repository;

import com.tecacet.account_manager.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    Optional<AccountEntity> findByBankNameAndAccountNumber(String bankName, String accountNumber);
}
