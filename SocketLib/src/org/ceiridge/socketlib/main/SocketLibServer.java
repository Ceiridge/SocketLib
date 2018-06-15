package org.ceiridge.socketlib.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.ceiridge.socketlib.listener.SocketLibListenerServer;
import org.ceiridge.socketlib.packets.SocketLibPacket;
import org.ceiridge.socketlib.packets.SocketLibPacketMix;
import org.ceiridge.socketlib.threadrunnables.SocketLibServerAcceptRunnable;
import org.ceiridge.socketlib.util.SecurityUtils;

public class SocketLibServer extends SocketLib {

	public ServerSocket ss;
	public Thread acceptThread;

	public HashMap<Thread, SocketLibIdMix> clientThreads = new HashMap<Thread, SocketLibIdMix>();
	public CopyOnWriteArrayList<SocketLibPacketMix> queue;
	public CopyOnWriteArrayList<SocketLibListenerServer> listeners;
	public boolean rsa;



	public PublicKey publicKey;
	public PrivateKey privateKey;

	public byte[] otherPublicKey;

	public int upperCLID = 0;

	public SecurityUtils security;

	public SocketLibServer(int port, boolean rsa) throws IOException {
		super(port);
		this.rsa = rsa;
		security = new SecurityUtils();
		queue = new CopyOnWriteArrayList<SocketLibPacketMix>();
		listeners = new CopyOnWriteArrayList<SocketLibListenerServer>();
		ss = new ServerSocket(port);
		if (rsa) {
			Key[] keys = SocketLib.genKeys();
			publicKey = (PublicKey) keys[0];
			privateKey = (PrivateKey) keys[1];
		}
		acceptThread = new Thread(new SocketLibServerAcceptRunnable(this), "SocketLibServerAcceptThread");
		acceptThread.start();

	}


	public void sendPacket(int clientId, SocketLibPacket packet) {
		queue.add(new SocketLibPacketMix(clientId, packet));
	}

	public void addListener(SocketLibListenerServer listener) {
		listeners.add(listener);
	}

	public void disconnectClient(int clientId) {
		try {
			for (SocketLibIdMix slid : clientThreads.values()) {
				if (slid.id == clientId) {
					slid.socket.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getIPById(int clientId) {
		try {
			for (SocketLibIdMix slid : clientThreads.values()) {
				if (slid.id == clientId)
					return slid.socket.getInetAddress().getHostAddress();
			}
			return "0.0.0.0";
		} catch (Exception e) {
			e.printStackTrace();
			return "0.0.0.0";
		}
	}

	public Socket getSocketById(int clientId) {
		try {
			for (SocketLibIdMix slid : clientThreads.values()) {
				if (slid.id == clientId)
					return slid.socket;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean isConnected(int clientId) {
		try {
			for (SocketLibIdMix slid : clientThreads.values()) {
				if (slid.id == clientId) {
					for (Thread tt : clientThreads.keySet()) {
						SocketLibIdMix slide = clientThreads.get(tt);
						if (slid.hashCode() == slide.hashCode()) {
							return tt.isAlive();
						}
					}
					break;
				}
			}
			return false;
		} catch (Throwable eeee) {
			return false;
		}
	}
}
