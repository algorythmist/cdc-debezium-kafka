# Consumer CDC messages
### MS SQLServer, Debezium, Kafka, and Spring Boot

#### Step 1: 

From the setup directory, run docker-compose. This will start the following services:

- MS SQL server
- Zookeeper
- Kafka
- The Debezium connector
- Kafka UI

The scripts will also create the "accounts" database, which contains two schemas: dbo and cdc. 
The dbo schema contains the account table, which is the table we are monitoring for changes. 
The cdc schema contains the cdc table, which is where the CDC messages will be stored.


#### Step 2: 

Start Debezium SQL Server connector

from the root directory:

curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @register-sqlserver.json

#### Step 3: 

1. Start the spring boot application. 

2. Go to http://localhost:8080/swagger-ui/index.html#/account-controller/createAccount

3. Create an account entering a request body like this:

```json
{
  "name": "Gru",
  "bankName": "Bank of Evil",
  "accountNumber": "666",
  "balance": 1000000,
  "type": "CHECKING"
}
```

The listener will receive the change and display a message like this:

```text
Received CDC create for account 666 with balance 1000000.00

#### Step 4:

Connect to kafka ui at http://localhost:8081
- Click "topics"
- You will see a topic called "mssql.accounts.dbo.account". Click on it
- Click on the messages tab to inspect the messages sent to the topic


```