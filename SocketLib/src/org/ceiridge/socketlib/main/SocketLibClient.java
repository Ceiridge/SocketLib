package org.ceiridge.socketlib.main;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.CopyOnWriteArrayList;
import org.ceiridge.socketlib.listener.SocketLibListener;
import org.ceiridge.socketlib.packets.SocketLibPacket;
import org.ceiridge.socketlib.threadrunnables.SocketLibClientRunnable;


public class SocketLibClient extends SocketLib {

	public InetSocketAddress connectionEnd;
	public SocketLibClientRunnable runnableInstance;
	public Thread clientThread;
	public Socket connection;
	public CopyOnWriteArrayList<SocketLibPacket> queue;
	public CopyOnWriteArrayList<SocketLibListener> listeners;
	public boolean rsa = false;
	public PublicKey publicKey;
	public PrivateKey privateKey;

	public byte[] otherPublicKey;

	public SocketLibClient(InetSocketAddress isa) throws IOException, ConnectException {
		super(isa.getPort());
		this.connectionEnd = isa;
		this.runnableInstance = new SocketLibClientRunnable(this);
		this.connection = new Socket(isa.getAddress(), isa.getPort());
		this.clientThread = new Thread(runnableInstance, "SocketLibClientThread");
		this.clientThread.start();
		queue = new CopyOnWriteArrayList<SocketLibPacket>();
		listeners = new CopyOnWriteArrayList<SocketLibListener>();
	}

	public void sendPacket(SocketLibPacket packet) {
		queue.add(packet);
	}


	public void disconnect() {
		try {
			this.connection.close();
		} catch (IOException e) {
		}
	}

	public void addListener(SocketLibListener listener) {
		listeners.add(listener);
	}

	public boolean isConnected() {
		try {
			return clientThread.isAlive();
		} catch (Throwable eeee) {
			return false;
		}
	}
}
