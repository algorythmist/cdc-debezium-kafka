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

Start the spring boot application. 

