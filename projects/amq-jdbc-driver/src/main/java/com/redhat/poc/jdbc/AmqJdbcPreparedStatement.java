package com.redhat.poc.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.redhat.poc.jdbc.utils.AMQConnectionFactory;
import com.redhat.poc.jdbc.utils.SQLConnectionFactory;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class AmqJdbcPreparedStatement implements PreparedStatement {

	private static final Logger log = Logger.getLogger(AmqJdbcPreparedStatement.class);
	private static AMQConnectionFactory amqFactory = AMQConnectionFactory
			.getInstance();

	private QueueSession queueSession;
	private QueueConnection queueConnection;
	private Queue writerQueue;
	private Queue readerQueue;
	private QueueSender writerQueueSender;
	private QueueSender readerQueueSender;
	private String query;

	private Map params;

	public AmqJdbcPreparedStatement(String query) throws JMSException,
			NamingException {
		queueConnection = amqFactory.getQueueConnectionInstance();
		queueSession = queueConnection.createQueueSession(false,
				Session.AUTO_ACKNOWLEDGE);
		writerQueue = amqFactory.getWriterQueue();
		writerQueueSender = queueSession.createSender(writerQueue);
		readerQueue = amqFactory.getReaderQueue();
		readerQueueSender = queueSession.createSender(readerQueue);
		this.query = query;
		params = new HashMap();
	}

	private Object executeAmqRequest(boolean writeToDb) throws SQLException {
		return executeAmqRequest(this.query, writeToDb);
	}

	public int executeUpdate(String sql) throws SQLException {
		return 0;
	}

	public void close() throws SQLException {
		try {
			writerQueueSender.close();
			readerQueueSender.close();
			queueSession.close();
		} catch (JMSException e) {
			throw new SQLException("Cannot close sender and session");
		}
	}

	public int getMaxFieldSize() throws SQLException {
		return 0;
	}

	public void setMaxFieldSize(int max) throws SQLException {
		
	}

	public int getMaxRows() throws SQLException {
		return 0;
	}

	public void setMaxRows(int max) throws SQLException {
		
	}

	public void setEscapeProcessing(boolean enable) throws SQLException {
		
	}

	public int getQueryTimeout() throws SQLException {
		return 0;
	}

	public void setQueryTimeout(int seconds) throws SQLException {
		
	}

	public void cancel() throws SQLException {
		
	}

	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	public void clearWarnings() throws SQLException {
		
	}

	public void setCursorName(String name) throws SQLException {
		
	}

	public boolean execute(String sql) throws SQLException {
		return false;
	}

	public ResultSet getResultSet() throws SQLException {
		return null;
	}

	public int getUpdateCount() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean getMoreResults() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setFetchDirection(int direction) throws SQLException {
		// TODO Auto-generated method stub

	}

	public int getFetchDirection() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setFetchSize(int rows) throws SQLException {
		// TODO Auto-generated method stub

	}

	public int getFetchSize() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getResultSetConcurrency() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getResultSetType() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public void addBatch(String sql) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void clearBatch() throws SQLException {
		// TODO Auto-generated method stub

	}

	public int[] executeBatch() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Connection getConnection() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getMoreResults(int current) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public ResultSet getGeneratedKeys() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean execute(String sql, String[] columnNames)
			throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public int getResultSetHoldability() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isClosed() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setPoolable(boolean poolable) throws SQLException {
		// TODO Auto-generated method stub

	}

	public boolean isPoolable() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void closeOnCompletion() throws SQLException {
		// TODO Auto-generated method stub

	}

	public boolean isCloseOnCompletion() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public ResultSet executeQuery() throws SQLException {
		return executeQuery(this.query);
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		String sqlRequest = prepareSQL(sql);
		log.info("Query to execute: "+sqlRequest);
		Connection conn = SQLConnectionFactory.createConnection();
		Statement stm = conn.createStatement();
		ResultSet rs = stm.executeQuery(sqlRequest);
		return rs;
//		try {
//			MapMessage response = (MapMessage) executeAmqRequest(sql, false);
//			Map data = new HashMap();
//			Enumeration enume = response.getMapNames();
//			while (enume.hasMoreElements()) {
//				String mapName = (String) enume.nextElement();
//				Object value = response.getObject(mapName);
//				data.put(mapName, value);
//			}
//			AmqJdbcResultSet resultset = new AmqJdbcResultSet(data);
//			return resultset;
//		} catch (JMSException e) {
//			throw new SQLException("Cannot retrieve message", e);
//		}
	}

	public int executeUpdate() throws SQLException {
		Map response = (Map) executeAmqRequest(true);
		int responseMessage = (Integer) response.get("result");
		return responseMessage;

	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		params.put(parameterIndex, new Boolean(x));
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		params.put(parameterIndex, new Byte(x));
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		params.put(parameterIndex, new Short(x));
	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		params.put(parameterIndex, new Integer(x));
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		params.put(parameterIndex, new Long(x));

	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		params.put(parameterIndex, new Float(x));

	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		params.put(parameterIndex, new Double(x));
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x)
			throws SQLException {

	}

	public void setString(int parameterIndex, String x) throws SQLException {
		params.put(parameterIndex, x);
	}

	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		params.put(parameterIndex, x);
	}

	public void setDate(int parameterIndex, Date x) throws SQLException {
		params.put(parameterIndex, x);
	}

	public void setTime(int parameterIndex, Time x) throws SQLException {
		params.put(parameterIndex, x);
	}

	public void setTimestamp(int parameterIndex, Timestamp x)
			throws SQLException {
		params.put(parameterIndex, x);
	}

	public void setAsciiStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		
	}

	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		
	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		
	}

	public void clearParameters() throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public boolean execute() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void addBatch() throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setCharacterStream(int parameterIndex, Reader reader, int length)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setRef(int parameterIndex, Ref x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setClob(int parameterIndex, Clob x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setArray(int parameterIndex, Array x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public ResultSetMetaData getMetaData() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDate(int parameterIndex, Date x, Calendar cal)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setTime(int parameterIndex, Time x, Calendar cal)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setNull(int parameterIndex, int sqlType, String typeName)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setURL(int parameterIndex, URL x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public ParameterMetaData getParameterMetaData() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setNString(int parameterIndex, String value)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setObject(int parameterIndex, Object x, int targetSqlType,
			int scaleOrLength) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setAsciiStream(int parameterIndex, InputStream x)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setBinaryStream(int parameterIndex, InputStream x)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setCharacterStream(int parameterIndex, Reader reader)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setBlob(int parameterIndex, InputStream inputStream)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		// TODO Auto-generated method stub

	}

	private String prepareSQL(String sqlString){
		String preparedString = new String(sqlString);
		Set keys = params.keySet();
		for (Object key : keys) {
			Object value = params.get(key);
			if (value instanceof Integer) {
				preparedString = preparedString.replaceFirst("[?]",
						String.valueOf(value));
			} else if (value instanceof Long) {
				preparedString = preparedString.replaceFirst("[?]",
						String.valueOf(value));
			}else if (value instanceof Double) {
				preparedString = preparedString.replaceFirst("[?]",
						String.valueOf(value));
			}else if (value instanceof Timestamp) {
				preparedString = preparedString.replaceFirst("[?]",
						formatTimestamp((Timestamp)value));
			}else {
				preparedString = preparedString.replaceFirst("[?]", "'"
						+ (String) value + "'");
			}
			preparedString = preparedString.replaceFirst("sysdate", "current_date");
		}
		return preparedString;
	}
	
	private String formatTimestamp(Timestamp value){
		Date date = new Date(value.getTime());
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		StringBuffer buff = new StringBuffer();
		buff.append("'");
		buff.append(format.format(date));
		buff.append("'");
		return buff.toString();
	}
	
	private Object executeAmqRequest(String sqlString, boolean writeToDb)
			throws SQLException {
		try {
			String preparedString = prepareSQL(sqlString);

			log.debug("PSTM query message: "+preparedString);
			TextMessage textMessage = queueSession
					.createTextMessage(preparedString);

			if (writeToDb) {
				//Destination tempDest = queueSession.createTemporaryQueue();
				//MessageConsumer responseConsumer = queueSession.createConsumer(tempDest);
				//textMessage.setJMSReplyTo(tempDest);
				//textMessage.setJMSCorrelationID(UUID.randomUUID().toString());
				writerQueueSender.send(textMessage);
				//Object responseMessage = responseConsumer.receive(120000l);
				//responseConsumer.receive(120000l);
				Map map = new HashMap();
				map.put("result", new Integer(1));
				return map;
			} else {
				Destination tempDest = queueSession.createTemporaryQueue();
				MessageConsumer responseConsumer = queueSession
						.createConsumer(tempDest);
				textMessage.setJMSReplyTo(tempDest);
				textMessage.setJMSCorrelationID(UUID.randomUUID().toString());
				readerQueueSender.send(textMessage);
				Object responseMessage = responseConsumer.receive(120000l);
				return responseMessage;
			}

		} catch (JMSException e) {
			throw new SQLException("Error in JMS operations", e);
		} catch (Exception e) {
			throw new SQLException("Error in JMS operations", e);
		} finally{
			try{
				queueSession.close();
			}catch(JMSException e){
				log.error("Error closing jms session",e);
			}
		}
	}

}
