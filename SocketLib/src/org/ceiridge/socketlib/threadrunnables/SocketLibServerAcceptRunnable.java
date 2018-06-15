package org.ceiridge.socketlib.threadrunnables;

import java.io.IOException;
import java.net.Socket;
import org.ceiridge.socketlib.listener.SocketLibListenerServer;
import org.ceiridge.socketlib.main.SocketLibIdMix;
import org.ceiridge.socketlib.main.SocketLibServer;

public class SocketLibServerAcceptRunnable implements Runnable {

	public SocketLibServer sls;

	public SocketLibServerAcceptRunnable(SocketLibServer sls) {
		this.sls = sls;
	}

	@Override
	public void run() {
		while (true) {

			try {
				if (sls.ss.isClosed())
					return;
				Socket client = sls.ss.accept();

				client.setKeepAlive(true);

				String ip = client.getInetAddress().getHostAddress();
				if (!sls.security.connectionsPerIP.containsKey(ip)) {
					sls.security.connectionsPerIP.put(ip, 1);
				} else {
					sls.security.connectionsPerIP.put(ip, sls.security.connectionsPerIP.get(ip) + 1);
				}

				if (sls.security.connectionsPerIP.get(ip) > sls.security.maxConsperIP) {
					System.out.println(
							ip + " is trying to connect for the " + sls.security.connectionsPerIP.get(ip) + " time but it has been prevented");
					sls.security.connectionsPerIP.put(ip, sls.security.connectionsPerIP.get(ip) - 1);
					continue;
				}
				int eid = sls.upperCLID++;
				Thread clientThread =
						new Thread(new SocketLibServerClientRunnable(sls, client, eid), "Client" + client.getInetAddress().getHostAddress());
				sls.clientThreads.put(clientThread, new SocketLibIdMix(client, eid));
				clientThread.start();
				System.out.println(ip + " connected (and created a thread for their ip) ID: " + eid);

				for (SocketLibListenerServer slls : sls.listeners) {
					slls.onJoin(eid, client);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
