package org.ceiridge.socketlib.main;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import javax.crypto.Cipher;
import org.ceiridge.socketlib.packets.SocketLibPacket;

public abstract class SocketLib {
	public int port;
	public ArrayList<SocketLibPacket> packetList = new ArrayList<SocketLibPacket>();


	public SocketLib(int port) {
		this.port = port;

	}

	public void registerPacket(SocketLibPacket p) {
		packetList.add(p);
	}

	public static Key[] genKeys() {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			SecureRandom rnd = new SecureRandom();

			kpg.initialize(2048, rnd);
			KeyPair kP = kpg.generateKeyPair();
			return new Key[] {kP.getPublic(), kP.getPrivate()};
		} catch (Throwable ee) {
			return null;
		}
	}

	public static byte[] encrypt(final byte[] puKeyCode, byte[] plainText) {
		try {

			PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(puKeyCode));

			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(plainText);

		} catch (Throwable ee) {
		}
		return null;
	}

	public static byte[] decrypt(final byte[] prKeyCode, byte[] plainText) {
		try {

			PrivateKey publicKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(prKeyCode));

			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			return cipher.doFinal(plainText);

		} catch (Throwable ee) {
		}
		return null;
	}
}
