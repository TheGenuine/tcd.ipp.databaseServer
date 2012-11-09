package de.reneruck.tcd.ipp.database;

import java.io.Console;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DatabaseServer server = new DatabaseServer();
		server.setRunning(true);
		server.start();
		
		Console console = System.console();

		if(console != null) {
			String input = "";
			while (!"exit".equals(input)){
				input = console.readLine();
			}
			server.setRunning(false);
			System.exit(0);
		}
	}

}
