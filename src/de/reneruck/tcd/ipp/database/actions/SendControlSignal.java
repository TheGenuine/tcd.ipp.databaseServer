package de.reneruck.tcd.ipp.database.actions;

import java.io.ObjectOutputStream;

import de.reneruck.tcd.ipp.datamodel.Datagram;
import de.reneruck.tcd.ipp.fsm.Action;
import de.reneruck.tcd.ipp.fsm.TransitionEvent;

public class SendControlSignal implements Action {

	private String signal;
	private ObjectOutputStream out;

	public SendControlSignal(ObjectOutputStream out, String signal) {
		this.out = out;
		this.signal = signal;
	}

	@Override
	public void execute(TransitionEvent event) throws Exception {
		try {
			this.out.writeObject(new Datagram(this.signal));
			this.out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
