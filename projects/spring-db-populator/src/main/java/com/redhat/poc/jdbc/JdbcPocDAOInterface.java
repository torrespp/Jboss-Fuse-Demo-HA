package com.redhat.poc.jdbc;

import com.redhat.poc.jdbc.dto.JdbcPocDTO;
import com.redhat.poc.jdbc.exception.BusinessException;
import com.redhat.poc.jdbc.exception.ServiceInitializationException;

public interface JdbcPocDAOInterface {

	void init() throws ServiceInitializationException;

	void stop();

	void registerJdbcPocRegistre(final JdbcPocDTO jdbcPocDTO) throws BusinessException;

	boolean jdbcPocExists(final String model) throws BusinessException;

	JdbcPocDTO searchJdbcPoc(final String model) throws BusinessException;

}
