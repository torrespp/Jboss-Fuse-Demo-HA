package com.redhat.poc.jdbc;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.redhat.poc.jdbc.exception.BusinessException;
import com.redhat.poc.jdbc.exception.ServiceInitializationException;

public class Main {

	private static Logger log = Logger.getLogger(Main.class);
	private ClassPathXmlApplicationContext ctx;

	public Main() throws ServiceInitializationException, BusinessException,
			SQLException {
		ctx = new ClassPathXmlApplicationContext("jdbc-spring-context.xml");
	}

	public void executeBulkTest(int initVal, int ammount, int threadPoolNumber) throws InterruptedException {
		log.debug("Iniciando simple test");
		ExecutorService service = Executors.newFixedThreadPool(50);
		for (int i = initVal; i < initVal+ammount; i++) {
			service.execute(new JdbcPocThread(i,ctx));
		}
		service.awaitTermination(2,TimeUnit.MINUTES);
		
	}

	public static void main(String[] args) {
		Main main;
		try {
			main = new Main();
			int initVal = Integer.parseInt(args[0]);	
			int ammount = Integer.parseInt(args[1]);
			int threadNumber = Integer.parseInt(args[2]);
			main.executeBulkTest(initVal, ammount, threadNumber);
		} catch (SQLException | ServiceInitializationException
				| BusinessException | InterruptedException e) {
			log.error("Error al ejecutar la prueba..", e);
		}
		System.exit(0);
	}

}
