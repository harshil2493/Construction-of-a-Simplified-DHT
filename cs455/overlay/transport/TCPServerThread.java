package cs455.overlay.transport;

import java.net.ServerSocket;
import java.net.Socket;

import cs455.overlay.node.Node;
import cs455.overlay.node.Registry;

public class TCPServerThread implements Runnable {
	int serverPortNumber;
	Node server;

	public TCPServerThread(String portNumber, Node requestingServer) {
		// TODO Auto-generated constructor stub
		serverPortNumber = Integer.parseInt(portNumber);
		server = requestingServer;

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			ServerSocket serverSocket = new ServerSocket(serverPortNumber);
			System.out
					.println("Registry Is Online Now..! MessagingNodes Can Send Requests!");
			while (true) {
				Socket socket = serverSocket.accept();

				TCPConnection tcpConnection = new TCPConnection(server, socket);

				Registry converted = (Registry) server;

				converted.tcpConnectionCache.serverToNode.put(socket,
						tcpConnection);

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Registry Might Be Already Live On Same Port");

		}
	}

}
