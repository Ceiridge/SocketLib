package org.ceiridge.socketlib.packets;

public class SocketLibPacketMix {
	public SocketLibPacket packet;
	public int clientId;

	public SocketLibPacketMix(int clientId, SocketLibPacket packet) {
		this.clientId = clientId;
		this.packet = packet;
	}
}
