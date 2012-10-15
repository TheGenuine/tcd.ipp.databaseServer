package de.reneruck.tcd.ipp.database;

import static org.junit.Assert.*;

import java.net.ConnectException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import de.reneruck.tcd.ipp.datamodel.Airport;
import de.reneruck.tcd.ipp.datamodel.DatabaseConnection;

public class DatabaseTests {

	private DatabaseConnection databaseConnection;

	@Before
	public void setUp() {
		try {
			this.databaseConnection = new DatabaseConnection();
		} catch (ConnectException e) {
			e.printStackTrace();
		}
	}	
	
	@Test
	public void testGetFlightByDate() {
		try {
			int flightForDate = this.databaseConnection.getFlightForDate(new Date(1351328400000L), Airport.city);
			
			assertNotSame(0, flightForDate);
			
		} catch (ConnectException e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testGetFlightByDate2() {
		try {
			int flightForDate = this.databaseConnection.getFlightForDate(new Date(1351328497435L), Airport.city);
			
			assertNotSame(0, flightForDate);
			
		} catch (ConnectException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetFlightByDate3() {
		try {
			int flightForDate = this.databaseConnection.getFlightForDate(new Date(1451328400000L), Airport.city);
			
			assertEquals(0, flightForDate);
			
		} catch (ConnectException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBookingExists() {
		try {
			boolean bookingExists = this.databaseConnection.bookingExists(1349365314015L);
			
			assertTrue(bookingExists);
			
		} catch (ConnectException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBookingExists2() {
		try {
			boolean bookingExists = this.databaseConnection.bookingExists(1349365314016L);
			
			assertFalse(bookingExists);
			
		} catch (ConnectException e) {
			e.printStackTrace();
		}
	}
}
