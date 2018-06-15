package org.ceiridge.socketlib.util;

import java.util.HashMap;

public class SecurityUtils {
	public HashMap<String, Integer> connectionsPerIP = new HashMap<String, Integer>();

	public int maxConsperIP = 30;
	public int minDelayBetweenNewPackets = 0;
	public int maxPacketLength = 100000;
	public int maxPacketsPerSecond = 200;

	/** Default: 30 */
	public void setMaxConnectionsPerIP(int maximum) {
		maxConsperIP = maximum;
	}

	/** Default: 0 */
	public void setMinDelayBetweenNewPackets(int milliseconds) {
		minDelayBetweenNewPackets = milliseconds;
	}

	/** Default: 100000 */
	public void setMaxPacketLength(int maxLength) {
		maxPacketLength = maxLength;
	}

	/** Default: 200 */
	public void setMaxPacketsPerSecond(int packets) {
		maxPacketsPerSecond = packets;
	}
}
