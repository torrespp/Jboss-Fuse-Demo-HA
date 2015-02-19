# Introduction

This demo was created to review some general features of JBoss Fuse 6.0. <br/>
It was created to help new fuse developers to understand a full development cycle of a project including using it on a HA enviorment. <br/><BR/>
ENJOY!!!!


## Objectives

This demo will include information about several topics wich include: 

* Create a JBoss Fuse installation and initial configuration.
* Understand how to use Karaf console
* Understand how to Begin a Fabric configuration 
* Access Web console to manage Fuse and Fabrics
* Learn how to create JMS Fabric profiles
* Learn how to create 2 JMS Brokers Group (Two JMS Clusters) using profiles
* Learn how to deploy projects profiles using fabric8 maven plugin
* Learn how to create multiple camel consumers
* Understand JBoss Fuse escalation
* Review how camel read data from AMQ JMS Brokers and how to ejecute commands on a Postgresql database


## Pre-Requisites

1. JBoss Fuse 6.0 zip installation file 
2. Java JDK 7 installed
3. Apache Maven 3.1.1 version installed
4. Web Browser
5. Basic Linux commands understanding
6. PostgreSQL 9.3 or superior installed
7. PostgreSQL basic understanding
8. Internet connection

# Setup JBoss Fuse

## Install JBoss Fuse

1. Open a command terminal

1. Unzip JBoss Fuse on any directory that you wish to use as $FUSE_HOME. In this example i will use directory `/opt/redhat/`. Copy JBoss Fuse installation zip file on the selected directory and be sure your user have read, write and execute privileges.

	- `cd /opt/redhat`
	- `unzip jboss-fuse-full-6.1.0.redhat-379.zip`<br/>
    ![Unzip Command](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture1.png)
    
	- `export FUSE_HOME=/opt/redhat/jboss-fuse-full-6.1.0.redhat-379`

	Thats it!!!, JBoss Fuse is already install!!!
 
## Configure JBoss Fuse 
 
Before running JBoss Fuse for the first time we need to configure user/password access.

1. Enable user/password for karaf console. On your opened terminal execute:
	- `cd $FUSE_HOME`
	- `vi ./etc/users.properties` (If you do not like vi, use any other text editor)
	- Uncomment the final line by removing # character from #admin=admin,admin line
	- Save the file (esc, :wq)

## Running JBoss Fuse

1. On opened terminal `$FUSE_HOME/bin/start`

2. Access karaf console:
	-  `./bin/client -u admin -p admin` 
    <br/>If you get a message **"Failed to get the session"** wait a few seconds and try again. This message means that JBoss Fuse is starting.<br/>
	![Karaf Console](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture2.png)

3. Create a fabric so we can manage all the brokers from a single console:
	- fabric:create --clean --wait-for-provisioning  --bind-address localhost --resolver manualip --global-resolver manualip --manual-ip localhost --zookeeper-password admin

7 View Fabric is created:
	- container-list

8 Open URL http://localhost:8181 on a web browser and login with user admin and password admin

9 Create JMSBrokers profiles on karaf console
	- fabric:profile-create mq-brokers
	- fabric:profile-edit --resource broker.xml mq-brokers
	- Paste broker.xml content, 
	
<beans
xmlns="http://www.springframework.org/schema/beans"
xmlns:amq="http://activemq.apache.org/schema/core"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://www.springframework.org/schema/beans
http://www.springframework.org/schema/beans/spring-beans-2.0.xsd
http://activemq.apache.org/schema/core http://activemq.apache.org/schema/core/activemq-core.xsd">
<bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
<property name="properties">
<bean class="org.fusesource.mq.fabric.ConfigurationProperties"/>
</property>
</bean>
<broker xmlns="http://activemq.apache.org/schema/core"
brokerName="${broker-name}"
brokerId="${broker-name}"
dataDirectory="${data}"
start="false"
persistent="true"
networkConnectorStartAsync="true"
useJmx="true">
<destinationPolicy>
<policyMap>
<policyEntries>
<policyEntry topic=">" producerFlowControl="false" optimizedDispatch="true"
memoryLimit="512mb">
<pendingMessageLimitStrategy>
<constantPendingMessageLimitStrategy limit="1000000"/>
</pendingMessageLimitStrategy>
<pendingSubscriberPolicy>
<fileCursor/>
</pendingSubscriberPolicy>
</policyEntry>
<policyEntry queue=">" producerFlowControl="false" optimizedDispatch="true"
memoryLimit="512mb">
</policyEntry>
</policyEntries>
</policyMap>
</destinationPolicy>
<managementContext>
<managementContext createConnector="false"/>
</managementContext>
<persistenceAdapter>
<kahaDB directory="${dataDir}/${brokerGroup}/kahadb" journalMaxFileLength="32mb"
enableJournalDiskSyncs="false" concurrentStoreAndDispatchTopics="true" indexCacheSize="100000"
concurrentStoreAndDispatchQueues="true">
<locker>
<shared-file-locker lockAcquireSleepInterval="200"/>
</locker>
</kahaDB>
</persistenceAdapter>
<plugins>
<jaasAuthenticationPlugin configuration="karaf" />
</plugins>
<systemUsage>
<systemUsage sendFailIfNoSpaceAfterTimeout="5000">
<memoryUsage>
<memoryUsage limit="1024 mb"/>
</memoryUsage>
<storeUsage>
<storeUsage limit="20480 mb"/>
</storeUsage>
<tempUsage>
<tempUsage limit="10240 mb"/>
</tempUsage>
</systemUsage>
</systemUsage>
<networkConnectors>
</networkConnectors>
<transportConnectors>
<transportConnector name="openwire" uri="tcp://${ipaddress}:${port}?
socketBufferSize=262144&amp;ioBufferSize=327680&amp;jms.useCompression=true;"/>
</transportConnectors>
</broker>
</beans>

	- Save (ctrl+s) and exit editor (ctrl + x)

