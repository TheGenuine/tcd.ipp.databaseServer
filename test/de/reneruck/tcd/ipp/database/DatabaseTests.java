package de.reneruck.tcd.ipp.database;

import static org.junit.Assert.*;

import java.net.ConnectException;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.reneruck.tcd.ipp.datamodel.Airport;
import de.reneruck.tcd.ipp.datamodel.Booking;
import de.reneruck.tcd.ipp.datamodel.DatabaseConnection;

public class DatabaseTests {

	private DatabaseConnection databaseConnection;

	@Before
	public void setUp() throws ConnectException {
		this.databaseConnection = new DatabaseConnection();
		// insert example data
	}

	@After
	public void tearDown() {
		// clean up
	}
	
	@Test
	public void testGetFlightByDate() throws ConnectException {
		int flightForDate = this.databaseConnection.getFlightForDate(new Date(1351328400000L), Airport.city);

		assertTrue(0 < flightForDate);
	}

	@Test
	public void testGetFlightByDate2() throws ConnectException {
		int flightForDate = this.databaseConnection.getFlightForDate(new Date(1351328497435L), Airport.city);

		assertTrue(0 < flightForDate);
	}

	@Test
	public void testGetFlightByDate3() throws ConnectException {
		int flightForDate = this.databaseConnection.getFlightForDate(new Date(1451328400000L), Airport.city);

		assertEquals(0, flightForDate);
	}

	@Test
	public void testBookingExists() throws ConnectException {
		boolean bookingExists = this.databaseConnection.bookingExists(1349365314015L);

		assertTrue(bookingExists);
	}

	@Test
	public void testBookingExists2() throws ConnectException {
		boolean bookingExists = this.databaseConnection.bookingExists(1349365314016L);

		assertFalse(bookingExists);
	}

	@Test
	public void testGetBookingsForFlight() throws ConnectException {
		int bookingsCount = this.databaseConnection.getBookingCountForFlight(19);
		assertTrue(bookingsCount > 0);
	}
	
	@Test
	public void testGetBookingsForFlight2() throws ConnectException {
		int bookingsCount = this.databaseConnection.getBookingCountForFlight(19);
		assertEquals(2, bookingsCount);
	}
	
	@Test
	public void testMakeBooking() throws ConnectException {
		Booking booking = new Booking("John Doe", new Date(1351328400000L), Airport.city);
		
		int bookingsCount = this.databaseConnection.makeABooking(booking, 19);
		assertTrue(0 < bookingsCount);
	}
}
