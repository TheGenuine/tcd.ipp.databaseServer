package de.reneruck.tcd.ipp.database;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.reneruck.tcd.ipp.datamodel.Airport;
import de.reneruck.tcd.ipp.datamodel.Booking;
import de.reneruck.tcd.ipp.datamodel.Statics;
import de.reneruck.tcd.ipp.datamodel.database.DBUtils;
import de.reneruck.tcd.ipp.datamodel.database.DatabaseConnection;
import de.reneruck.tcd.ipp.datamodel.database.MySqlDatabaseConnection;
import de.reneruck.tcd.ipp.datamodel.database.SqliteDatabaseConnection;
import de.reneruck.tcd.ipp.datamodel.exceptions.DatabaseException;
import de.reneruck.tcd.ipp.datamodel.transition.NewBookingTransition;
import de.reneruck.tcd.ipp.datamodel.transition.TemporalTransitionsStore;
import de.reneruck.tcd.ipp.datamodel.transition.Transition;

public class DatabaseTests {

	private DatabaseConnection databaseConnection;

	@Before
	public void setUp() throws FileNotFoundException, IOException, DatabaseException {
		this.databaseConnection = new SqliteDatabaseConnection("");
		DBUtils.setupDBStructure(this.databaseConnection);
		DBUtils.setupInitData(this.databaseConnection);
		DBUtils.insertTestData(this.databaseConnection);
	}

	@After
	public void tearDown() throws ConnectException {
		//clean up ... maybe
	}
	
	/**
	 * Checks for two different date to get different flights
	 * @throws ConnectException
	 */
	@Test
	public void testGetFlightByDate() throws ConnectException {
		// check flight from city to camp
		int flightForDate = this.databaseConnection.getFlightForDate(new Date(1351335600000L), Airport.city);
		// check flight from camp to city
		int flightForDate2 = this.databaseConnection.getFlightForDate(new Date(1351360800000L), Airport.camp);

		assertTrue(0 < flightForDate);
		assertTrue(0 < flightForDate2);
		assertTrue(flightForDate != flightForDate2);
	}

	/**
	 * Checks a date that does not exactly match the date in the flight plan
	 * @throws ConnectException
	 */
	@Test
	public void testGetFlightByDate2() throws ConnectException { 
		// check a time within the next hour
		
		//1361728800000
		//+1h 1361732400000
		//-1h 1361725200000
		int flightForDate = this.databaseConnection.getFlightForDate(new Date(1361732400000L), Airport.city);
		// check a time within the previous hour	
		int flightForDate2 = this.databaseConnection.getFlightForDate(new Date(1361725200000L), Airport.city);

		assertTrue(0 < flightForDate);
		assertTrue(0 < flightForDate2);
		assertEquals(flightForDate, flightForDate2); // both times we should find the same flight
	}

	/**
	 * Checks a date that does not match to a flight in the plan
	 * @throws ConnectException
	 */
	@Test
	@Ignore
	public void testGetFlightByDate3() throws ConnectException {
		int flightForDate = this.databaseConnection.getFlightForDate(new Date(1361732400000L), Airport.city);

		assertEquals(0, flightForDate);
	}

	/**
	 * Checks for an existing booking
	 * @throws ConnectException
	 */
	@Test
	public void testBookingExists() throws ConnectException {
		boolean bookingExists = this.databaseConnection.bookingExists(1351181776L);

		assertTrue(bookingExists);
	}

	/**
	 * Checks for a non existing booking
	 * @throws ConnectException
	 */
	@Test
	public void testBookingExists2() throws ConnectException {
		boolean bookingExists = this.databaseConnection.bookingExists(1349365314016L);

		assertFalse(bookingExists);
	}

	/**
	 * Find bookings for a flight
	 * @throws ConnectException
	 */
	@Test
	public void testGetBookingsForFlight() throws ConnectException {
		int bookingsCount = this.databaseConnection.getBookingCountForFlight(961);
		assertTrue(bookingsCount > 0);
	}
	
	/**
	 * Try to find a booking for a flight without bookings
	 * @throws ConnectException
	 */
	@Test
	public void testGetBookingsForFlight2() throws ConnectException {
		int bookingsCount = this.databaseConnection.getBookingCountForFlight(962);
		assertEquals(0, bookingsCount);
	}
	
	/**
	 * Get the correct number of bookings for a flight
	 * @throws ConnectException
	 */
	@Test
	public void testGetBookingsForFlight3() throws ConnectException {
		int bookingsCount = this.databaseConnection.getBookingCountForFlight(982);
		assertEquals(3, bookingsCount);
	}
	
	/**
	 * 
	 * @throws ConnectException
	 */
	@Test
	public void testMakeBooking() throws ConnectException {
		Booking booking = new Booking("Harry Horse", new Date(1353520800000L), Airport.city);
		
		int bookingsCount = this.databaseConnection.makeABooking(booking, 961);
		assertTrue(0 < bookingsCount);
	}
	
	/**
	 * 
	 * @throws ConnectException
	 */
	@Test
	public void testCancelBooking() throws ConnectException {
		int bookingsCountBefore = this.databaseConnection.getBookingsCount();
		this.databaseConnection.removeBooking(1351181776L);
		int bookingsCountAfter = this.databaseConnection.getBookingsCount();
		
		assertTrue(bookingsCountBefore > bookingsCountAfter);
	}
	
	/**
	 * 
	 * @throws ConnectException
	 * @throws DatabaseException 
	 * @throws SQLException 
	 */
	@Test
	public void testPersistTransitionQueueEntry() throws ConnectException, DatabaseException, SQLException {
		TemporalTransitionsStore queue = new TemporalTransitionsStore("");
		Booking booking = new Booking("Harry Horse", new Date(1353520800000L), Airport.city);
		Transition trans = new NewBookingTransition(booking);
		
		queue.addTransition(trans);
		
		sleep();
		
		ResultSet resultSet = this.databaseConnection.executeQuery("SELECT Transition_ID FROM TransitionQueue");
		int storedTransitionId = resultSet.getInt(1);
		assertEquals(trans.getTransitionId(), storedTransitionId);
	}

	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @throws ConnectException
	 * @throws DatabaseException 
	 * @throws SQLException 
	 */
	@Test
	public void testDePersistTransitionQueueEntry() throws ConnectException, DatabaseException, SQLException {
		TemporalTransitionsStore queue = new TemporalTransitionsStore("");
		Booking booking = new Booking("Harry Horse", new Date(1353520800000L), Airport.city);
		Transition trans = new NewBookingTransition(booking);
		
		queue.addTransition(trans);
		queue.addTransition(trans);
		queue.removeTransition(trans);
		
		ResultSet resultSet = this.databaseConnection.executeQuery("SELECT Transition_ID FROM TransitionQueue");
		assertTrue(resultSet.getRow() == 0);
	}
}
