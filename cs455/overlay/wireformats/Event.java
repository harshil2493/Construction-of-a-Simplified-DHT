package cs455.overlay.wireformats;

import cs455.overlay.node.Node;

public interface Event {
	public byte getType();

	public byte[] getByte() throws Exception;

	public void setNode(Node node);
}
