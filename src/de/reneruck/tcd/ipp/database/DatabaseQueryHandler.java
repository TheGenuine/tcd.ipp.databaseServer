package de.reneruck.tcd.ipp.database;

import java.net.ConnectException;
import java.util.Date;
import java.util.Queue;

import de.reneruck.tcd.ipp.database.interServerCommunication.InterServerConnector;
import de.reneruck.tcd.ipp.datamodel.Statics;
import de.reneruck.tcd.ipp.datamodel.database.SqliteDatabaseConnection;
import de.reneruck.tcd.ipp.datamodel.exceptions.DatabaseException;
import de.reneruck.tcd.ipp.datamodel.transition.TemporalTransitionsStore;
import de.reneruck.tcd.ipp.datamodel.transition.Transition;
import de.reneruck.tcd.ipp.datamodel.transition.TransitionState;

public class DatabaseQueryHandler extends Thread {

	private boolean running = false;
	private Queue<Transition> queue;
	private TemporalTransitionsStore transitionQueue;
	private InterServerConnector interServerConnector;
	
	public DatabaseQueryHandler(Queue<Transition> queue,
			TemporalTransitionsStore transitionsQueue, InterServerConnector interServerConnector) {
		this.queue = queue;
		this.transitionQueue = transitionsQueue;
		this.interServerConnector = interServerConnector;
	}

	@Override
	public void run() {
		while (running) {
			checkQueue();
		}
	}

	private void checkQueue() {
		if(!this.queue.isEmpty())
		{
			applyTransition(this.queue.poll());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void applyTransition(Transition transition) {
		System.out.println("[DBQueryHandler] Handling transition " + transition.getTransitionId());
		try {
			if(TransitionState.ACKNOWLEGED.equals(transition.getTransitionState())) {
				this.transitionQueue.removeTransition(transition);
			} else if(TransitionState.PENDING.equals(transition.getTransitionState()) ) {
				
				SqliteDatabaseConnection connection = new SqliteDatabaseConnection(Statics.DB_FILE);
				transition.performTransition(connection);
				connection.close();
				
				// contact other database servers
				if(this.interServerConnector != null){
					this.interServerConnector.shareTransition(transition);
				}
				
				transition.setTransitionState(TransitionState.PROCESSED);
				transition.setHandlingDate(new Date(System.currentTimeMillis()));

				this.transitionQueue.addTransition(transition);
			} else {
				System.err.println("Invalid System TransitionState: " + transition.getTransitionState() + " no processing");
			}
		} catch (ConnectException e) {
			System.err.println(e.getMessage());
		} catch (DatabaseException e) {
			System.err.println(e.getMessage());
		}
	}

	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean running) {
		this.running = running;
	}

	public Queue<Transition> getQueue() {
		return this.queue;
	}
	
}
