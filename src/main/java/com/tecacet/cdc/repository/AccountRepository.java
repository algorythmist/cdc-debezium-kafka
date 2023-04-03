package com.tecacet.cdc.repository;

import com.tecacet.cdc.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    Optional<AccountEntity> findByBankNameAndAccountNumber(String bankName, String accountNumber);
}
