package com.redhat.poc.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class AmqJdbcDriver implements Driver {

	static {
		try {
			AmqJdbcDriver driverInst = new AmqJdbcDriver();
			DriverManager.registerDriver(driverInst);
			//System.setSecurityManager(new RMISecurityManager());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Connection connect(String url, Properties info) throws SQLException {
		AmqJdbcConnection conn = new AmqJdbcConnection(url, info);
		return conn;
	}

	public boolean acceptsURL(String url) throws SQLException {
		if(url.startsWith("jdbc:amqjdbc")){
			return true;
		}
		return false;
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean jdbcCompliant() {
		// TODO Auto-generated method stub
		return true;
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
