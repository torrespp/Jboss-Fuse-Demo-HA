package com.redhat.poc.jdbc.utils;

import java.io.IOException;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class AMQConnectionFactory {

	private static AMQConnectionFactory instance;
	
	private String writerQueueName;
	private String readerQueueName;
	private String username;
	private String password;
	private QueueConnection queueConnection;
	private InitialContext ctx;

	private AMQConnectionFactory() {
		try {
			Properties configs = new Properties();
			configs.load(this.getClass().getClassLoader().getResourceAsStream("amq.properties"));
			this.writerQueueName = configs.getProperty("writerQueueName");
			this.readerQueueName = configs.getProperty("readerQueueName");
			this.username = configs.getProperty("username");
			this.password = configs.getProperty("password");
			
			Properties properties = new Properties();
			properties.put("java.naming.factory.initial",configs.getProperty("java.naming.factory.initial"));
		    properties.put(Context.PROVIDER_URL, configs.getProperty("provider.url"));
			properties.put("queue." + writerQueueName, writerQueueName);
			properties.put("queue." + readerQueueName, readerQueueName);
			ctx = new InitialContext(properties);
			QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) ctx.lookup("QueueConnectionFactory");
			this.queueConnection = queueConnectionFactory.createQueueConnection(username, password);
			this.queueConnection.start();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static AMQConnectionFactory getInstance(){
		if(instance == null){
			instance = new AMQConnectionFactory();
		}
		return instance;
	}
	
	public QueueConnection getQueueConnectionInstance(){
		return queueConnection;
	}
	
	public Queue getWriterQueue() throws NamingException{
		Queue queue = (Queue) ctx.lookup(writerQueueName);
		return queue;
	}
	
	public Queue getReaderQueue() throws NamingException{
		Queue queue = (Queue) ctx.lookup(readerQueueName);
		return queue;
	}
	
	public void closeConnection() throws JMSException{
		queueConnection.close();
	}
}
