package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import cs455.overlay.routing.*;
import cs455.overlay.transport.*;
import cs455.overlay.util.*;
import cs455.overlay.node.*;

public class OverlayNodeSendsRegistration implements Event {
	MessagingNode node;

	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return Protocol.OVERLAY_NODE_SENDS_REGISTRATION;
	}

	@Override
	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(getType());
		InetAddress localAddress = node.serverSocketForSending.getInetAddress()
				.getLocalHost();
		int localPortNumber = node.serverSocketForSending.getLocalPort();
		byte[] byteLocalIP = localAddress.getAddress();
		byte addressLength = (byte) byteLocalIP.length;
		dout.write(addressLength);
		dout.write(byteLocalIP);
		dout.writeInt(localPortNumber);
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
