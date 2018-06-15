package org.ceiridge.socketlib.listener;

import java.net.Socket;
import org.ceiridge.socketlib.packets.SocketLibPacket;

public abstract class SocketLibListenerServer {
	public SocketLibListenerServer() {}

	public abstract void onPacketReceive(int clientId, SocketLibPacket p);

	public abstract void onPacketSend(int clientId, SocketLibPacket p);

	public abstract void onJoin(int clientId, Socket socket);

	public abstract void onDisconnect(int clientId, Socket socket);

}
