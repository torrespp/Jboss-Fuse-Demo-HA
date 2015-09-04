package com.redhat.poc.jdbc;

import com.redhat.poc.jdbc.dto.JdbcPocDTO;
import com.redhat.poc.jdbc.exception.BusinessException;

public interface JdbcPocDAOInterface {

	void registerJdbcPocRegistre(final JdbcPocDTO jdbcPocDTO) throws BusinessException;
}
