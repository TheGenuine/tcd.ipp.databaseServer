package de.reneruck.tcd.ipp.database.actions;

import de.reneruck.tcd.ipp.database.Connection;
import de.reneruck.tcd.ipp.fsm.Action;
import de.reneruck.tcd.ipp.fsm.TransitionEvent;

public class ShutdownConnection implements Action {

	private Connection connection;

	public ShutdownConnection(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void execute(TransitionEvent event) throws Exception {
		this.connection.setRunning(false);
	}

}
