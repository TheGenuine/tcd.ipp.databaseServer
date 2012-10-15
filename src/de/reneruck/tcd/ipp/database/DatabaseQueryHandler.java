package de.reneruck.tcd.ipp.database;

import java.net.ConnectException;
import java.util.Queue;

import de.reneruck.tcd.ipp.datamodel.DatabaseConnection;
import de.reneruck.tcd.ipp.datamodel.Transition;
import de.reneruck.tcd.ipp.datamodel.TransitionState;

public class DatabaseQueryHandler extends Thread {

	private boolean running = false;
	private Queue<Transition> queue;
	private TransitionsQueue transitionQueue;
	
	public DatabaseQueryHandler(Queue<Transition> queue, TransitionsQueue transitionsQueue) {
		this.queue = queue;
		this.transitionQueue = transitionsQueue;
	}

	@Override
	public void run() {
		while (running) {
			if(!this.queue.isEmpty())
			{
				applyTransition(this.queue.poll());
			}
		}
	}
	
	private void applyTransition(Transition transition) {
		System.out.println("[DBQueryHandler] Handling transition " + transition.getBookingId());
		try {
			if(TransitionState.ACKNOWLEGED.equals(transition.getTransitionState())) {
				this.transitionQueue.removeTransition(transition);
			} else {
				transition.performTransition(new DatabaseConnection());
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
