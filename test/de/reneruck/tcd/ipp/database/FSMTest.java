package de.reneruck.tcd.ipp.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;

import de.reneruck.tcd.ipp.datamodel.Datagram;
import de.reneruck.tcd.ipp.datamodel.Statics;
import de.reneruck.tcd.ipp.datamodel.TemporalTransitionsStore;
import de.reneruck.tcd.ipp.fsm.FiniteStateMachine;
import de.reneruck.tcd.ipp.fsm.State;

public class FSMTest {

	private Connection connection;
	private ByteArrayOutputStream arrayOutputStream;

	@Before
	public void setUp() throws Exception {
		
		TemporalTransitionsStore transitionsStore = new TemporalTransitionsStore();
		Socket socketConnection = mock(Socket.class);
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
		oos.flush();
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		
		this.arrayOutputStream = new ByteArrayOutputStream();
		
		when(socketConnection.getOutputStream()).thenReturn(arrayOutputStream);
		when(socketConnection.getInputStream()).thenReturn(byteArrayInputStream);
		this.connection = new Connection(socketConnection, transitionsStore);
		this.connection.setRunning(false);
		Thread.sleep(500);
	}

	@After
	public void teardown() {
		this.connection = null;
		this.arrayOutputStream = null;
	}
	
	@Test
	public void testSyn() throws Exception {
		send(Statics.SYN);
		
		Thread.sleep(500);
		
		State fsmState = getCurrentFsmStateFromConnection();
		assertEquals("syn", fsmState.getIdentifier());
		
		Object readObject = readObjectOutputStream();
		assertTrue(readObject instanceof Datagram);
		assertEquals(Statics.ACK, ((Datagram)readObject).getType());
	}
	

	@Test
	public void testSynAck() throws Exception {
		send(Statics.SYN);
		Thread.sleep(500);
		this.arrayOutputStream.reset();
		
		State synState = getCurrentFsmStateFromConnection();
		
		send(Statics.SYNACK);
		Thread.sleep(500);
		
		State fsmState = getCurrentFsmStateFromConnection();
		assertEquals("waitRxMode", fsmState.getIdentifier());
	}
	
	@Test
	public void testRxModeReceive() throws Exception {
		send(Statics.SYN);
		Thread.sleep(100);
		this.arrayOutputStream.reset();
		
		send(Statics.SYNACK);
		Thread.sleep(100);
		this.arrayOutputStream.reset();
		
		send(Statics.RX_SERVER);
		Thread.sleep(100);
		Object readObject = readObjectOutputStream();
		
		assertTrue(readObject instanceof Datagram);
		assertEquals(Statics.RX_SERVER_ACK, ((Datagram)readObject).getType());
		
		State fsmState = getCurrentFsmStateFromConnection();
		assertEquals("ReceiveData", fsmState.getIdentifier());
	}
	
	private Object readObjectOutputStream() {
		byte[] byteArray = this.arrayOutputStream.toByteArray();
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(byteArray));
			this.arrayOutputStream.reset();
			return ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void callHandleInput(Object input) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class connectionClass = this.connection.getClass();
		Method handleInput = connectionClass.getDeclaredMethod("handleInput", Object.class);
		handleInput.setAccessible(true);
		handleInput.invoke(this.connection, input);
	}

	private State getCurrentFsmStateFromConnection() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Class connectionClass = this.connection.getClass();
		Field fsmField = connectionClass.getDeclaredField("fsm");
		fsmField.setAccessible(true);
		FiniteStateMachine fsm = (FiniteStateMachine) fsmField.get(this.connection);
		return fsm.getCurrentState();
	}

	private void send(String type) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		callHandleInput(new Datagram(type));
	}

}
