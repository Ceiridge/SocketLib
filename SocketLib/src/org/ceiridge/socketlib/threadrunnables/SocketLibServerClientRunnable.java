package org.ceiridge.socketlib.threadrunnables;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import org.ceiridge.socketlib.listener.SocketLibListenerServer;
import org.ceiridge.socketlib.main.SocketLib;
import org.ceiridge.socketlib.main.SocketLibServer;
import org.ceiridge.socketlib.packets.SocketLibPacket;
import org.ceiridge.socketlib.packets.SocketLibPacketMix;
import org.ceiridge.socketlib.util.TimeHelper;


public class SocketLibServerClientRunnable implements Runnable {

	public SocketLibServer sls;
	public Socket client;
	public int clientId;
	public long stayAliveTime;
	public long stayAlivePing;
	private boolean gotRSA = false;
	private TimeHelper lastPacketDelay = new TimeHelper();
	private TimeHelper secondDelay = new TimeHelper();
	private int receivedPackets = 0;

	public SocketLibServerClientRunnable(SocketLibServer sls, Socket client, int clientId) {
		this.sls = sls;
		this.client = client;
		this.clientId = clientId;
		this.stayAliveTime = System.currentTimeMillis();
		this.stayAlivePing = System.currentTimeMillis();

	}

	@Override
	public void run() {
		try {
			System.out.println("Client thread started");

			BufferedReader br = new BufferedReader(new InputStreamReader(this.client.getInputStream(), "UTF-8"));
			PrintWriter pw = new PrintWriter(this.client.getOutputStream(), false);
			boolean receivingPacket = false;
			ArrayList<String> receivingData = new ArrayList<String>();
			int packetId = -1;

			if (sls.rsa) {
				pw.println("rsa");
				pw.println(Base64.getEncoder().encodeToString(sls.publicKey.getEncoded()));
				pw.flush();
			} else {
				pw.println("norsa");
				pw.flush();
			}
			secondDelay.reset();
			while (true) {
				if (!client.isConnected() || client.isClosed() || sls.ss.isClosed()) {
					br.close();
					pw.close();
					onDisconnect();
					return;
				}
				if (gotRSA || !sls.rsa)
					for (SocketLibPacketMix pack : sls.queue) {
						if (pack.clientId != this.clientId)
							continue;
						try {
							pack.packet.onSend(pack.packet.data);

							for (SocketLibListenerServer slls : sls.listeners) {
								slls.onPacketSend(packetId, pack.packet);
							}

							int packetIndex = 0;
							for (SocketLibPacket pList : sls.packetList) {
								if (pList.getClass().getName().equals(pack.packet.getClass().getName()))
									break;
								packetIndex++;
							}

							if (packetIndex < 0) {
								System.out.println("Invalid packet index");
								continue;
							}

							pw.println("p " + packetIndex);

							for (String dataPackages : pack.packet.data.rawData) {
								if (sls.rsa) {
									pw.println(Base64.getEncoder()
											.encodeToString(SocketLib.encrypt(sls.otherPublicKey, dataPackages.getBytes("UTF-8"))));
								} else
									pw.println(Base64.getEncoder().encodeToString(dataPackages.getBytes("UTF-8")));
							}
							pw.println("e");

							pw.flush();
						} catch (Exception e) {
							e.printStackTrace();
						}
						sls.queue.remove(pack);
					}

				if ((stayAliveTime + 5000l) < System.currentTimeMillis()) {
					stayAliveTime = System.currentTimeMillis();
					pw.println("k");
					pw.flush();
				}
				if ((stayAlivePing + 20000l) < System.currentTimeMillis()) {
					pw.close();
					br.close();
					onDisconnect();
					return;
				}
				long strSize = 0l;
				for (String str : receivingData) {
					strSize += str.length();
				}
				if (strSize > 25000000) {
					System.out.println("Destroying " + client.getInetAddress().getHostAddress() + "'s thread because of too high memory usage");
					pw.close();
					br.close();
					onDisconnect();
					receivingData.clear();
					return;
				}

				String line;
				while (br.ready() && (line = br.readLine()) != null) {
					if (line.length() > sls.security.maxPacketLength)
						continue;
					if (line == null || line.equals("null")) {
						pw.close();
						br.close();
						onDisconnect();
						return;
						// System.out.println(line);
						// continue;
					}
					if (line.equals("k")) {
						stayAlivePing = System.currentTimeMillis();
						continue;
					}
					if (line.equals("rsa")) {
						String otherPKey = br.readLine();
						sls.otherPublicKey = Base64.getDecoder().decode(otherPKey.getBytes("UTF-8"));
						gotRSA = true;
						pw.println("rsac");
						pw.flush();
						continue;
					}

					if (line.startsWith("p ")) {
						try {
							if (lastPacketDelay.hasReached(sls.security.minDelayBetweenNewPackets)) {
								lastPacketDelay.reset();
								packetId = Integer.parseInt(line.split(" ")[1]);
								receivingPacket = true;
								receivingData.clear();
							}
							continue;
						} catch (Exception e) {
						}
					}

					if (line.length() == 1) {
						if (line.equals("e") && receivingPacket) {
							receivingPacket = false;
							try {
								if (secondDelay.hasReached(1000)) {
									secondDelay.reset();
									receivedPackets = 0;
								}
								receivedPackets++;
								if (receivedPackets > sls.security.maxPacketsPerSecond) {
									receivingData.clear();
									packetId = -1;
									continue;
								}
								final SocketLibPacket newPack = sls.packetList.get(packetId);
								newPack.data.rawData.clear();
								newPack.data.rawData.addAll(receivingData);
								newPack.onReceive(newPack.data);
								new Thread(new Runnable() {

									@Override
									public void run() {
										for (SocketLibListenerServer lis : sls.listeners) {
											lis.onPacketReceive(clientId, newPack);
										}
									}

								}, "PacketReceiveHandle Thread").start();

							} catch (Exception e) {
								e.printStackTrace();
							}
							receivingData.clear();
							packetId = -1;
							continue;
						}

					}

					try {
						if (receivingPacket) {
							line = new String(Base64.getDecoder().decode(line), "UTF-8");
							if (sls.rsa)
								receivingData.add(new String(SocketLib.decrypt(sls.privateKey.getEncoded(), line.getBytes()), "UTF-8"));
							else
								receivingData.add(line);
						}
					} catch (Exception e) {
					}
				}
				Thread.sleep(13l);
			}

		} catch (Throwable e) {
			e.printStackTrace();
			onDisconnect();
		}
	}

	public void onDisconnect() {
		System.err.println("One Lib Client Thread crashed!");
		String ip = client.getInetAddress().getHostAddress();
		sls.security.connectionsPerIP.put(ip, sls.security.connectionsPerIP.get(ip) - 1);
		for (SocketLibListenerServer slls : sls.listeners) {
			slls.onDisconnect(clientId, client);
		}
	}

}
