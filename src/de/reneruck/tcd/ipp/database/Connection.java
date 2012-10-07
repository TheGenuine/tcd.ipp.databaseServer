package de.reneruck.tcd.ipp.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.google.gson.Gson;

import de.reneruck.tcd.datamodel.Statics;
import de.reneruck.tcd.datamodel.Transition;

public class Connection extends Thread {

	private Socket connection;
	private boolean running = false;
	private boolean clientAck;
	private Gson gson = new Gson();
	private ObjectOutputStream out;
	private ObjectInputStream in;

	public Connection(Socket connection) {
		this.connection = connection;
		this.running = true;
		this.start();
	}

	@Override
	public void run() {
		try {
			InputStream inputStream = this.connection.getInputStream();
			this.out = new ObjectOutputStream(this.connection.getOutputStream());
			this.out.flush();
			this.in = new ObjectInputStream(inputStream);
			while (this.running) {
				byte[] buffer = new byte[1000];
				handleInput(this.in.readObject());
			}
			Thread.sleep(500);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void send(String message) {
		try {
			System.out.println("Sending> " + message);
			this.out.writeObject(message);
			this.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleInput(Object input) {
		if(input instanceof String)
		{
			String message = (String) input;
			System.out.println("<Received " + message);
			
			if(message.equals(Statics.ACK+Statics.ACK)){
				this.clientAck = true;
			} else if(message.equals(Statics.SYN))
			{
				send(Statics.ACK);
			} else if(message.equals(Statics.FIN))
			{
				send(Statics.FINACK);
				try {
					Thread.sleep(500);
					this.in.close();
					this.out.close();
					this.connection.close();
					this.running = false;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else if(this.clientAck) {
				handleTransition(message);
			}
		} else {
			System.err.println("Unknown type " + input.getClass());
		}
		
	}

	private void handleTransition(String message) {
		String[] split = message.split("=");
		if(split.length > 1)
		{
			try {
				Class<?> transitionClass = Class.forName(split[0]);
				Transition fromJson = (Transition) this.gson.fromJson(split[1].trim(), transitionClass);
				System.out.println("Successfully deserialized ");
				System.out.println("ID: " + fromJson.getBookingId());
			} catch (ClassNotFoundException e) {
				e.getMessage();
				System.err.println("Discarding");
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
