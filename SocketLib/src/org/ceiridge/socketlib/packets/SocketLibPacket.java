package org.ceiridge.socketlib.packets;


public abstract class SocketLibPacket {

	public SocketLibPacket() {}

	public SocketLibPacketData data = new SocketLibPacketData();

	public abstract void onSend(SocketLibPacketData sl);

	public abstract void onReceive(SocketLibPacketData sl);

}
