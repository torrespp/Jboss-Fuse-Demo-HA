# About hainserter

The purpose of this project is to create a camel rout that can be used in JBoss Fuse.
The route will read SQL standard commands from ActiveMQ **jdbcwriter** queue. As soon as a command arrive, route will read the command and execute it on postgresql database configured.<br/>
Database can be change to any JDBC complaint database by changing amq.properties file data and by changing pom.xml fabric8 maven plugin driver wrap.

# Explaining Camel Route

Camel route is defined at `.src/main/resources/META-INF/spring/camel-context.xml`. This file define some beans for it to work:<br/>

* OSGIX: <br/>`<osgix:cm-properties id="props" persistent-id="amq" />` <br/>This line import an amq.properties file so that we can retrieve configurations variables from OSGI container. Note that is important to import osgi namespaces at camel-context.xml file. The namespaces required are<br/> 
```XML
xmlns:osgi="http://www.springframework.org/schema/osgi" xmlns:osgix="http://www.springframework.org/schema/osgi-compendium”
``` 
and schema imports are 
```XML
http://www.springframework.org/schema/osgi http://www.springframework.org/schema/osgi/spring-osgi.xsd
http://www.springframework.org/schema/osgi-compendium http://www.springframework.org/schema/osgi-compendium/spring-osgi-compendium.xsd">
```

* Properties holder:<br/>
`<context:property-placeholder properties-ref="props"></context:property-placeholder>` <br/>
define property file reader.

* ActiveMQ: <br/>
```XML
<bean id="activemq" class="org.apache.activemq.ActiveMQConnectionFactory">
		<property name="brokerURL"
			value="failover:(tcp://${ipaddress}:${port})?jms.optimizeAcknowledge=true&amp;jms.prefetchPolicy.queuePrefetch=5000&amp;timeout=3000" />
		<property name="userName" value="${username}" />
		<property name="password" value="${password}" />
	</bean>
``` 
<br/>
defines activemq configuration. Note how **failover** protocol is enabled.
* Datasource: <br/>
```XML
<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource"
		destroy-method="close">
		<property name="driverClassName" value="org.postgresql.Driver" />
		<property name="url"
			value="jdbc:postgresql://${dbipaddress}:${dbport}/${dbname}" />
		<property name="username" value="${dbusername}" />
		<property name="password" value="${dbpassword}" /> 
</bean>
``` 
defines database access data. Note how values are mapped to amq.properties variables. Change this variables if needed.

* CamelContext:<br/>
```XML
<camelContext xmlns="http://camel.apache.org/schema/spring">
  <onException>
    <exception>java.lang.Exception</exception>
    <redeliveryPolicy maximumRedeliveries="3" redeliveryDelay="6000" retryAttemptedLogLevel="WARN"/>
    <handled>
      <constant>true</constant>
    </handled>
    <to uri="activemq:jdbcwriter"/>
  </onException>
  <route>
    <from uri="activemq:jdbcwriter"/>
    <to uri="jdbc:dataSource"/>
  </route>
</camelContext>
```
CamelContext defines all camel route. As you can see, it defines how will it react in case of exception `java.lang.Exception`. If this exception is thrown it will try to complete execution 3 times `maximumRedeliveries="3"` waiting 6000 ms on every retry `redeliveryDelay="6000"`and writing a warning to log each time `retraAttemptedLogLevel="WARN"`.<br/><br/>
After defining error policy, camel context define where will the input message come `<from uri="activemq:jdbcwriter"/>`(activemq bean and jdbcwriter queue) and where will it route `<to uri="jdbc:dataSource"/>` (The content inside payload message which are sql commands will be executed inside datasource)

# POM.xml fabric deploy definition

In order for `fabric8:deploy` command to be executed we need to define what profile and settings will be used inside pom.xml file.

## Apache maven felix plugin definition

Apache felix maven plugin will add MANIFEST.MF parameters so the jar can be deploy on a OSGI container. <br/>

```XML 
		  <plugin>
				<groupId>org.apache.felix</groupId>
				<artifactId>maven-bundle-plugin</artifactId>
				<version>2.5.3</version>
				<extensions>true</extensions>
				<configuration>
					<instructions>
						<Export-Package>
                        com.redhat.poc.fimpe.hainserter.*
                        </Export-Package>
						<Bundle-SymbolicName>
                        	${pom.artifactId}
                        </Bundle-SymbolicName>
						<Import-Package>
							org.apache.activemq.*,
							org.springframework.*,
							org.postgresql.*,
							org.apache.commons.dbcp.*,
							javax.sql.*,
							org.apache.camel.*
						</Import-Package>
						<DynamicImport-Package>

						</DynamicImport-Package>
						<Include-Resource>src/main/resources</Include-Resource>
						<_failok>true</_failok>
					</instructions>
				</configuration>
			</plugin>
			
```

In this case we are exporting com.redhat.poc.fimpe.hainserter.* classes so they will be available for other osgi bundles. The `<Import-Package>` define what packages we need to import from osgi containers and `Bundle-SymbolicName` defines what name will bundle set inside osgi container.<br/>
`<Include-Resource>` instruct to insert everything inside /src/main/resourges into bundle.

## fabric maven plugin definition

This plugin defines what profile will be created inside fuse and what features will it import.

```XML
		<plugin>
				<groupId>io.fabric8</groupId>
				<artifactId>fabric8-maven-plugin</artifactId>
                <version>1.2.0.Beta4</version>
				<configuration>
					<profile>camel-jdbcpoc</profile>
					<jolokiaUrl>
                    http://localhost:8181/jolokia
                    </jolokiaUrl>
					<parentProfiles>
                    feature-camel mq-base feature-camel-jms
                    </parentProfiles>
					<features>
                    camel-jms camel-spring spring camel activemq-camel spring-jms spring-jdbc camel-jdbc camel-sql
                    </features>
					<bundles>
                    wrap:mvn:org.postgresql/postgresql/9.2-1002-jdbc4 mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.commons-dbcp/1.4_3
                    </bundles>
				</configuration>
			</plugin>
```

Using `<configuration>` tag we define that the new profile name will be **camel-jdbcpoc**. The `<jolokiaUrl>` defines where the fuse fabric is running (change this for remote deployment), `<parentProfiles>` define which profiles will be this profile parents and `<features>` define what fuse features we need. <br/>
Look at `<bundles>` tag. It defines what other osgi bundles we need to import. This bundles MUST to exist and be installed on maven repository. It is also important to notice the `wrap:` prefix on postgres driver definition. In order to use jars inside osgi bunles, this jars must have MANIFEST.MF osgi data (like we defined on apache felix maven plugin). This data is no existing on many jars so the **wrap** prefix instruct fabric plugin to create this data inside the jar manifest (In this case postgresql adbc driver jar).  Use wrap prefix on any jar needed inside maven with is not a bundle.<br/><br/>
If you want to try this demo using a different database (mysql, sqlserver, oracle, etc) change `wrap:mvn:org.postgresql/postgresql/9.2-1002-jdbc4` and point it to database jdbc driver (Ex. `wrap:mvn:ojdbc/ojdbc/14` for oracle)

# Running installation

In order tu install this project to JBoss Fuse run: `mvn clean install fabric8:deploy` and use fabric username and password when maven ask for them.

