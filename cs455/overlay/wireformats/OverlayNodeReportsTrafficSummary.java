package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Node;

public class OverlayNodeReportsTrafficSummary implements Event {
	MessagingNode node;

	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY;
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
		dout.writeInt(node.messageSent);
		dout.writeLong(node.payloadSent);
		dout.writeInt(node.messageRecieved);
		dout.writeLong(node.payloadRecieved);
		dout.writeInt(node.messageRelayed);
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

}
