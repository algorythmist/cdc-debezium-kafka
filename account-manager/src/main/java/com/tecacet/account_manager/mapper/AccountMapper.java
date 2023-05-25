package com.tecacet.account_manager.mapper;

import com.tecacet.account_manager.entity.AccountEntity;
import com.tecacet.account_manager.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AccountMapper {

    AccountEntity toEntity(Account account);

    Account toDto(AccountEntity entity);

    void updateCustomerFromDto(Account account, @MappingTarget AccountEntity entity);
}
