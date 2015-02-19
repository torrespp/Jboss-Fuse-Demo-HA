package com.redhat.poc.jdbc;

import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;

import com.redhat.poc.jdbc.dto.JdbcPocDTO;
import com.redhat.poc.jdbc.exception.BusinessException;

public class JdbcPocDAO implements JdbcPocDAOInterface {

	private JmsTemplate jmsTemplate = null;
	
	private JmsTemplate jmsTemplate2 = null;


	private static Logger logger = Logger.getLogger(JdbcPocDAO.class.getName());

	@Override
	public void registerJdbcPocRegistre(JdbcPocDTO jdbcPocDTO)
			throws BusinessException {
		StringBuffer query = new StringBuffer();
		query.append(" INSERT INTO JDBC_POC ");
		query.append(" (ID, MODEL, MODEL_ID, INSERT_DATE) ");
		query.append(" VALUES (nextval('POC_SEQ'),'");
		query.append(jdbcPocDTO.getModel());
		query.append("',");
		query.append(jdbcPocDTO.getModelId());
		query.append(",current_date)");
		String q = query.toString();
		logger.debug("Query: "+q);
		if(!isOdd(jdbcPocDTO.getModelId())){
			jmsTemplate.send(new TextMessageCreator(q, jdbcPocDTO.getModelId()));
		}else{
			jmsTemplate2.send(new TextMessageCreator(q, jdbcPocDTO.getModelId()));
		}
	}
	
	public JmsTemplate getJmsTemplate2() {
		return jmsTemplate2;
	}

	public void setJmsTemplate2(JmsTemplate jmsTemplate2) {
		this.jmsTemplate2 = jmsTemplate2;
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public static boolean isOdd(int num){
	    return num % 2 != 0;
	}
}
