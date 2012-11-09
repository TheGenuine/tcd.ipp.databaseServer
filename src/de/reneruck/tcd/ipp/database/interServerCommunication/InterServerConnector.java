package de.reneruck.tcd.ipp.database.interServerCommunication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.reneruck.tcd.ipp.datamodel.Datagram;
import de.reneruck.tcd.ipp.datamodel.Statics;
import de.reneruck.tcd.ipp.datamodel.transition.TemporalTransitionsStore;
import de.reneruck.tcd.ipp.datamodel.transition.Transition;

/**
 * The {@link InterServerConnector} is the base class for the inter-server
 * communication. He waits for incoming connections from other servers. For
 * every new incoming connection a new {@link InterServerCommunication} Object
 * will be created to handle the communication.
 * 
 * @author Rene
 * 
 */
public class InterServerConnector extends Thread {

	private boolean running;
	private ServerSocket socket;
	private TemporalTransitionsStore transitionQueue;
	private Map<InetAddress, Long> serverNeighborhood;
	
	public InterServerConnector(TemporalTransitionsStore transitionsStore, Map<InetAddress, Long> serverNeighborhood) {
		this.transitionQueue = transitionsStore;
		this.serverNeighborhood = serverNeighborhood;
	}

	public InterServerConnector(Transition transition) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		while(this.running) {
			if(this.socket != null) {
				waitForIncomingMessage();
			} else {
				setupSocket();
			}
		}
	}
	
	private void waitForIncomingMessage() {
		try {
			Socket accept = this.socket.accept();
			System.out.println("New Inter-Server-Communication request from " + accept.getInetAddress());
			new InterServerCommunication(accept, this.transitionQueue).start();
			
			
			Thread.sleep(500);
		} catch (IOException e) {
			System.err.println("[ISC] Failed to read socket " + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void setupSocket() {
		try {
			this.socket = new ServerSocket(Statics.INTER_SERVER_COM_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean running) {
		this.running = running;
	}

	public void shareTransition(Transition transition) {
		Set<InetAddress> servers = this.serverNeighborhood.keySet();
		for (InetAddress server : servers) {
			contactServer(server, transition);
		}
	}

	private void contactServer(InetAddress server, Transition transition) {
		Socket socket = null;
		try {
			socket = new Socket(server, Statics.INTER_SERVER_COM_PORT);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			
			Map<String, Object> datagramContent = new HashMap<String, Object>();
			datagramContent.put(Statics.CONTENT_TRANSITION, transition);
			try {
				out.writeObject(new Datagram(Statics.DATA, datagramContent));
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Thread.sleep(500);
			// TODO currently we don't care what's coming back
			Object readObject = in.readObject();
			System.out.println("[ISC] <received: " + readObject);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if(socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
}
