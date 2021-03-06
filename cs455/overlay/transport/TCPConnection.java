package cs455.overlay.transport;

import java.net.Socket;

import cs455.overlay.node.Node;

public class TCPConnection {
	public TCPReceiverThread receiver;
	public TCPSender sender;
	public Socket s;

	public TCPConnection(Node node, Socket socket) throws Exception {
		// TODO Auto-generated constructor stub
		receiver = new TCPReceiverThread(node, socket);
		sender = new TCPSender(socket);
		s = socket;
		Thread threadReceiver = new Thread(receiver);

		threadReceiver.start();

	}

}
