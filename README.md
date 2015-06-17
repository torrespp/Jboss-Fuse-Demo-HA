# Introduction

This demo was created to review some general features of JBoss Fuse 6.1.1 <br/>
It was created to help new fuse developers to understand a full development cycle of a project including using it on a HA enviorment. <br/><br/>
ENJOY!!!!

# Demo Architecture

What this demo does is to provide a client that send sql insert commands to activemq brokers. There are four JMS brokers grouped in two Master/Slave settings. By doing this we create two Active/Active groups and each group has an Active/Passive (Master/Slave) configuration.<br/>
We will also create four camel rout brokers. Two of this brokers will listen to JMS Group 1 and the other two will listen to JMS Group 2. This camel rout brokers will read every insert sql command and execute it on a postgresql database.<br/>
Here is a diagram showing all this:<br/>
![Demo Architecture](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/FuseHADemoArq.jpg)

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

1. JBoss Fuse 6.1.1 zip installation file 
2. Java JDK 7 installed
3. Apache Maven 3.1.1 version installed
4. Web Browser
5. Basic Linux commands understanding
6. PostgreSQL 9.3 or superior installed
7. PostgreSQL administration understanding (Create users, databases and running scripts)
8. Internet connection

# Setup JBoss Fuse

## Install JBoss Fuse

1. Open a command terminal

1. Unzip JBoss Fuse on any directory that you wish to use as $FUSE_HOME. In this example i will use directory `/opt/redhat/`. Copy JBoss Fuse installation zip file on the selected directory and be sure your user have read, write and execute privileges.

	- `cd /opt/redhat`
	- `unzip jboss-fuse-full-6.1.1.redhat-412.zip`<br/>
    ![Unzip Command](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture1.png)
    
	- `export FUSE_HOME=/opt/redhat/jboss-fuse-full-6.1.1.redhat-412`

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
	- `fabric:create --clean --wait-for-provisioning  --bind-address localhost --resolver manualip --global-resolver manualip --manual-ip localhost --zookeeper-password admin`<br/><br/>
    All this parameters are needed so that zookeeper and fuse fabric bind everything to **localhost** address. This is not what you need to do on production servers but since ipaddress might change on laptops or PC's fabric might not start correctly on different networks.
    
4. Validate that fabric created by running `container-list` on karaf console.
	<br/>
	![Container-list command](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture3.png)

5. Open URL http://localhost:8181 on a web browser and login with user admin and password admin<br/>
	![Fabric Login](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture4.png)
    <br/>
    ![Fabric Home](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture5.png)
    <br/>
    ![Fabric Containers](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture6.png)

# Setup JMS Broker Master/Slave Groups

## Setup mq-brokers profile

6. Create JMSBrokers profiles on karaf console
 * `fabric:profile-create mq-brokers`
 * `fabric:profile-edit --resource broker.xml mq-brokers`
 * Paste the following xml text on **broker.xml** content:<br/> 

```
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
```
- Save (ctrl+s) and exit editor (ctrl + x)
    
On Fabric configurations profiles are what define what projects, features, configurations and parameters will be available for brokers.<br/><br/>
In this case, what happened is that we create a new profile called **mq-brokers** that will have all JMS definition on a file called **broker.xml**.<br/>
	Notice how the xml we use define some variables (Example ${ipaddress}). This variables will help us next because Fuse Fabric profiles are hierarchical. This means that any child profile of **mq-brokers** can override just the variables definitions to configure different enviorments.<br/><br/>
	You can view the new profile on the web console too. Go to **Runtime/Manage** tabs and search for **mq-brokers** profile. Click on it and view how **broker.xml** file exists. You can also edit it visually on web console.

## Setup JMS Group1 profile and brokers

1. Since we want to configure two clusters we need a child profile for each group. Lets create brokers group one profile
	- `fabric:profile-create mq-group1`
	- `fabric:profile-edit --pid org.fusesource.mq.fabric.server-broker mq-group1`
	- On editor add the next lines changing its values as needed:
	```Text
	brokerGroup=JMSGroup1
	port=61617
	ipaddress=127.0.0.1
	dataDir=/opt/tmp
	```
	- Save (ctrl+s) and exit editor (ctrl + x)
    
	In this case we want to create a Master/Slave group named **brokerGroup** on port **61617** on ipaddress **127.0.0.1**. Notice hoy this variables are the ones defined on **mq-brokers/broker.xml** definition. Also notices that **dataDir** should exists, so if it isn't available, create it or change that value to an existing directory. 

2. We already explain that profiles are hierarchical so we need to change profiles parents
	- `profile-change-parents mq-brokers mq-default` (Add mq-default profile as parent)
	- `profile-change-parents mq-group1 mq-brokers` (Add mq-brokers profile as parent) <br/><br/>
    As you can see, we add mq-default profile (already existing on fabric installation) to mq-brokers. This will add AMQ features to mq-brokers and also to mq-group1 since his parent is mq-brokers.

3. Create Group1 containers. This are the actual brokers.
	- `fabric:container-create-child --jvm-opts "-Xmx2048m -Xms2048m" --profile mq-group1 root JMSGroup1 2`<br/><br/>
    Look how we assign **mq-group1** profile to the containers. Also look how we create two brokers by using the las **2** parameter. <br/>
    ![Fabric Brokers creation](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture7.png)

