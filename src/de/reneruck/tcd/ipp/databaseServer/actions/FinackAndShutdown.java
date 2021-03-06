package de.reneruck.tcd.ipp.databaseServer.actions;

import java.io.ObjectOutputStream;

import de.reneruck.tcd.ipp.databaseServer.Connection;
import de.reneruck.tcd.ipp.datamodel.Datagram;
import de.reneruck.tcd.ipp.datamodel.Statics;
import de.reneruck.tcd.ipp.datamodel.transition.TransitionExchangeBean;
import de.reneruck.tcd.ipp.fsm.Action;
import de.reneruck.tcd.ipp.fsm.TransitionEvent;

public class FinackAndShutdown implements Action {

	private TransitionExchangeBean bean;
	private Connection connection;

	public FinackAndShutdown(TransitionExchangeBean transitionExchangeBean, Connection connection) {
		this.bean = transitionExchangeBean;
		this.connection = connection;
	}

	@Override
	public void execute(TransitionEvent event) throws Exception {
		System.out.println("Sending> " + Statics.FINACK);
		ObjectOutputStream out = this.bean.getOut();
		out.writeObject(new Datagram(Statics.FINACK));
		out.flush();
		
		this.connection.shutdown();
	}

}
