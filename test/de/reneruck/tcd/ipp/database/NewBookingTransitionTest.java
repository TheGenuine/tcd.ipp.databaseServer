package de.reneruck.tcd.ipp.database;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import de.reneruck.tcd.ipp.datamodel.Airport;
import de.reneruck.tcd.ipp.datamodel.Booking;
import de.reneruck.tcd.ipp.datamodel.database.SqliteDatabaseConnection;
import de.reneruck.tcd.ipp.datamodel.transition.NewBookingTransition;

public class NewBookingTransitionTest {

	private SqliteDatabaseConnection databaseConnection;

	@Before
	public void setUp() throws FileNotFoundException, IOException {
		this.databaseConnection = new SqliteDatabaseConnection("");
		TestData.setupDBStructure(this.databaseConnection);
		TestData.setupInitData(this.databaseConnection);
	}

	@Test
	public void test1() throws ConnectException{
		Booking booking =  new Booking("Karl Klammer", new Date(1361728800000L), Airport.city);
		NewBookingTransition transition = new NewBookingTransition(booking);
		transition.performTransition(this.databaseConnection);
	}
}
