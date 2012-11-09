package de.reneruck.tcd.ipp.database.interServerCommunication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.reneruck.tcd.ipp.datamodel.Airport;
import de.reneruck.tcd.ipp.datamodel.Statics;

/**
 * The {@link DatabaseServerDiscoverer} constitutes listening part of discovery service.<br>
 * It is joining the specified multicast group and listens for all incoming packages. 
 * Every incoming package from a new sender will be added to list of available servers.
 * This list is also available to the caller of this thread, in that way the outside 
 * world gets the discovered servers.
 * 
 * @author Rene
 *
 */
public class DatabaseServerDiscoverer extends Thread {

	private Map<InetAddress, Long> dbServers;
	private MulticastSocket listeningSocket;
	private boolean running;

	public DatabaseServerDiscoverer(Map<InetAddress, Long> dbServers) {
		this.dbServers = dbServers;
	}

	@Override
	public void run() {
		while(this.running) {
			if(this.listeningSocket != null && !this.listeningSocket.isClosed()){
				listenForDatabaseServers();
			} else {
				setupDatagramSocket();
			}
		}
		shutdown();
	}

	private void shutdown() {
		if(this.listeningSocket != null)
		{
			this.listeningSocket.disconnect();
			this.listeningSocket.close();
		} 
	}

	private void setupDatagramSocket() {
		try {
			InetAddress group = InetAddress.getByName("230.0.0.1");
			this.listeningSocket = new MulticastSocket(null);
			this.listeningSocket.joinGroup(group);
			this.listeningSocket.setReuseAddress(true);
			this.listeningSocket.bind(new InetSocketAddress(Statics.DISCOVERY_PORT));
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void listenForDatabaseServers() {
		if (this.listeningSocket != null) {
			byte[] buffer = new byte[100];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			
			try {
				this.listeningSocket.receive(packet);
				InetAddress address = packet.getAddress();
				
				if(!address.equals(InetAddress.getLocalHost())){
					//System.out.println("Discovered server at " + address);
					updateFoundAdresses(address);
				}
				Thread.sleep(1500);
			} catch (IOException e) {
				System.err.println("Failed to read socket " + e.getMessage());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("No listening socket available");
		}

	}

	private void updateFoundAdresses(InetAddress address) {
		updateCurrentAdress(address);
		removeOldEntries();
	}

	/**
	 * remove all entries thats last update is longer than 30 sec ago
	 */
	private void removeOldEntries() {
		Set<Entry<InetAddress, Long>> entrySet = this.dbServers.entrySet();
		long thresholdTime = System.currentTimeMillis() - 30000;
		for (Entry<InetAddress, Long> entry : entrySet) {
			if(thresholdTime > entry.getValue()){ 
				this.dbServers.remove(entry.getKey());
			}
		}
	}

	private void updateCurrentAdress(InetAddress address) {
		this.dbServers.put(address, System.currentTimeMillis());
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}

