package cs455.overlay.transport;

import java.io.DataInputStream;
import java.net.Socket;

import cs455.overlay.node.Node;

public class TCPReceiverThread implements Runnable {

	private Socket socket;
	private DataInputStream dataInputStream;
	private Node node;

	TCPReceiverThread(Node node, Socket socket) throws Exception {
		this.socket = socket;
		this.dataInputStream = new DataInputStream(socket.getInputStream());
		this.node = node;
	}

	public void run() {
		try {
			while (true) {
				byte[] rawBytes = this.receive();

			}
		} catch (Exception e) {
		}
	}

	byte[] receive() throws Exception {
		int dataLength;
		dataLength = dataInputStream.readInt();
		byte[] data = new byte[dataLength];
		dataInputStream.readFully(data, 0, dataLength);
		node.onEvent(data);
		return data;
	}

}
