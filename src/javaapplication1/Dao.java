package javaapplication1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Dao {
	// instance fields
	static Connection connect = null;
	Statement statement = null;

	// constructor
	public Dao() {
	  
	}

	public Connection getConnection() {
		// Setup the connection with the DB
		try {
			connect = DriverManager
					.getConnection("jdbc:mysql://www.papademas.net:3307/tickets?autoReconnect=true&useSSL=false"
							+ "&user=fp411&password=411");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connect;
	}

	// CRUD implementation

	public void createTables() {
		// variables for SQL Query table creations
		final String createTicketsTable = "CREATE TABLE pcrow_tickets(ticket_id INT AUTO_INCREMENT PRIMARY KEY, ticket_issuer VARCHAR(30), ticket_description VARCHAR(200), start_date DATE, end_date DATE)";
		final String createUsersTable = "CREATE TABLE pcrow_users(uid INT AUTO_INCREMENT PRIMARY KEY, uname VARCHAR(30), upass VARCHAR(30), admin int)";

		try {

			// execute queries to create tables

			statement = getConnection().createStatement();

			statement.executeUpdate(createTicketsTable);
			statement.executeUpdate(createUsersTable);
			System.out.println("Created tables in given database...");

			// end create table
			// close connection/statement object
			statement.close();
			//connect.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		// add users to user table
		addUsers();
	}

	public void addUsers() {
		// add list of users from userlist.csv file to users table

		// variables for SQL Query inserts
		//String sql;

		Statement statement;
		PreparedStatement pst;
		BufferedReader br;
		List<List<String>> array = new ArrayList<>(); // list to hold (rows & cols)

		// read data from file
		try {
			br = new BufferedReader(new FileReader(new File("./userlist.csv")));

			String line;
			while ((line = br.readLine()) != null) {
				array.add(Arrays.asList(line.split(",")));
			}
		} catch (Exception e) {
			System.out.println("There was a problem loading the file");
		}

		try {

			// Setup the connection with the DB

			statement = getConnection().createStatement();

			// create loop to grab each array index containing a list of values
			// and PASS (insert) that data into your User table
			for (List<String> rowData : array) {
				pst = getConnection().prepareStatement("INSERT INTO pcrow_users(uname,upass,admin) VALUES(?,?,?);");
				pst.setString(1, rowData.get(0));
				pst.setString(2, rowData.get(1));
				pst.setString(3, rowData.get(2));
				pst.executeUpdate();
				/*sql = "insert into jpapa_users(uname,upass,admin) " + "values('" + rowData.get(0) + "'," + " '"
						+ rowData.get(1) + "','" + rowData.get(2) + "');";
				statement.executeUpdate(sql);*/
				pst.close();
				//connect.close();
			}
			System.out.println("Inserts completed in the given database...");

			// close statement object
			statement.close();
			//connect.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public int insertRecords(String ticketName, String ticketDesc) {
		int id = 0;
		try {
			PreparedStatement pst = getConnection().prepareStatement("INSERT INTO pcrow_tickets (ticket_issuer, ticket_description, start_date) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
			pst.setString(1, ticketName);
			pst.setString(2, ticketDesc);
			
			LocalDate local = LocalDate.now();
			Date d = Date.from(local.atStartOfDay(ZoneId.systemDefault()).toInstant());
			java.sql.Date sqlDate = new java.sql.Date(d.getTime());
			
			pst.setDate(3, sqlDate);
			pst.executeUpdate();
			
			/*statement = getConnection().createStatement();
			statement.executeUpdate("Insert into jpapa_tickets" + "(ticket_issuer, ticket_description) values(" + " '"
					+ ticketName + "','" + ticketDesc + "')", Statement.RETURN_GENERATED_KEYS);
			*/
			// retrieve ticket id number newly auto generated upon record insertion
			ResultSet resultSet = null;
			resultSet = pst.getGeneratedKeys();
			if (resultSet.next()) {
				// retrieve first field in table
				id = resultSet.getInt(1);
			}
			resultSet.close();
			pst.close();
			//connect.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;

	}

	public ResultSet readRecords() {

		ResultSet results = null;
		try {
			statement = getConnection().createStatement();
			results = statement.executeQuery("SELECT * FROM pcrow_tickets");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return results;
	}
	// continue coding for updateRecords implementation
	
	public boolean updateRecords(int ticketID, String ticketName, String ticketDesc) {

		boolean valid = true;
		try {
			PreparedStatement pst = getConnection().prepareStatement("UPDATE pcrow_tickets SET ticket_issuer = ?, ticket_description = ? where ticket_id = ?;");
			pst.setString(1, ticketName);
			pst.setString(2, ticketDesc);
			pst.setInt(3, ticketID);
			pst.executeUpdate();
			pst.close();
			//connect.close();
		} catch (SQLException e2) {
			valid = false;
			e2.printStackTrace();
		}
		return valid;
	}

	// continue coding for deleteRecords implementation
	
	public boolean deleteRecords(int ticketID) {
		
		boolean valid = true;
		try {
			PreparedStatement pst = getConnection().prepareStatement("DELETE FROM pcrow_tickets WHERE ticket_id = ?");
			pst.setInt(1, ticketID);
			pst.executeUpdate();
			pst.close();
			//connect.close();
		} catch (SQLException e3) {
			valid = false;
			e3.printStackTrace();
		}
		return valid;
	}
	
	public boolean closeRecords(int ticketID) {
		
		boolean valid = true;
		try {
			PreparedStatement pst = getConnection().prepareStatement("UPDATE pcrow_tickets SET end_date = ?, ticket_description = CONCAT(ticket_description, ' | CLOSED.') WHERE ticket_id = ?;");
			pst.setInt(2, ticketID);
			
			LocalDate local = LocalDate.now();
			Date d = Date.from(local.atStartOfDay(ZoneId.systemDefault()).toInstant());
			java.sql.Date sqlDate = new java.sql.Date(d.getTime());
			
			pst.setDate(1, sqlDate);
			pst.executeUpdate();
			pst.close();
			//connect.close();
		} catch (SQLException e4) {
			valid = false;
			e4.printStackTrace();
		}
		return valid;
	}
}
