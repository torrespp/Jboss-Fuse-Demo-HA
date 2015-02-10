package com.redhat.poc.jdbc;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.redhat.poc.jdbc.JdbcPocDAO;
import com.redhat.poc.jdbc.dto.JdbcPocDTO;
import com.redhat.poc.jdbc.exception.BusinessException;
import com.redhat.poc.jdbc.exception.ServiceInitializationException;

public class Main {

	private static Logger log = Logger.getLogger(Main.class);
	private ClassPathXmlApplicationContext ctx;

	public Main() throws ServiceInitializationException, BusinessException,
			SQLException {
		ctx = new ClassPathXmlApplicationContext("jdbc-spring-context.xml");
	}

	public void executeBulkTest(int initVal, int ammount)
			throws ServiceInitializationException, BusinessException,
			SQLException {
		log.debug("Iniciando simple test");
		JdbcPocDAO dao = ctx.getBean(JdbcPocDAO.class);
		for (int i = initVal; i < initVal+ammount; i++) {
			log.debug("Iniciando Dao: "+i);
			dao.init();
			log.debug("Registrando modelo AAAA"+i+".txt");
			dao.registerJdbcPocRegistre(getJdbcPocDTO(i));
			log.debug("Deteniendo registrado");
			dao.stop();
			log.debug("Dao detenido");
		}
	}

	private JdbcPocDTO getJdbcPocDTO(int index) {
		JdbcPocDTO dto = new JdbcPocDTO();
		dto.setModel("AAAA" + index + ".txt");
		dto.setModelId(index);
		return dto;
	}

	public static void main(String[] args) {
		Main main;
		try {
			main = new Main();
			int initVal = Integer.parseInt(args[0]);
			int ammount = Integer.parseInt(args[1]);
			main.executeBulkTest(initVal, ammount);
		} catch (SQLException | ServiceInitializationException
				| BusinessException e) {
			log.error("Error al ejecutar la prueba..", e);
		}
		System.exit(0);
	}

}
