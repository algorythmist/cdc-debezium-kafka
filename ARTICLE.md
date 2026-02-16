# Real-Time Data Streaming with Change Data Capture: A Hands-On Guide with SQL Server, Debezium, Kafka, and Spring Boot

If you've ever needed to react to database changes in real time — syncing data across microservices, building audit logs, or feeding analytics pipelines — you've probably come across **Change Data Capture (CDC)**. It's one of those patterns that sounds complex on paper but becomes surprisingly elegant once you see the moving parts working together.

In this article, I'll walk you through a complete, working example that captures every INSERT, UPDATE, and DELETE on a SQL Server table and streams those changes through Kafka to a Spring Boot consumer — all without writing a single database trigger.

The full source code is available on [GitHub](https://github.com/algorythmist/cdc-debezium-kafka).

---

## What is Change Data Capture?

Change Data Capture is a design pattern that identifies and captures changes made to data in a database, then delivers those changes in real time to downstream consumers.

The traditional approach — polling the database on a timer, or writing triggers that push to a message queue — has serious drawbacks. Polling is wasteful and introduces latency. Triggers couple your application logic to the database and can degrade write performance.

CDC takes a fundamentally different approach: it reads the database's **transaction log**. Every relational database already records every change in its log for crash recovery and replication. CDC simply taps into that existing stream. The result is:

- **Zero impact on write performance** — no triggers, no additional queries
- **Complete fidelity** — every change is captured, including the before and after state
- **Low latency** — changes are available within seconds

SQL Server has built-in CDC support at the engine level, and **Debezium** is the open-source platform that turns those CDC logs into Kafka events.

---

## Architecture Overview

Here's how the pieces fit together:

```
┌──────────┐     HTTP      ┌──────────────────┐     JDBC      ┌────────────┐
│  Client   │ ──────────── │  Account Manager │ ──────────── │ SQL Server │
│ (Swagger) │              │  (Spring Boot)   │              │  (CDC On)  │
└──────────┘              └──────────────────┘              └─────┬──────┘
                                                                  │
                                                          Transaction Log
                                                                  │
                                                           ┌──────┴──────┐
                                                           │  Debezium   │
                                                           │ (Connector) │
                                                           └──────┬──────┘
                                                                  │
                                                            Kafka Topic
                                                   mssql.accounts.dbo.account
                                                                  │
                                                           ┌──────┴──────┐
                                                           │  Account    │
                                                           │  Receiver   │
                                                           │(Spring Boot)│
                                                           └─────────────┘
```

The data flow is:

1. A client sends an HTTP request to the **Account Manager** API (create, update, or delete an account).
2. The Account Manager writes the change to **SQL Server**.
3. SQL Server's CDC agent captures the change from the transaction log.
4. **Debezium** reads the CDC log and publishes a structured event to a **Kafka** topic.
5. The **Account Receiver** consumes the event from Kafka and processes it.

The beauty of this architecture is that services 1–2 and 5 are completely decoupled. The Account Manager doesn't know (or care) that anyone is listening to its changes. The Account Receiver doesn't know (or care) where the data originally came from.

---

## The Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Database | MS SQL Server | 2022 |
| CDC Connector | Debezium | 2.6 |
| Message Broker | Apache Kafka (Confluent) | 7.6.0 |
| Producer Service | Spring Boot + Spring Data JPA | 3.5.3 |
| Consumer Service | Spring Boot + Spring Kafka | 3.5.3 |
| Language | Java | 21 |
| API Documentation | SpringDoc OpenAPI (Swagger UI) | 2.7.0 |
| Kafka Dashboard | Kafka UI | 0.7.1 |

---

## Setting Up the Infrastructure

Everything runs in Docker. The `docker-compose.yaml` defines five services:

```yaml
services:

  mssql:
    build: .
    ports:
      - '1433:1433'
    volumes:
      - mssqldata:/var/opt/mssql
      - ./scripts:/usr/src/app
    working_dir: /usr/src/app
    command: sh -c './entrypoint.sh & /opt/mssql/bin/sqlservr'
    environment:
      SA_PASSWORD: 'myStrongPassword123'
      ACCEPT_EULA: 'Y'
      MSSQL_AGENT_ENABLED: 'true'

  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - 22181:2181

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    depends_on:
      - zookeeper
    ports:
      - 29092:29092
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  debezium:
    image: quay.io/debezium/connect:2.6
    ports:
      - 8083:8083
    links:
      - kafka
      - mssql
    environment:
      - BOOTSTRAP_SERVERS=kafka:9092
      - GROUP_ID=1
      - CONFIG_STORAGE_TOPIC=connect_configs
      - OFFSET_STORAGE_TOPIC=connect_offsets
      - STATUS_STORAGE_TOPIC=connect_statuses

  kafka-ui:
    image: provectuslabs/kafka-ui:v0.7.1
    ports:
      - 8081:8080
    environment:
      DYNAMIC_CONFIG_ENABLED: true
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
```

A few things worth noting:

- **`MSSQL_AGENT_ENABLED: 'true'`** is critical. SQL Server CDC depends on the SQL Server Agent service to read the transaction log. Without it, CDC won't capture any changes.
- **Kafka exposes two listeners**: `PLAINTEXT` on port 9092 for communication between Docker containers (Debezium, Kafka UI), and `PLAINTEXT_HOST` on port 29092 for applications running on the host machine (our Spring Boot services).
- **Debezium runs as a Kafka Connect worker**. It uses three internal Kafka topics (`connect_configs`, `connect_offsets`, `connect_statuses`) to manage its own state, which means it's stateless and can be restarted without losing its position in the transaction log.

---

## Enabling CDC on SQL Server

When the SQL Server container starts, an initialization script runs automatically to create the database and enable CDC:

```sql
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

-- Enable CDC at the database level
EXEC sys.sp_cdc_enable_db;
GO

-- Enable CDC on the specific table
EXEC sys.sp_cdc_enable_table
  @source_schema = N'dbo',
  @source_name = N'account',
  @capture_instance = N'account_CDC',
  @supports_net_changes = 1,
  @role_name = NULL;
GO
```

Two things happen here that are easy to miss:

1. **`sys.sp_cdc_enable_db`** turns on CDC for the entire database. This creates a `cdc` schema and several system tables that track which tables are being monitored.

2. **`sys.sp_cdc_enable_table`** sets up CDC for a specific table. The `@supports_net_changes = 1` parameter enables "net changes" queries, meaning if a row is updated multiple times between polls, you can retrieve just the final state. The `@capture_instance` name gives you control over naming if you ever need to reconfigure CDC without downtime.

Behind the scenes, SQL Server creates a *change table* in the `cdc` schema that mirrors the structure of the source table, plus metadata columns like the operation type and LSN (Log Sequence Number).

---

## The Account Manager: Writing to the Database

The Account Manager is a straightforward Spring Boot CRUD application. It exposes a REST API and writes to SQL Server using Spring Data JPA.

### The Entity

```java
@Entity
@Table(name = "account")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AccountEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "bankName", nullable = false)
    private String bankName;

    @Column(name = "accountNumber", nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    public enum Type {
        CHECKING, SAVINGS
    }
}
```

### The REST Controller

```java
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Create account")
    @PostMapping("/account")
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        return ResponseEntity.ok(accountService.createAccount(account));
    }

    @Operation(summary = "Update account")
    @PutMapping
    public ResponseEntity<Account> update(@RequestBody Account account) {
        return ResponseEntity.ok(accountService.updateAccount(account));
    }

    @Operation(summary = "Delete account")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam String bankName,
                       @RequestParam String accountNumber) {
        accountService.deleteAccount(bankName, accountNumber);
    }

    @Operation(summary = "Find all accounts")
    @GetMapping
    public ResponseEntity<List<Account>> findAll() {
        return ResponseEntity.ok(accountService.findAll());
    }
}
```

This is deliberately simple. The important point is that **there is no Kafka code here**. The Account Manager is a pure CRUD service. It has no idea that its database changes are being streamed anywhere. This is the power of CDC — the producer doesn't need to change at all.

---

## Registering the Debezium Connector

Once all the Docker services are running, you need to tell Debezium *what* to monitor. This is done by POSTing a JSON configuration to the Kafka Connect REST API:

```json
{
    "name": "account-connector",
    "config": {
        "connector.class": "io.debezium.connector.sqlserver.SqlServerConnector",
        "tasks.max": "1",
        "topic.prefix": "mssql",
        "database.hostname": "mssql",
        "database.port": "1433",
        "database.user": "sa",
        "database.password": "myStrongPassword123",
        "database.names": "accounts",
        "schema.history.internal.kafka.bootstrap.servers": "kafka:9092",
        "schema.history.internal.kafka.topic": "schema-changes.accounts",
        "database.encrypt": "false"
    }
}
```

Key configuration points:

- **`topic.prefix`** determines the Kafka topic naming. Debezium creates topics using the pattern `{prefix}.{database}.{schema}.{table}`. With prefix `mssql`, database `accounts`, schema `dbo`, and table `account`, the topic becomes **`mssql.accounts.dbo.account`**.
- **`schema.history.internal.kafka.topic`** is where Debezium stores DDL history. If the table schema changes (columns added/removed), Debezium needs to know the schema at each point in time to correctly deserialize the log entries.
- **`database.hostname`** uses `mssql` (the Docker service name), not `localhost`, because Debezium runs inside the Docker network.

Register it with:

```bash
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" \
  http://localhost:8083/connectors/ -d @register-sqlserver.json
```

You should get a `201 Created` response. You can verify the connector is running:

```bash
curl http://localhost:8083/connectors/account-connector/status
```

---

## The Account Receiver: Consuming CDC Events

This is where the real CDC magic becomes visible. The Account Receiver is a Spring Boot application that listens to the Kafka topic and processes Debezium change events.

### Kafka Configuration

```java
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value(value = "${kafka.bootstrapAddress:localhost:29092}")
    private String bootstrapAddress;

    private String groupId = "consumer-test-group";

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        return new DefaultKafkaConsumerFactory<>(props,
            new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
            kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
```

Messages are consumed as raw strings (using `StringDeserializer`) rather than using Avro or a schema registry. This keeps things simple for a demo, though in production you'd likely want a schema registry for type safety.

### Understanding the Debezium Message Format

A Debezium CDC message is a rich JSON document. The key structure is the **payload**, which contains:

```java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payload<T> {

    public enum OperationType {
        CREATE, UPDATE, DELETE
    }

    private T before;       // State before the change (null for inserts)
    private T after;        // State after the change (null for deletes)
    private DebeziumMessage.Source source;  // Metadata (LSN, timestamp, etc.)
    private String op;      // "c" = create, "u" = update, "d" = delete
    private Long ts_ms;     // Timestamp of the event

    public OperationType getOperationType() {
        switch (op) {
            case "c": return OperationType.CREATE;
            case "u": return OperationType.UPDATE;
            case "d": return OperationType.DELETE;
            default: throw new IllegalArgumentException("Unknown operation type " + op);
        }
    }
}
```

For an INSERT, `before` is null and `after` contains the new row. For a DELETE, `before` contains the deleted row and `after` is null. For an UPDATE, both `before` and `after` are populated, giving you a complete diff.

### The Account Model

Debezium uses the database column names, so we need Jackson annotations to map them:

```java
@Data
public class DebeziumAccount {

    private String id;
    private String name;
    @JsonProperty("bank_name")
    private String bankName;
    @JsonProperty("account_number")
    private String accountNumber;
    private byte[] balance;
    private Account.Type type;
}
```

Notice that **`balance` is a `byte[]`**, not a `BigDecimal`. This is a Debezium quirk: `DECIMAL` columns are serialized as raw bytes representing the unscaled value. You need to reconstruct the `BigDecimal` manually.

### The Listener

```java
@Slf4j
@Component
public class CdcChangeListener {

    private final ObjectMapper objectMapper;

    public CdcChangeListener() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @KafkaListener(topics = "${kafka.topic}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void receive(String message) throws JsonProcessingException {
        DebeziumMessage<DebeziumAccount> debeziumMessage =
            objectMapper.readValue(message, DebeziumAccountMessage.class);

        var payload = debeziumMessage.getPayload();
        DebeziumAccount after = payload.getAfter();

        // Reconstruct BigDecimal from Debezium's byte[] encoding
        var balance = (after == null) ? null :
            new BigDecimal(new BigInteger(after.getBalance()), 2);

        switch (payload.getOperationType()) {
            case CREATE:
                log.info("Received CDC create for account {} with balance {}",
                    after.getAccountNumber(), balance);
                break;
            case UPDATE:
                log.info("Received CDC update for account {} with balance {}",
                    after.getAccountNumber(), balance);
                break;
            case DELETE:
                log.info("Received CDC delete for account {}",
                    payload.getBefore().getAccountNumber());
                break;
        }
    }
}
```

The decimal reconstruction on line `new BigDecimal(new BigInteger(after.getBalance()), 2)` deserves attention. Debezium encodes `DECIMAL(19,2)` as the unscaled integer value in bytes. So a balance of `1000000.00` becomes the integer `100000000` encoded as bytes, and we reconstruct it by specifying scale `2`.

---

## Running the Complete Demo

### Prerequisites

- Docker and Docker Compose
- Java 21
- Maven

### Step 1: Start the Infrastructure

```bash
cd setup
docker-compose up -d
```

Wait about 30 seconds for SQL Server to fully initialize (the entrypoint script has a 15-second sleep to wait for the database engine).

### Step 2: Register the Debezium Connector

```bash
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" \
  http://localhost:8083/connectors/ -d @register-sqlserver.json
```

If Debezium isn't ready yet, you'll get a connection refused error. Wait a few more seconds and retry.

### Step 3: Start the Spring Boot Applications

In separate terminals:

```bash
# Terminal 1 — Start the CDC consumer first
cd account-receiver
mvn spring-boot:run

# Terminal 2 — Start the API
cd account-manager
mvn spring-boot:run
```

### Step 4: Create an Account

Open the Swagger UI at **http://localhost:8080/swagger-ui/index.html** and use the "Create account" endpoint with this request body:

```json
{
  "name": "Gru",
  "bankName": "Bank of Evil",
  "accountNumber": "666",
  "balance": 1000000,
  "type": "CHECKING"
}
```

### Step 5: Watch the CDC Event Arrive

In the Account Receiver terminal, you should immediately see:

```
Received CDC create for account 666 with balance 1000000.00
```

### Step 6: Explore in Kafka UI

Open **http://localhost:8081**, click on **Topics**, and find **`mssql.accounts.dbo.account`**. Click on the **Messages** tab to see the raw Debezium event — a detailed JSON document containing the full schema, the before/after state, and source metadata including the LSN and commit timestamp.

### Step 7: Try Updates and Deletes

Go back to Swagger and update the account's balance, then delete it. You'll see corresponding CDC events in the receiver logs:

```
Received CDC update for account 666 with balance 2000000.00
Received CDC delete for account 666
```

---

## Port Reference

| Port | Service |
|------|---------|
| 1433 | SQL Server |
| 8080 | Account Manager (Swagger UI) |
| 8081 | Kafka UI |
| 8082 | Account Receiver |
| 8083 | Debezium Connect REST API |
| 29092 | Kafka (external) |

---

## What Would You Do Differently in Production?

This demo is intentionally simplified. In a production system, you'd likely want to:

1. **Use Avro + Schema Registry** instead of raw JSON. This gives you type safety, schema evolution, and smaller message sizes.

2. **Handle failures in the consumer**. Right now, a deserialization error will crash the listener. You'd want a dead-letter topic and retry logic.

3. **Secure the connections**. The demo uses the `sa` account with a hardcoded password. In production, use dedicated service accounts, TLS, and SASL authentication for Kafka.

4. **Scale the consumer**. `ConcurrentKafkaListenerContainerFactory` supports concurrency, but you'd also want to think about partitioning the Kafka topic for parallel consumption.

5. **Monitor Debezium**. The connector can fall behind if the transaction log grows too fast. Debezium exposes JMX metrics for lag monitoring.

6. **Consider Debezium Server** or **Debezium Engine** if you don't want to run a full Kafka Connect cluster just for CDC.

---

## Wrapping Up

Change Data Capture with Debezium is one of those patterns where the setup effort is front-loaded but the payoff is enormous. Once the pipeline is running, every database change automatically becomes a real-time event — no polling, no triggers, no dual writes.

The key insight is that CDC leverages work the database is already doing. SQL Server's transaction log exists regardless of whether you read it. Debezium just gives you a clean, structured way to tap into that stream.

If you're building microservices that need to stay in sync, implementing audit logging, feeding a search index, or building event-driven architectures, CDC with Debezium and Kafka is worth a serious look.

The complete source code is available on [GitHub](https://github.com/algorythmist/cdc-debezium-kafka).
