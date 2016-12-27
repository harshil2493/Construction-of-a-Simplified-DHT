package cs455.overlay.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs455.overlay.node.Registry;
import cs455.overlay.wireformats.Protocol;

public class RoutingEntry {
	private static RoutingEntry routingEntry = new RoutingEntry();
	public int routingTableSize;
	public Map routingTable = new HashMap();

	private RoutingEntry() {

	}

	public static RoutingEntry getInstance() {
		return routingEntry;
	}

	public void createTable(Registry registry, int tableSize) throws Exception {
		// TODO Auto-generated method stub
		routingTableSize = tableSize;
		registry.sizeOfRoutingTable = routingTableSize;

		List<Integer> list = registry.identifier;
		Collections.sort(list);

		int sizeOfElements = list.size();
		for (int i = 0; i < sizeOfElements; i++) {
			List<Integer> routingElements = new ArrayList<Integer>();
			int count = 1;
			for (int counter = 0; counter < tableSize; counter++) {
				if (i + count < sizeOfElements)
					routingElements.add(list.get(i + count));
				else
					routingElements.add(list.get(i + count - sizeOfElements));
				count = count * 2;
			}
			routingTable.put(list.get(i), routingElements);

		}
		registry.routingTableCreated = routingTable;

		byte[] sendingData = { Protocol.REGISTRY_SENDS_NODE_MANIFEST };
		registry.onEvent(sendingData);
	}
}
