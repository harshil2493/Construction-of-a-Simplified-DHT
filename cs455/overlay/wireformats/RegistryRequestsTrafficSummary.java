package cs455.overlay.wireformats;

import cs455.overlay.node.Node;

public class RegistryRequestsTrafficSummary implements Event {
	Node node;

	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY;
	}

	@Override
	public byte[] getByte() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNode(Node node) {
		// TODO Auto-generated method stub
		this.node = node;
	}
}
