package cs455.overlay.wireformats;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Node;
import cs455.overlay.transport.TCPConnection;

public class OverlayNodeSendsData implements Event {
	MessagingNode node;
	byte[] sendingBytes;
	int destination;

	public OverlayNodeSendsData(MessagingNode messagingNodes,
			byte[] marshalledBytes, int generateDestination) {
		// TODO Auto-generated constructor stub
		node = messagingNodes;
		sendingBytes = marshalledBytes;
		destination = generateDestination;
	}

	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return Protocol.OVERLAY_NODE_SENDS_DATA;
	}

	@Override
	public byte[] getByte() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNode(Node node) {
		// TODO Auto-generated method stub
		this.node = (MessagingNode) node;
	}

	public void setData(byte[] marshalledBytes) {
		// TODO Auto-generated method stub
		sendingBytes = marshalledBytes;

	}

	public void routingPackets() {
		// TODO Auto-generated method stub

		TCPConnection connection = (TCPConnection) node.routingConnections
				.get(destination);
		connection.sender.sendData(sendingBytes);

	}

	public void setDestination(int generateDestination) {
		// TODO Auto-generated method stub
		destination = generateDestination;

	}

}
