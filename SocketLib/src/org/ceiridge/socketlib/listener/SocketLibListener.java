package org.ceiridge.socketlib.listener;

import org.ceiridge.socketlib.packets.SocketLibPacket;

public abstract class SocketLibListener {
	public SocketLibListener() {}

	public abstract void onPacketReceive(SocketLibPacket p);

	public abstract void onPacketSend(SocketLibPacket p);

}
