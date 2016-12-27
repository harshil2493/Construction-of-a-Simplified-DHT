package cs455.overlay.node;

import java.net.ServerSocket;

public interface Node {
	public ServerSocket returnServerSocket();

	public void onEvent(byte[] receivedData) throws Exception;
}
