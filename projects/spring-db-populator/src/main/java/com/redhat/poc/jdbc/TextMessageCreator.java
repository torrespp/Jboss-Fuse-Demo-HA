package com.redhat.poc.jdbc;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.core.MessageCreator;

public class TextMessageCreator implements MessageCreator {

	private String query;
	private int id;
	
	public TextMessageCreator(String query, int id){
		this.query = query;
		this.id = id;
	}
	
	@Override
	public Message createMessage(Session session) throws JMSException {
		TextMessage message = session.createTextMessage(query.toString());
		message.setJMSMessageID(String.valueOf(id));
//		message.setJMSMessageID(UUID.randomUUID().toString());
		return message;
	}

}
