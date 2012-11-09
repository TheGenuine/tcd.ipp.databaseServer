package de.reneruck.tcd.ipp.database;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import de.reneruck.tcd.ipp.database.interServerCommunication.DatabaseServerDiscoverer;
import de.reneruck.tcd.ipp.database.interServerCommunication.InterServerConnector;
import de.reneruck.tcd.ipp.datamodel.Statics;
import de.reneruck.tcd.ipp.datamodel.database.DatabaseConnection;
import de.reneruck.tcd.ipp.datamodel.database.SqliteDatabaseConnection;
import de.reneruck.tcd.ipp.datamodel.exceptions.DatabaseException;
import de.reneruck.tcd.ipp.datamodel.transition.TemporalTransitionsStore;
import de.reneruck.tcd.ipp.datamodel.transition.Transition;

/**
 * The {@link DatabaseServer} is the base and starting control point of the
 * whole server. <br>
 * 
 * @author Rene
 * 
 */
public class DatabaseServer extends Thread {

	private boolean running = false;
	private ServerSocket socket;
	private Queue<Transition> dbQueue = new LinkedBlockingQueue<Transition>(new LinkedList<Transition>());
	private TemporalTransitionsStore transitionsStore;
	private DatabaseQueryHandler queryHandler;
	private DatabaseDiscoveryService discoveryHandler;
	private DatabaseServerDiscoverer databaseServerDiscoverer;
	private Map<InetAddress, Long> dbServersNeigborhood = new HashMap<InetAddress, Long>();
	private InterServerConnector interServerConnector;

	public DatabaseServer() {
		
		try {
			// for initalization of the database
			DatabaseConnection dbCon = new SqliteDatabaseConnection(Statics.DB_FILE);
			this.transitionsStore = new TemporalTransitionsStore(Statics.DB_FILE);
		} catch (ConnectException e) {
			e.printStackTrace();
		}catch (DatabaseException e) {
			e.printStackTrace();
		}
		
		this.databaseServerDiscoverer = new DatabaseServerDiscoverer(this.dbServersNeigborhood);
		this.databaseServerDiscoverer.setRunning(true);
		this.databaseServerDiscoverer.start();
		
		this.interServerConnector = new InterServerConnector(this.transitionsStore, this.dbServersNeigborhood);
		this.interServerConnector.setRunning(true);
		this.interServerConnector.start();
		
		this.queryHandler = new DatabaseQueryHandler(this.dbQueue, this.transitionsStore, this.interServerConnector);
		this.queryHandler.setRunning(true);
		this.queryHandler.start();
		
		this.discoveryHandler = new DatabaseDiscoveryService();
		this.discoveryHandler.setRunning(true);
		this.discoveryHandler.start();
	}
	
	@Override
	public void run() {
		while (this.running) {
			try {
				if (this.socket != null && this.socket.isBound()) {
					readFromSocket();
				} else {
					bind();
				}
				Thread.sleep(500);
			} catch (IOException e) {
				e.fillInStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void readFromSocket() {

		try {
			System.out.println("waiting for incoming connection");
			Socket connection = this.socket.accept();
			System.out.println("Connection received from " + connection.getInetAddress().getHostName());
			new Connection(connection, this.transitionsStore, this.queryHandler);
		} catch (IOException e) {
			System.err.println("Cannot read from channel " + e.getMessage());
		}
	}

	private void bind() throws IOException {
		this.socket = new ServerSocket(Statics.DB_SERVER_PORT, 10, InetAddress.getLocalHost());
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

}
