package com.redhat.poc.jdbc;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.redhat.poc.jdbc.dto.JdbcPocDTO;
import com.redhat.poc.jdbc.exception.BusinessException;

public class JdbcPocThread implements Runnable {

	private static Logger log = Logger.getLogger(JdbcPocThread.class);
	private ClassPathXmlApplicationContext ctx;

	private int id;

	public JdbcPocThread(int id, ClassPathXmlApplicationContext ctx) {
		this.id = id;
		this.ctx = ctx;
	}

	public void run() {
		JdbcPocDAO dao = ctx.getBean(JdbcPocDAO.class);
		log.debug("Iniciando Dao: " + id);
		try {
			log.debug("Registrando modelo " + id + ".txt");
			dao.registerJdbcPocRegistre(getJdbcPocDTO(id));
		} catch (BusinessException e) {
			log.error("Error al enviar el mensaje",e);
		}
	}

	private JdbcPocDTO getJdbcPocDTO(int index) {
		JdbcPocDTO dto = new JdbcPocDTO();
		dto.setModel("AAAA" + index + ".txt");
		dto.setModelId(index);
		return dto;
	}
}