10 Create brokers group one profile
	- fabric:profile-create mq-group1
	- fabric:profile-edit --pid org.fusesource.mq.fabric.server-broker mq-group1
	- On editor add the next lines changing its values as needed:
brokerGroup=JMSGroup1
port=61617
ipaddress=127.0.0.1
dataDir=/any/directory/location
	- Save (ctrl+s) and exit editor (ctrl + x)

11 Change profiles parents
	- profile-change-parents mq-brokers mq-default
	- profile-change-parents mq-group1 mq-brokers

12 Create Group1 containers
	- fabric:container-create-child --jvm-opts "-Xmx2048m -Xms2048m" --profile mq-group1 root JMSGroup1 2

13 Wait until they are created and started
	- watch container-list

11 Check if cluster is started
	- cluster-list

12 Create brokers group two profile
	- fabric:profile-create mq-group2
	- fabric:profile-edit --pid org.fusesource.mq.fabric.server-broker mq-group2
	- On editor add the next lines changing its values as needed:
brokerGroup=JMSGroup2
port=61618
ipaddress=127.0.0.1
dataDir=/any/directory/location
	- Save (ctrl+s) and exit editor (ctrl + x)

13 Change profiles parents
	- profile-change-parents mq-group2 mq-brokers

14 Create Group2 containers
	- fabric:container-create-child --jvm-opts "-Xmx2048m -Xms2048m" --profile mq-group2 root JMSGroup2 2

15 Wait until they are created and started
	- watch container-list

16 Check if cluster is started
	- cluster-list


CONFIGURING PROJECTS

There are two projects:
	- hainserter: Fuse route that reads sql commands and execute them on the real JDBC driver
	- spring-db-populator: Batch testing client that insert data using spring

	
1 Create PostgreSQL database:
	- Create database jdbcpoc
	- Create table and sequence with /<projects_dir>/spring-db-populator/src/main/resources/DATABASE_SCRIPT.sql

2 Install ha-inserter to Fuse
	- Edit file /<projects_dir>/hainserter/src/main/fabric8/amq.properties and set datasource properties and activemq properties
	- cd <projects_dir>/ha-inserter/
	- Execute: mvn clean install fabric8:zip fabric8:deploy
	- Set fabric8 username and password when prompted. User: admin, password: admin

4 Customize two profiles at different ports for parallel processing on karaf console
	- fabric:profile-create camel-broker1
	- fabric:profile-change-parents camel-broker1 camel-jdbcpoc
	- fabric:profile-edit --pid amq camel-broker1
	- On editor add: port=61617
	- Save (ctrl+s) and exit editor (ctrl + x)
	- fabric:profile-create camel-broker2
	- fabric:profile-change-parents camel-broker2 camel-jdbcpoc
	- fabric:profile-edit --pid amq camel-broker2
	- On editor add: port=61618
	- Save (ctrl+s) and exit editor (ctrl + x)

4 Create broker for executing route. On karaf console run:
	- fabric:container-create-child --profile camel-broker1 root JDBCPocBroker1
	- fabric:container-create-child --profile camel-broker2 root JDBCPocBroker2
	- watch container-list

5 Create two more containers for parallel processing on each queue
	- fabric:container-create-child --profile camel-broker1 root JDBCPocBroker3
 	- fabric:container-create-child --profile camel-broker2 root JDBCPocBroker4
	- watch container-list

5 Compile and run spring-db-populator client
	- cd <projects_dir>/spring-db-populator/
	- Edit file ./src/main/resources/jdbc-spring-context.xml and change beans activemq and activemq2 to point at both brokers groups ports 
	- mvn clean package
	- java -jar ./target/spring-db-populator-0.0.1-SNAPSHOT.jar 1 5000 1000 
Note: On the client the parameters are 1 = start index, 5000 = how many inserts to execute, 1000 total parallel threads
	


