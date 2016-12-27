package cs455.overlay.transport;

import java.net.ServerSocket;
import java.net.Socket;

import cs455.overlay.node.MessagingNode;

public class TCPMessageNodeAsServerThread implements Runnable {
	ServerSocket serverSocket;
	MessagingNode messagingNode;

	public TCPMessageNodeAsServerThread(ServerSocket serverSocket,
			MessagingNode messagingNode) {
		// TODO Auto-generated constructor stub
		this.messagingNode = messagingNode;
		this.serverSocket = serverSocket;

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while (true) {
				Socket socket = serverSocket.accept();

				TCPConnection tcpConnection = new TCPConnection(messagingNode,
						socket);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
