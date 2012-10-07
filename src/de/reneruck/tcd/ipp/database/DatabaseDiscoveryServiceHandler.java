package de.reneruck.tcd.ipp.database;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import de.reneruck.tcd.datamodel.Statics;
import de.reneruck.tcd.datamodel.Utils;

public class DatabaseDiscoveryServiceHandler extends Thread {

	private boolean running;
	private DatagramSocket socket;

	public DatabaseDiscoveryServiceHandler() {
		System.out.println("Starting DatabaseDiscoveryServiceHandler");
	}
	
	@Override
	public void run() {
		while(this.running)
		{
			if(this.socket != null && this.socket.isBound())
			{
				read();
			} else {
				bind();
			}
		}
	}
	
	private void bind() {
		try {
			System.out.println("[DatabaseDiscoveryServiceHandler] Binding to Port " + Statics.DISCOVERY_PORT);
			this.socket = new DatagramSocket(Statics.DISCOVERY_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private void read() {
		try {
			DatagramPacket packet = new DatagramPacket(new byte[100], 100);
			System.out.println("[DatabaseDiscoveryServiceHandler] Listening for incoming packets");
			this.socket.receive(packet);
			byte[] trimArray = Utils.trimArray(packet.getData());
			byte[] bytes = Statics.SYN.getBytes();
			if(Arrays.equals(trimArray, bytes))
			{
				System.out.println("[DatabaseDiscoveryServiceHandler] Received packet from " + packet.getAddress());
				DatagramPacket returnPacket = new DatagramPacket(new byte[100], 100, packet.getAddress(), Statics.DISCOVERY_PORT+1);
				Thread.sleep(200);
				this.socket.send(returnPacket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
