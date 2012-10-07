package de.reneruck.tcd.ipp.database;

import java.net.ConnectException;
import java.util.Queue;

import de.reneruck.tcd.datamodel.NewBookingTransition;
import de.reneruck.tcd.datamodel.Transition;

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
		boolean bookingExists = false;
		try {
			bookingExists = MySqlConnection.bookingExists(transition.getBookingId());
		} catch (ConnectException e1) {
			e1.printStackTrace();
		}
		
		String generateSql = transition.generateSql();
		System.out.println(generateSql);
		if(!bookingExists)
		{
			if(transition instanceof NewBookingTransition)
			{
				try {
					MySqlConnection.executeSql(generateSql);
				} catch (ConnectException e) {
					e.printStackTrace();
				}
			}
			// TODO: what to do??
		} else {
			try {
				MySqlConnection.executeSql(generateSql);
			} catch (ConnectException e) {
				e.printStackTrace();
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
