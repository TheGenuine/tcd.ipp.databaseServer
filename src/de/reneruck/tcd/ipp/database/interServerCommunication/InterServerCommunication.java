package de.reneruck.tcd.ipp.database.interServerCommunication;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Date;

import com.google.gson.Gson;

import de.reneruck.tcd.ipp.datamodel.Datagram;
import de.reneruck.tcd.ipp.datamodel.Statics;
import de.reneruck.tcd.ipp.datamodel.database.SqliteDatabaseConnection;
import de.reneruck.tcd.ipp.datamodel.exceptions.DatabaseException;
import de.reneruck.tcd.ipp.datamodel.transition.TemporalTransitionsStore;
import de.reneruck.tcd.ipp.datamodel.transition.Transition;
import de.reneruck.tcd.ipp.datamodel.transition.TransitionState;

public class InterServerCommunication extends Thread {

	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private boolean running = true;
	private Gson gson;
	private TemporalTransitionsStore transitionQueue;

	public InterServerCommunication(Socket accept, TemporalTransitionsStore transitionQueue) {
		this.socket = accept;
		this.transitionQueue = transitionQueue;
		this.gson = new Gson();
	}

	@Override
	public void run() {
		try {
			InputStream inputStream = this.socket.getInputStream();
			this.out = new ObjectOutputStream(this.socket.getOutputStream());
			this.out.flush();
			this.in = new ObjectInputStream(inputStream);
			while (this.running) {
				handleInput(deserialize(this.in.readObject()));
				Thread.sleep(500);
			}
		this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private Object deserialize(Object readObject) {
		if (readObject instanceof String) {
			String input = (String) readObject;
			String[] split = input.split("=");
			if (split.length > 1) {
				try {
					Class<?> transitionClass = Class.forName(split[0]);
					Object fromJson = this.gson.fromJson(split[1].trim(), transitionClass);
					System.out.println("[ISC] Successfully deserialized ");
					return fromJson;
				} catch (ClassNotFoundException e) {
					e.getMessage();
					System.err.println("[ISC] Cannot deserialize, discarding package");
				}
			} else {
				System.err.println("[ISC] No valid class identifier found, discarding package");
			}
		}
		return readObject;
	}

	private void handleInput(Object input) throws IOException {
		if (input instanceof Datagram) {
			Datagram datagram = (Datagram) input;
			String type = datagram.getType();
			
			if(Statics.FIN.equals(type)){
				this.running = false;
				this.socket.close();
			} else {
				Transition transitionPayload = (Transition) datagram.getPayload(Statics.CONTENT_TRANSITION);
				handleTransition(transitionPayload);
			}
		} else {
			System.err.println("Unknown type " + input.getClass() + " discarding package");
		}
	}

	private void handleTransition(Transition transitionPayload) throws ConnectException, IOException {
		System.out.println("[ISC] Handling transition " + transitionPayload.getTransitionId());
		if(TransitionState.ACKNOWLEGED.equals(transitionPayload.getTransitionState())) {
			System.out.println("[ISC] Removing transition from transition queue");
			this.transitionQueue.removeTransition(transitionPayload);
		} else if(TransitionState.PENDING.equals(transitionPayload.getTransitionState()) ) {
			System.out.println("[ISC] Applying transition");
			
			try {
				SqliteDatabaseConnection connection = new SqliteDatabaseConnection(Statics.DB_FILE);
				transitionPayload.performTransition(connection);
				connection.close();
			} catch (DatabaseException e) {
				this.out.writeObject(new Datagram(Statics.ERR));
			}
			
			transitionPayload.setTransitionState(TransitionState.PROCESSED);
			transitionPayload.setHandlingDate(new Date(System.currentTimeMillis()));

			System.out.println("[ISC] Acknowleging");
			this.out.writeObject(new Datagram(Statics.ACK));
			
			System.out.println("[ISC] adding to transition queue");
			this.transitionQueue.addTransition(transitionPayload);
		} else {
			System.err.println("[ISC] Invalid System TransitionState: " + transitionPayload.getTransitionState() + " no processing");
		}
	}
}
