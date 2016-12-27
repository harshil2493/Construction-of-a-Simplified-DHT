package cs455.overlay.routing;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.Map;

import cs455.overlay.node.Registry;
import cs455.overlay.transport.TCPConnection;

public class RoutingTable implements Runnable {
	byte type;
	int size;
	Registry node;
	List<Integer> tableElements;
	int nodeID;

	public RoutingTable(byte type, int key, int size, Registry node,
			List<Integer> tableElements) {
		// TODO Auto-generated constructor stub
		this.type = type;
		this.size = size;
		this.node = node;
		this.tableElements = tableElements;
		this.nodeID = key;
	}

	public void run() {
		try {
			TCPConnection connection = (TCPConnection) node.listOfConnections
					.get(nodeID);
			connection.sender.sendData(sendingBytes());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private byte[] sendingBytes() throws Exception {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(type);
		dout.write((byte) size);

		for (int loop = 0; loop < size; loop++) {
			int element = tableElements.get(loop);
			dout.writeInt(element);

			Map IPAndPortNumber = (Map) node.listOfMessagingNodes
					.get((Object) element);
			String IP = IPAndPortNumber.keySet().toArray()[0].toString();

			byte[] IPInByte = IP.getBytes();
			dout.write((byte) IPInByte.length);

			dout.write(IPInByte);
			dout.writeInt((int) IPAndPortNumber.get(IP));

		}
		dout.write((byte) node.identifier.size());
		for (int i = 0; i < node.identifier.size(); i++)
			dout.writeInt(node.identifier.get(i));

		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}

}
