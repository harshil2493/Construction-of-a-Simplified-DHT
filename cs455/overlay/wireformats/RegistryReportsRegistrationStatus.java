package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cs455.overlay.routing.*;
import cs455.overlay.transport.*;
import cs455.overlay.util.*;
import cs455.overlay.node.*;

public class RegistryReportsRegistrationStatus implements Event {
	Registry node;
	DataInputStream dataInputStream;
	int nodeID;
	int errorToPrint;

	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS;
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
		if (nodeID != -1) {
			message = "Registration request Successful. The number of messaging nodes currently constituting the overlay is ("
					+ node.identifier.size() + ")";
		} else {
			if (errorToPrint == 1) {
				message = "Registration request Unsuccessful! Overlay might have the same node registered already ";
				System.out.println("Registered Node ReRequesting");
			} else if (errorToPrint == 2) {
				message = "Registration request Unsuccessful! Overlay can have only 128 nodes";
				// System.out.println("Registered Node ReRequesting");
			} else {
				message = "Registration request Unsuccessful! Some other node is trying to initiate connection using your IPAddress";
				System.out
						.println("Fake Messaging Node Is Trying To Initiate Connection");
			}
		}
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

	public void setStream(DataInputStream dataInputStream) {
		this.dataInputStream = dataInputStream;
	}

	static String[] sufixes = new String[] { "th", "st", "nd", "rd", "th",
			"th", "th", "th", "th", "th" };

	public static String writingFormat(int nodeNumber) {

		switch (nodeNumber % 100) {
		case 11:
		case 12:
		case 13:
			return nodeNumber + "th";
		default:
			return nodeNumber + sufixes[nodeNumber % 10];

		}
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
		String IPString = IP.toString();
		Map IPAndPort = new HashMap();
		IPAndPort.put(IPString, portReq);
		boolean alreadyThere = false;
		for (Object key : node.listOfMessagingNodes.keySet()) {
			if (node.listOfMessagingNodes.get(key).equals(IPAndPort)) {
				alreadyThere = true;
			}
		}

		if (!alreadyThere) {
			boolean stop = true;
			Random rand = new Random();

			nodeID = rand.nextInt(128);
			while (stop) {

				if (!node.identifier.contains(nodeID)) {
					stop = false;
				} else {
					nodeID = rand.nextInt(128);
				}
			}
			Socket socketToNodeRegistered = new Socket(IP, portReq);
			TCPConnection tcpConnection = new TCPConnection(node,
					socketToNodeRegistered);
			if (node.identifier.size() >= 128) {
				System.out
						.println("Registry Has Reached The Limit Of Max 128 Nodes");
				errorToPrint = 2;
			} else {
				errorToPrint = 0;
				node.identifier.add(nodeID);
				node.listOfMessagingNodes.put(nodeID, IPAndPort);

				node.listOfConnections.put(nodeID, tcpConnection);
				System.out.println(writingFormat(node.identifier.size())
						+ " Node Registered! With NodeID : " + nodeID);
				try {
					tcpConnection.sender.sendData(getByte());
				} catch (Exception e) {
					System.out
							.println("Some Problem occurred While Sending Registration Status To Node: "
									+ nodeID);
					node.identifier.remove((Object) nodeID);

					node.listOfMessagingNodes.remove((Object) nodeID);

					node.listOfConnections.remove((Object) nodeID);
					System.out.println(nodeID + " Node DeRegistered! ");
				}
			}
		} else {
			nodeID = -1;
			errorToPrint = 1;

		}
	}
}
