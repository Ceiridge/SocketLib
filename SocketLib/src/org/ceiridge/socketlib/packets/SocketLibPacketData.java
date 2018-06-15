package org.ceiridge.socketlib.packets;

import java.util.ArrayList;

public class SocketLibPacketData {

	public ArrayList<String> rawData;

	public SocketLibPacketData() {
		rawData = new ArrayList<String>();
	}

	public void writeData(String data) {
		rawData.add(data);
	}

	public String readData(int index) {
		return rawData.get(index);
	}
}
