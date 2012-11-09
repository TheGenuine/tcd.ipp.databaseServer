package de.reneruck.tcd.ipp.databaseServer.actions;

import de.reneruck.tcd.ipp.databaseServer.Connection;
import de.reneruck.tcd.ipp.fsm.Action;
import de.reneruck.tcd.ipp.fsm.TransitionEvent;

public class ShutdownConnection implements Action {

	private Connection connection;

	public ShutdownConnection(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void execute(TransitionEvent event) throws Exception {
		this.connection.shutdown();
	}

}
