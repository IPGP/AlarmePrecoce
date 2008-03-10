/**
 * Created Sat 08, 2008 19:39:09 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.utilities;

import fr.ipgp.earlywarning.EarlyWarning;
import java.sql.*;
import java.util.*;
/**
 *  
 * @author Patrice Boissier
 *
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
	
	public DataBaseHeartBeat() throws ClassNotFoundException {
		host = new String(EarlyWarning.configuration.getString("dbms.host"));
		port = EarlyWarning.configuration.getInt("dbms.port");
		database = new String(EarlyWarning.configuration.getString("dbms.database"));
		user = new String(EarlyWarning.configuration.getString("dbms.user"));
		driver = new String(EarlyWarning.configuration.getString("dbms.driver"));
		editor = new String(EarlyWarning.configuration.getString("dbms.editor"));
		password = new String(EarlyWarning.configuration.getString("dbms.paassword"));
		applicationNumber = EarlyWarning.configuration.getInt("dbms.num_appli");
		loadDriverAndConnect();
		
	}
	
	public void loadDriverAndConnect() throws ClassNotFoundException {
		Class.forName(driver);
		String url = "jdbc:" + editor + "://" + host + ":" + port + "/" + database;
		System.out.println(url);
	}

	public void sendHeartBeat(){ 
	}
}