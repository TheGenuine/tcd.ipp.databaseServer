package de.reneruck.tcd.ipp.database;

import java.net.ConnectException;
import java.util.Date;
import java.util.Queue;

import de.reneruck.tcd.ipp.datamodel.Statics;
import de.reneruck.tcd.ipp.datamodel.database.SqliteDatabaseConnection;
import de.reneruck.tcd.ipp.datamodel.transition.TemporalTransitionsStore;
import de.reneruck.tcd.ipp.datamodel.transition.Transition;
import de.reneruck.tcd.ipp.datamodel.transition.TransitionState;

public class DatabaseQueryHandler extends Thread {

	private boolean running = false;
	private Queue<Transition> queue;
	private TemporalTransitionsStore transitionQueue;
	
	public DatabaseQueryHandler(Queue<Transition> queue, TemporalTransitionsStore transitionsQueue) {
		this.queue = queue;
		this.transitionQueue = transitionsQueue;
	}

	@Override
	public void run() {
		while (running) {
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
				
				transition.setTransitionState(TransitionState.PROCESSED);
				transition.setHandlingDate(new Date(System.currentTimeMillis()));
				
				
				this.transitionQueue.addTransition(transition);
			} else {
				System.err.println("Invalid System TransitionState: " + transition.getTransitionState() + " no processing");
			}
		} catch (ConnectException e) {
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
