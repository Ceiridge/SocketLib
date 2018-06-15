package org.ceiridge.socketlib.main;

import java.net.Socket;

public class SocketLibIdMix {
	public Socket socket;
	public Integer id;

	public SocketLibIdMix(Socket socket, Integer id) {
		this.socket = socket;
		this.id = id;
	}
}
