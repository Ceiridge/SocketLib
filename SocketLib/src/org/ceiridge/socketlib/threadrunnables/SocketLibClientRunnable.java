package org.ceiridge.socketlib.threadrunnables;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import org.ceiridge.socketlib.listener.SocketLibListener;
import org.ceiridge.socketlib.main.SocketLib;
import org.ceiridge.socketlib.main.SocketLibClient;
import org.ceiridge.socketlib.packets.SocketLibPacket;


public class SocketLibClientRunnable implements Runnable {

	SocketLibClient slc;
	public long stayAlivePing;
	private boolean gotRSA = false;

	public SocketLibClientRunnable(SocketLibClient slct) {
		this.slc = slct;
		this.stayAlivePing = System.currentTimeMillis();
	}

	@Override
	public void run() {
		try {
			while (!slc.connection.isConnected()) {
				Thread.sleep(500l);
			}
			System.out.println("Connected with server");

			BufferedReader br = new BufferedReader(new InputStreamReader(slc.connection.getInputStream(), "UTF-8"));
			PrintWriter pw = new PrintWriter(slc.connection.getOutputStream(), false);

			slc.connection.setKeepAlive(true);

			boolean receivingPacket = false;
			ArrayList<String> receivingData = new ArrayList<String>();
			int packetId = -1;

			while (true) {
				if (!slc.connection.isConnected() || slc.connection.isClosed()) {
					br.close();
					pw.close();
					System.err.println("Complete Lib Client Thread crashed!");
					return;
				}
				if (gotRSA)
					for (SocketLibPacket pack : slc.queue) {
						pack.onSend(pack.data);

						for (SocketLibListener slls : slc.listeners) {
							slls.onPacketSend(pack);
						}

						int packetIndex = 0;
						for (SocketLibPacket pList : slc.packetList) {
							if (pList.getClass().getName().equals(pack.getClass().getName()))
								break;
							packetIndex++;
						}

						if (packetIndex < 0) {
							System.out.println("Invalid packet index");
							continue;
						}

						pw.println("p " + packetIndex);

						for (String dataPackages : pack.data.rawData) {
							if (slc.rsa) {
								pw.println(Base64.getEncoder().encodeToString(SocketLib.encrypt(slc.otherPublicKey, dataPackages.getBytes("UTF-8"))));
							} else
								pw.println(Base64.getEncoder().encodeToString(dataPackages.getBytes("UTF-8")));
						}
						pw.println("e");

						pw.flush();
						slc.queue.remove(pack);
					}
				if ((stayAlivePing + 20000l) < System.currentTimeMillis()) {
					pw.close();
					br.close();
					System.err.println("Complete Lib Client Thread crashed because of timeout!");
					return;
				}
				String line;
				while (br.ready() && (line = br.readLine()) != null) {

					if (line.equals("null")) {
						slc.connection.close();
						pw.close();
						br.close();
						return;
					}

					if (line.equals("k")) {
						pw.println("k");
						pw.flush();
						stayAlivePing = System.currentTimeMillis();
						continue;
					}

					if (line.equals("rsa")) {
						slc.rsa = true;
						String otherPKey = br.readLine();
						slc.otherPublicKey = Base64.getDecoder().decode(otherPKey.getBytes("UTF-8"));
						Key[] myKeys = SocketLib.genKeys();
						slc.publicKey = (PublicKey) myKeys[0];
						slc.privateKey = (PrivateKey) myKeys[1];

						pw.println("rsa");
						pw.println(Base64.getEncoder().encodeToString(slc.publicKey.getEncoded()));
						pw.flush();
						continue;
					}

					if (line.equals("norsa")) {
						gotRSA = true;
						continue;
					}

					if (line.equals("rsac")) {
						gotRSA = true;
						continue;
					}

					if (line.startsWith("p ")) {
						try {
							packetId = Integer.parseInt(line.split(" ")[1]);
							receivingPacket = true;
							receivingData.clear();
							continue;
						} catch (Exception e) {
						}
					}

					if (line.length() == 1) {
						if (line.equals("e") && receivingPacket) {
							receivingPacket = false;
							try {
								SocketLibPacket newPack = slc.packetList.get(packetId);
								newPack.data.rawData.clear();
								newPack.data.rawData.addAll(receivingData);

								newPack.onReceive(newPack.data);
								for (SocketLibListener lis : slc.listeners) {
									lis.onPacketReceive(newPack);
								}
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
							if (slc.rsa)
								receivingData.add(new String(SocketLib.decrypt(slc.privateKey.getEncoded(), line.getBytes()), "UTF-8"));
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
			System.err.println("Complete Lib Client Thread crashed!");
		}
	}

}
