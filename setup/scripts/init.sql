IF NOT EXISTS(SELECT * from sys.databases WHERE name = 'accounts')
    CREATE DATABASE accounts;
GO

USE accounts;
GO

CREATE TABLE account (
  id UNIQUEIDENTIFIER PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  bank_name VARCHAR(255) NOT NULL,
  account_number VARCHAR(255) NOT NULL,
  balance DECIMAL(19,2) NOT NULL,
  type VARCHAR(10) NOT NULL
);
GO

EXEC sys.sp_cdc_enable_db;
GO

EXEC sys.sp_cdc_enable_table
  @source_schema = N'dbo',
  @source_name = N'account',
  @capture_instance = N'account_CDC',
  @supports_net_changes = 1,
  @role_name = NULL;
GO
