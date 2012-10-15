package de.reneruck.tcd.ipp.database;

import java.net.ConnectException;
import java.util.Queue;

import de.reneruck.tcd.ipp.datamodel.DatabaseConnection;
import de.reneruck.tcd.ipp.datamodel.NewBookingTransition;
import de.reneruck.tcd.ipp.datamodel.Transition;

public class DatabaseQueryHandler extends Thread {

	private boolean running = false;
	private Queue<Transition> queue;
	
	public DatabaseQueryHandler(Queue<Transition> queue) {
		this.queue = queue;
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
			transition.performTransition(new DatabaseConnection());
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
