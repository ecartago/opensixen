package org.opensixen.server.manager.ui.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DB {

	private static Connection conn;

	// Hardcode. From Database.DB_NAMES
	public static String DB_POSTGRES = "PostgreSQL";
	public static String PORT_POSTGRES = "5432";
	public static String DB_ORACLE = "Oracle";
	public static String PORT_ORACLE = "1521";

	public static void init(Properties prop) throws SQLException {
		if (test(prop)) {
			String connectionString = null;
			if (DB_POSTGRES.equals(prop.get("type"))) {
				connectionString = getPostgresConnectionString(prop);
			}
			conn = DriverManager.getConnection(connectionString, prop.getProperty("UID"), prop.getProperty("PWD"));
		}
	}
	
	public static PreparedStatement getPsmt(String sql) throws SQLException	{
		return conn.prepareStatement(sql);
	}
	
	public static boolean connected()	{
		if (conn != null)	{
			return true;
		}
		return false;
	}

	/**
	 * Perform a test connection and thorws
	 * an exception if fails
	 * @param prop
	 * @return
	 * @throws SQLException
	 */
	public static boolean test(Properties prop) throws SQLException {
		String connectionString = null;
		if (DB_POSTGRES.equals(prop.get("type"))) {
			registerPostgreSQL();
			connectionString = getPostgresConnectionString(prop);
		} else {
			return false;
		}
		Connection test_conn = DriverManager.getConnection(connectionString,
				prop.getProperty("UID"), prop.getProperty("PWD"));
		DatabaseMetaData meta = test_conn.getMetaData();
		// gets driver info:
		System.out.println("JDBC driver version is " + meta.getDriverVersion());

		test_conn.close();
		test_conn = null;
		return true;
	}
	
	public static boolean silent_test(Properties prop) {
		try {
			return test(prop);
		}
		catch (SQLException e)	{
			return false;
		}		
	}

	/**
	 * Return connection String
	 * 
	 * @param prop
	 * @return
	 */
	private static String getPostgresConnectionString(Properties prop) {
		StringBuffer sb = new StringBuffer();
		sb.append("jdbc:postgresql://");
		sb.append(prop.get("DBhost"));
		sb.append(":").append(prop.get("DBport"));
		sb.append("/").append(prop.get("DBname"));
		return sb.toString();
	}

	private static void registerPostgreSQL() throws SQLException {
		Driver driver = new org.postgresql.Driver();
		DriverManager.registerDriver(driver);
	}

}
