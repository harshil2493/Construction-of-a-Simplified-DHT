package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;

import cs455.overlay.node.Node;
import cs455.overlay.node.Registry;
import cs455.overlay.transport.TCPConnection;

public class RegistryReportsDeregistrationStatus implements Event {

	Registry node;
	DataInputStream dataInputStream;
	int nodeID;
	int success = 0;

	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS;
	}

	@Override
	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(getType());

		dout.writeInt(nodeID);
		String message;
		if (success == 0)
			message = "DeRegistration request successful. The number of messaging nodes currently constituting the overlay is "
					+ "(" + node.identifier.size() + ")";
		else if (success == 1)
			message = "You Are Already Not Registered";
		else
			message = "DeRegistration Failed";
		byte[] messageInBytes = message.getBytes();
		dout.write((byte) messageInBytes.length);
		dout.write(messageInBytes);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;
	}

	@Override
	public void setNode(Node node) {
		// TODO Auto-generated method stub
		this.node = (Registry) node;
	}

	public void setStream(DataInputStream din) {
		// TODO Auto-generated method stub
		this.dataInputStream = din;

	}

	public void createConnection() throws Exception {
		// TODO Auto-generated method stub

		byte[] addressLength = new byte[1];
		dataInputStream.readFully(addressLength);

		int lengthInt = addressLength[0];
		byte[] byteIP = new byte[lengthInt];
		dataInputStream.readFully(byteIP);

		InetAddress IP = InetAddress.getByAddress(byteIP);
		int portReq = dataInputStream.readInt();

		nodeID = dataInputStream.readInt();

		if (node.identifier.contains(nodeID)) {
			TCPConnection connection = (TCPConnection) (node.listOfConnections
					.get((Object) nodeID));

			if (connection.s.getInetAddress().equals(IP)
					&& portReq == connection.s.getPort()) {
				node.identifier.remove((Object) nodeID);

				node.listOfMessagingNodes.remove((Object) nodeID);

				node.listOfConnections.remove((Object) nodeID);
				System.out.println(nodeID + " Node DeRegistered! ");
				success = 0;
				node.listOfDeletedConnections.put(nodeID, connection);
				connection.sender.sendData(getByte());
			} else {
				success = 2;
				System.out.println(" For NodeID " + nodeID
						+ " Fake DeRegistration Requestion Is Generated");
			}
		} else {
			success = 1;
			TCPConnection connection = (TCPConnection) (node.listOfDeletedConnections
					.get((Object) nodeID));
			connection.sender.sendData(getByte());
			System.out
					.println("Not Registered Node Sent DeRegistration Request For "
							+ nodeID);
		}
	}
}
