package com.redhat.poc.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.redhat.poc.jdbc.dto.JdbcPocDTO;
import com.redhat.poc.jdbc.exception.BusinessException;
import com.redhat.poc.jdbc.exception.ServiceInitializationException;

public class JdbcPocDAO implements JdbcPocDAOInterface {

	private DataSource dataSource = null;

	private Connection connection = null;

	private PreparedStatement pstmInsertJdbcDTO = null;

	private PreparedStatement pstmQueryJdbcDTO = null;

	private static Logger logger = Logger.getLogger(JdbcPocDAO.class.getName());

	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void init() throws ServiceInitializationException {

		try {
			connection = this.dataSource.getConnection();

			StringBuffer query = new StringBuffer();
			query.append(" SELECT * FROM JDBC_POC WHERE MODEL = ? ");
			pstmQueryJdbcDTO = connection.prepareStatement(query.toString());

			query = new StringBuffer();
			query.append(" INSERT INTO JDBC_POC ");
			query.append(" (ID, MODEL, MODEL_ID, INSERT_DATE) ");
			query.append(" VALUES (nextval('POC_SEQ'),?,?,sysdate)");
			pstmInsertJdbcDTO = connection.prepareStatement(query.toString());

		} catch (Exception e) {
			logger.error("Error al inicializar GpsDAO", e);
			throw new ServiceInitializationException(
					"Error al inicializar GpsDAO");
		}

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerJdbcPocRegistre(JdbcPocDTO jdbcPocDTO)
			throws BusinessException {
		try {

			// Se actualizan los parametros
			pstmInsertJdbcDTO.clearParameters();
			pstmInsertJdbcDTO.setString(1, jdbcPocDTO.getModel());
			pstmInsertJdbcDTO.setLong(2, jdbcPocDTO.getModelId());
			pstmInsertJdbcDTO.executeUpdate();

		} catch (Exception sqle) {
			logger.error("Ocurrio un error al insertar el registro", sqle);
			throw new BusinessException(
					"Ocurrio un error al insertar el registro");
		}
	}

	@Override
	public boolean jdbcPocExists(String model) throws BusinessException {
		boolean found = false;
		ResultSet rs = null;
		try {
			pstmQueryJdbcDTO.clearParameters();
			pstmQueryJdbcDTO.setString(1, model);
			rs = pstmQueryJdbcDTO.executeQuery();
			if (rs.next()) {
				found = true;
			}
		} catch (Exception e) {
			logger.error("Error al buscar el registro.", e);
			throw new BusinessException("Error al buscar el registro.");
		} 
		return found;
	}

	@Override
	public JdbcPocDTO searchJdbcPoc(String model) throws BusinessException {
		
		JdbcPocDTO dto = null;
		
		ResultSet rs = null;

		try {
			pstmQueryJdbcDTO.clearParameters();
			pstmQueryJdbcDTO.setString(1, model);
			rs = pstmQueryJdbcDTO.executeQuery();
			if (rs.next()) {
				dto = new JdbcPocDTO();
				dto.setId(rs.getInt("ID"));
				dto.setModel(rs.getString("MODEL"));
				dto.setModelId(rs.getInt("MODEL_ID"));
				dto.setInsertDate(rs.getDate("INSERT_DATE"));
			}
		} catch (Exception e) {
			logger.error("Error al buscar el registro.", e);
			throw new BusinessException("Error al buscar el registro.");
		} 

		return dto;
	}

}
