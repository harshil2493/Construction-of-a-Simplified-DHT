package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Node;

public class NodeReportsOverlaySetupStatus implements Event {
	MessagingNode node;

	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS;
	}

	@Override
	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(getType());

		dout.writeInt(node.nodeID);
		String message = "Node " + node.nodeID
				+ " Has Successfully SetUp The Connection";
		byte[] sendingMessage = message.getBytes();
		dout.write((byte) sendingMessage.length);
		dout.write(sendingMessage);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;
	}

	@Override
	public void setNode(Node node) {
		// TODO Auto-generated method stub
		this.node = (MessagingNode) node;
	}

	public byte[] getFailByte() throws Exception {
		// TODO Auto-generated method stub
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(getType());
		int fail = -1;
		dout.writeInt(fail);
		String message = "Node " + node.nodeID
				+ " Has Not Successfully SetUp The Connection";
		byte[] sendingMessage = message.getBytes();
		dout.write((byte) sendingMessage.length);
		dout.write(sendingMessage);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;
	}

}
