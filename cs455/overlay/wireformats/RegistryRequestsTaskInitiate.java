package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import cs455.overlay.node.Node;
import cs455.overlay.node.Registry;

public class RegistryRequestsTaskInitiate implements Event {
	Registry node;
	int numberOfMessages = 0;

	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return Protocol.REGISTRY_REQUESTS_TASK_INITIATE;
	}

	@Override
	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(getType());
		dout.writeInt(numberOfMessages);
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

	public void performTask(int number) throws Exception {
		// TODO Auto-generated method stub
		numberOfMessages = number;

	}
}
