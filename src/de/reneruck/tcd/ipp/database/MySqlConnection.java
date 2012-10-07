package de.reneruck.tcd.ipp.database;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.reneruck.tcd.datamodel.Booking;


public class MySqlConnection {

    private static Connection dbConnection = null;
    
    // Hostname
    private static String dbHost = "127.0.0.1";
    // Port -- Standard: 3306
    private static String dbPort = "3306";
    // Datenbankname
    private static String database = "mydb";
    // Datenbankuser
    private static String dbUser = "root";
    // Datenbankpasswort
    private static String dbPassword = "password";
 
    private MySqlConnection() throws ConnectException {
        try {
 
            Class.forName("com.mysql.jdbc.Driver");
            dbConnection = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":"
                    + dbPort + "/" + database + "?" + "user=" + dbUser + "&"
                    + "password=" + dbPassword);
        } catch (ClassNotFoundException e) {
            throw new ConnectException("JDBC driver not found");
        } catch (SQLException e) {
            throw new ConnectException("No connection possible");
        }
    }
     
    private static Connection getInstance() throws ConnectException
    {
        if(dbConnection == null)
            new MySqlConnection();
        return dbConnection;
    }
 
    public static boolean bookingExists(long id) throws ConnectException {
    	dbConnection = getInstance();
    	
    	if(dbConnection != null)
    	{
    		try {
				Statement query = dbConnection.createStatement();
				String sql = "SELECT * FROM bookings WHERE ID=" + id;
				ResultSet result = query.executeQuery(sql);
				return result.first();
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    	return false;
    }

    public static void storeBooking(Booking booking) throws ConnectException
    {
    	dbConnection = getInstance();
    	
    	if(dbConnection != null)
    	{
    		// Anfrage-Statement erzeugen.
    		Statement query;
    		try {
    			query = dbConnection.createStatement();
    			
    			// Ergebnistabelle erzeugen und abholen.
    			String sql = "INSERT INTO booking(ID, forname, surname) " +
    					"VALUES(" + booking.getId() + ")";
    			query.executeUpdate(sql);
    			
    			// Es wird der letzte Datensatz abgefragt
    			ResultSet result = query.executeQuery("SELECT actor_id, first_name, last_name FROM actor ORDER BY actor_id desc LIMIT 1");
    			
    			// Wenn ein Datensatz gefunden wurde, wird auf diesen Zugegriffen 
    			if(result.next())
    			{
    				System.out.println("(" + result.getInt(1) + ")" + result.getString(2) + " " + result.getString(3));
    			}
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    }

	public static void executeSql(String sqlStatement) throws ConnectException {
		dbConnection = getInstance();
		
		if(dbConnection != null)
		{
			// Anfrage-Statement erzeugen.
			Statement query;
			try {
				query = dbConnection.createStatement();
				query.execute(sqlStatement);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
