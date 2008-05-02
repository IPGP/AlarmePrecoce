/**
 * Created Sat 08, 2008 19:39:09 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.utilities;

import org.apache.commons.configuration.*;
import java.sql.*;
import java.util.NoSuchElementException;
/**
 * Database heartbeat class. Updates the database on a regular basis.
 * @author Patrice Boissier
 */
public class DataBaseHeartBeat {
	private String host;
	private int port;
	private String database;
	private String user;
	private String password;
	private String driver;
	private String editor;
	protected int applicationNumber;
	private Connection connection;
	
	public DataBaseHeartBeat(Configuration configuration) throws ClassNotFoundException, ConversionException, NoSuchElementException, NullPointerException {
		host = new String(configuration.getString("dbms.host"));
		port = configuration.getInt("dbms.port");
		database = new String(configuration.getString("dbms.database"));
		user = new String(configuration.getString("dbms.user"));
		driver = new String(configuration.getString("dbms.driver"));
		editor = new String(configuration.getString("dbms.editor"));
		password = new String(configuration.getString("dbms.password"));
		applicationNumber = configuration.getInt("heartbeat.num_appli");
		loadDriver();
	}
	
	/**
	 * Loads the database driver and connect to the database.
	 * @throws ClassNotFoundException if the driver class is not found
	 */
	public void loadDriver() throws ClassNotFoundException {
		Class.forName(driver);
	}
	
	/**
	 * Connects to the database and updates the heartbeat message.
	 * @throws SQLException if the connection or query aborts.
	 * @return either the row count for the UPDATE statement, or 0 for SQL statements that return nothing
	 */
	public int sendHeartBeat(int message, String date) throws SQLException {
		String url = "jdbc:" + editor + "://" + host + ":" + port + "/" + database;
		connection = DriverManager.getConnection(url, user, password);
		Statement statement = connection.createStatement();
		String query = "UPDATE acquisition SET date_heure='" + date + "' where num_appli=" + applicationNumber + " AND num_type=" + message;
		int result = statement.executeUpdate(query);
		statement.close();
		connection.close();
		return result;
	}
}