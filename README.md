# Consumer CDC messages
### MS SQLServer, Debezium, Kafka, and Spring Boot

#### Step 1: 

From the setup directory, run docker-compose. This will start the following services:

- MS SQL server
- Zookeeper
- Kafka
- The Debezium connector
- Kafka Manager

The scripts will also create the "accounts" database

#### Step 2:

Connect to kafka manager at http://localhost:9000
- Choose Cluster -> Add Cluster
- Cluster Name: CDCCluster
- Cluster Zookeeper Hosts: zookeeper:2181
- Save
- Go to Cluster View

#### Step 3: 

Start Debezium SQL Server connector

from the root directory:

curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @register-sqlserver.json

#### Step 4: 

1. Start the spring boot application. 

2. Go to http://localhost:8080/swagger-ui/index.html#/account-controller/createAccount

3. Create an account entering a request body like this:

```json
{
  "name": "Groo",
  "bankName": "Bank of Evil",
  "accountNumber": "123",
  "balance": 1000000,
  "type": "CHECKING"
}
```

The listener will receive the change and display a message like this:

```text
Received CDC change with payload: DebeziumMessage.Payload(before=null, after=DebeziumMessage.Value(id=27C63BDE-597E-1642-B6A8-50DD2F1637AD, name=Groo, bankName=Bank of Evil, accountNumber=123, balance=[5, -11, -31, 0], type=CHECKING), source=DebeziumMessage.Source(version=2.2.0.Alpha3, connector=sqlserver, name=server1, ts_ms=1680531761800, snapshot=false, db=accounts, sequence=null, schema=dbo, table=account, change_lsn=00000025:00000dc0:0002, commit_lsn=00000025:00000dc0:0003, event_serial_no=1), op=c, ts_ms=1680531762964, transactionBlock=null)
```