package cs455.overlay.wireformats;

import java.util.List;
import java.util.Map;

import cs455.overlay.node.Node;
import cs455.overlay.node.Registry;
import cs455.overlay.routing.RoutingTable;

public class RegistrySendsNodeManifest implements Event {
	Registry node;

	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return Protocol.REGISTRY_SENDS_NODE_MANIFEST;
	}

	@Override
	public byte[] getByte() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNode(Node node) {
		// TODO Auto-generated method stub
		this.node = (Registry) node;
	}

	public void sendTableToNodes() {
		// TODO Auto-generated method stub
		int size = node.sizeOfRoutingTable;
		Map createdRoutingTables = node.routingTableCreated;
		for (Object key : createdRoutingTables.keySet()) {

			List<Integer> tableElements = (List<Integer>) createdRoutingTables
					.get(key);
			RoutingTable routingTable = new RoutingTable(getType(), (int) key,
					size, node, tableElements);
			Thread thread = new Thread(routingTable);
			thread.start();
		}

	}
}
