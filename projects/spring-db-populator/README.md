# About pring-db-populator

This project is a ims client that insert sql commands to activemq.<br/>
It does the insert in a round robin strategy using two different activemq connections.
It also execute this inserts using parallel threads.

# Input parameters

To run this client run: `java -jar spring-db-populator-0.0.1-SNAPSHOT.jar 1 5000 1000`

# Parameters definition

The client need 3 parameters to begin execution. This parameters are:

 * Parameter 1: index beginning id. This id is the sequence id that will be use as starting point. It can be any number

 * Parameter 2: How many inserts to execute. In the example above it will send 5000 insert commands to activemq

 * Parameter 3: It defines how many parallel threads will be inserting commands to activemq. In the example above it will create 1000 concurrent threads.

# Changing activemq parameters.

All the activemq configuration is provided using spring jmx and is defined at `src/main/resources/jdbc-spring-context-xml.<br/>
Edit **activemq** and **activemq2** bean definition as needed. This client will only round  robin two brokers.