4. Wait until they are created and started
	- `watch container-list`<br/>
    ![Fabric Brokers creation](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture8.png)

5. Check if cluster is started
	- `cluster-list`<br/>
    ![Fabric Brokers creation](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture10.png)

## Setup JMS Group2 profile and brokers

1. Create brokers group two profile
	- `fabric:profile-create mq-group2`
	- `fabric:profile-edit --pid org.fusesource.mq.fabric.server-broker mq-group2`
	- On editor add the next lines changing its values as needed:
	```Text
	brokerGroup=JMSGroup2
	port=61618
	ipaddress=127.0.0.1
	dataDir=/opt/tmp
	```
	- Save (ctrl+s) and exit editor (ctrl + x)
    
	Same step as mq-group1 profile but port changed to **61618**

2. Change profiles parents
	- `profile-change-parents mq-group2 mq-brokers`

3. Create Group2 containers
	- `fabric:container-create-child --jvm-opts "-Xmx2048m -Xms2048m" --profile mq-group2 root JMSGroup2 2`

4. Wait until they are created and started
	- `watch container-list`<br/>
    ![Fabric Brokers creation](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture11.png)

5. Check if cluster is started
	- `cluster-list`<br/>
    ![Fabric Brokers creation](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture12.png)

# Configure Camel project

There are two projects:
 * hainserter: Fuse route that reads sql commands and execute them on the real JDBC driver
 * spring-db-populator: Batch testing client that insert data using spring

1. Create PostgreSQL database:
	- Create a Postgresql user called **fusedemo** with password **12345678**
	- Create database jdbcpoc and assign it to **fusedemo** user 
	- Create table and sequence with provided script at `/<projects_dir>/spring-db-populator/src/main/resources/DATABASE_SCRIPT.sql`

2. Install ha-inserter to Fuse. The next steps will deploy a new **camel-jdbcpoc** profile into fuse. For more information about how this is done check Readme.md file inside ha-inserter project.
	- Edit file `/<projects_dir>/hainserter/src/main/fabric8/amq.properties` and set datasource properties and activemq properties. (No need if using demo defaults and postgresql fusedemo user)
	- cd <projects_dir>/ha-inserter/
	- Execute: `mvn clean install fabric8:deploy`. This will compile install into maven and deploy fuse projet into a fuse profile.
	- Set fabric8 username and password when prompted. User: admin, password: admin
    - Wait for build success<br/>
    ![Fabric camel deploy](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture13.png)
    
	When deploy finished you can view the new profile using web console. Go to **Runtime/Manage** tabs and search for **camel-jdbcpoc** profile. 

3. Customize two profiles at different ports for parallel processing on karaf console
	- `fabric:profile-create camel-broker1`
	- `fabric:profile-change-parents camel-broker1 camel-jdbcpoc`
	- `fabric:profile-edit --pid amq camel-broker1`
	- On editor add: `port=61617`
	- Save (ctrl+s) and exit editor (ctrl + x)
	- `fabric:profile-create camel-broker2`
	- `fabric:profile-change-parents camel-broker2 camel-jdbcpoc`
	- `fabric:profile-edit --pid amq camel-broker2`
	- On editor add: `port=61618`
	- Save (ctrl+s) and exit editor (ctrl + x)

4. Create broker for executing route. On karaf console run:
	- `fabric:container-create-child --profile camel-broker1 root JDBCPocBroker1`
	- `fabric:container-create-child --profile camel-broker2 root JDBCPocBroker2`
	- `watch container-list`
	![Camel broker 1 and 2 ](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture17.png)

# Testing Inserts

6. Compile and run spring-db-populator client
	- `cd <projects_dir>/spring-db-populator/`
	- Edit file `./src/main/resources/jdbc-spring-context.xml` and change beans activemq and activemq2 to point at both brokers groups ports 
	- `mvn clean package`
	- `java -jar ./target/spring-db-populator-0.0.1-SNAPSHOT.jar 1 5000 1000` <br/> 
	Note: On the client the parameters are 1 = start index, 5000 = how many inserts to execute, 1000 total parallel threads.
    
    This brokers now are listening at two master/slave groups. JDBCPocBroker1 is listening at messages arriving at JMS Brokers 61617 port and JDBCPocBroker2 is listening at messages arriving at JMS Brokers 61618 port.<br/>
    Run the client and check how fast its inserting data arriving from client.
    The client will insert using multithreading to both JMS ports.
    
# Testing HA

Test HA at JMS Brokers by running the client with 100000 inserts and shutdown one of the JMS Brokers. You will see on client how it is disconnested and the reconnected when slave broker of that group becomes master. You can also use ´cluster-list` command on karaf console to see how slave broker is now master after fail.

# Increasing performance

1. Create two more camel containers listening to JMS Brokers for parallel processing on each queue
	- `fabric:container-create-child --profile camel-broker1 root JDBCPocBroker3`
 	- `fabric:container-create-child --profile camel-broker2 root JDBCPocBroker4`
	- `watch container-list`<br/>
	![Camel broker 3 and 4 ](https://github.com/igl100/JBossFuseHADemo/blob/master/docs/image/Capture18.png)
    
   Now there are two camel brokers listening at messages at port 61617 and two more listenint at port 61618. Review that speed increase on inserts. Look how easy was to create two more camel brokers by using the same profiles we already created.
    
Thats all folks

    
	


