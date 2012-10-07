package de.reneruck.tcd.ipp.database;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DatabaseServer server = new DatabaseServer();
		server.setRunning(true);
		server.start();
	}

}
