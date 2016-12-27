package cs455.overlay.transport;

import java.util.HashMap;
import java.util.Map;

public class TCPConnectionsCache {
	private static TCPConnectionsCache tcpConnectionsCache = new TCPConnectionsCache();

	public Map serverToNode;

	private TCPConnectionsCache() {
		serverToNode = new HashMap();
	}

	public static TCPConnectionsCache getInstance() {
		return tcpConnectionsCache;
	}

}
