package com.redhat.poc.jdbc.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class SQLConnectionFactory {

	private static String actualUrl = null;
	private static List<String> urls;
	private static int urlsIndex = 0;
	private static Properties props;

	public static Connection createConnection() {

		String url = getNextUrl();
		try {
			Connection conn = DriverManager.getConnection(url, props);
			ResultSet rs = conn.createStatement().executeQuery("select current_date");
			if (rs != null && rs.next()) {
				return conn;
			} else {
				throw new SQLException("Connection not valid");
			}
		} catch (SQLException e) {
			return createConnection();
		}

	}

	private static String getNextUrl() {
		if ((urlsIndex) == urls.size()) {
			urlsIndex = 0;
		}
		actualUrl = urls.get(urlsIndex);
		urlsIndex++;
		return actualUrl;
	}

	public static void setFailover(String url, Properties info) {
		props = info;
		if(urls == null){
			String parsedUrls = url.substring(url.indexOf("//")+2);
			List<String> urlList = new ArrayList<String>();
			StringTokenizer tokenizer = new StringTokenizer(parsedUrls,",");
			while(tokenizer.hasMoreTokens()){
				urlList.add("jdbc:postgresql://"+tokenizer.nextToken());
			}
			urls = urlList;
		}
	}

}
