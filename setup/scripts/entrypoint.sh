#!/bin/bash

wait_time=15s
password=myStrongPassword123

echo Creating DB
sleep $wait_time

/opt/mssql-tools/bin/sqlcmd -S 0.0.0.0 -U sa -P $password -i ./init.sql
