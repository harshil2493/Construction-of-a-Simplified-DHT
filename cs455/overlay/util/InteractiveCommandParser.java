package cs455.overlay.util;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Node;
import cs455.overlay.node.Registry;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Protocol;

public class InteractiveCommandParser implements Runnable {
	Node node;
	int counterPrintingError = 0;
	boolean doNotSetUp = false;

	public InteractiveCommandParser(Node nodeThreaded) {
		// TODO Auto-generated constructor stub
		node = nodeThreaded;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			Scanner reader = new Scanner(System.in);
			try {
				String command = reader.nextLine();

				if (command.contains("list-messaging-nodes")) {
					Registry registryPrintingConnections = (Registry) node;
					System.out.println();
					System.out.println("NodeID\tIPAddress\tPortNumber\tHostName");
					for (Object key : registryPrintingConnections.listOfMessagingNodes
							.keySet()) {
						Map IPAndPort = (Map) registryPrintingConnections.listOfMessagingNodes
								.get(key);

						for (Object keyN : IPAndPort.keySet()) {
							System.out.println(key + "\t"
									+ keyN.toString().replace("/", "") + "\t"
									+ IPAndPort.get(keyN) + "\t\t"+ InetAddress.getByName(keyN.toString().replace("/", "")).getHostName());
						}

					}
				} else if (command.contains("list-routing-paths")) {
					MessagingNode messagingNodeToListPath = (MessagingNode) node;
					System.out.println();
					System.out.println("Routing Path Nodes For : "
							+ messagingNodeToListPath.nodeID);
					System.out
							.println("For Node ID\tPacket Will Route To Node");
					for (int nodes : messagingNodeToListPath.listOfAllNodes) {
						if (nodes != messagingNodeToListPath.nodeID)
							System.out.println(nodes
									+ "\t\t"
									+ messagingNodeToListPath
											.generateDestination(nodes));
					}

				} else if (command.contains("setup-overlay")) {
					if (!doNotSetUp) {
						Registry registry = (Registry) node;

						String[] parsedCommand = command.split(" ");
						int sizeOfR = 3;
						if (parsedCommand.length == 2) {
							sizeOfR = Integer.parseInt(parsedCommand[1]);
						} else {
							System.out
									.println("As You Are Not Giving Argument .. Registry Will Take Default Value Of "
											+ sizeOfR);
						}
						int x = registry.identifier.size() - 1;
						int counter = 0;
						while (Math.pow(2, counter) <= x) {
							counter++;

						}

						if (sizeOfR >= 1 && sizeOfR <= counter) {
							try {
								registry.routingEntry.createTable(registry,
										sizeOfR);
								doNotSetUp = true;
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							System.out.println();
							System.out
									.println("Overlay Size Should Be Greater Than 0 & Lesser Or Equal To "
											+ counter);
							System.out.println("Try Again.. Sorry!");
						}

					} else {
						System.out
								.println("You Cannot SetUp-Overlay Now.. It Is Already Been SetUp!");
					}
				} else if (command.contains("list-routing-tables")) {
					Registry registryPrintingConnections = (Registry) node;

					for (Object key : registryPrintingConnections.routingTableCreated
							.keySet()) {
						System.out.println("NodeID: " + key);
						int count = 0;
						System.out
								.println("HopeAway\tNodeID\tNodeIP\t\tNodePortNumber");
						List<Integer> routingElements = (List<Integer>) registryPrintingConnections.routingTableCreated
								.get(key);
						for (int element : routingElements) {
							Map IPAndPort = (Map) registryPrintingConnections.listOfMessagingNodes
									.get(element);

							for (Object keyN : IPAndPort.keySet()) {
								System.out.println((int) Math.pow(2, count)
										+ "\t\t" + element + "\t"
										+ keyN.toString().replace("/", "")
										+ "\t" + IPAndPort.get(keyN));
							}
							count++;
						}
						System.out.println();

						System.out.println();

					}
				} else if (command.contains("list-traffic-details")) {
					Registry registryNew = (Registry) node;
					StatisticsCollectorAndDisplay display = StatisticsCollectorAndDisplay
							.getInstance();
					display.setNode(registryNew);
					display.printStats();
				} else if (command.contains("start")) {
					Registry registry = (Registry) node;

					String[] parsedCommand = command.split(" ");
					int messageNumbers = Integer.parseInt(parsedCommand[1]);

					try {

						registry.numberOfMessages = messageNumbers;
						byte[] type = { Protocol.REGISTRY_REQUESTS_TASK_INITIATE };
						registry.onEvent(type);

					} catch (Exception e) {

					}

				} else if (command.contains("print-counters-and-diagnostics")) {
					MessagingNode statsDisplay = (MessagingNode) node;
					StatisticsCollectorAndDisplay collectorAndDisplay = StatisticsCollectorAndDisplay
							.getInstance();
					collectorAndDisplay.setNode(statsDisplay);
					collectorAndDisplay.display();
				} else if (command.contains("exit-overlay")) {
					MessagingNode messagingNode = (MessagingNode) node;
					Event event = messagingNode.eventFactory
							.createEvent(Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION);
					event.setNode(messagingNode);
					try {
						messagingNode.tcpConnectionMessagingNode.sender
								.sendData(event.getByte());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} 
				else if (command.contains("change-timer")) {
						Registry timeChange = (Registry) node;
						String[] parsedCommand = command.split(" ");
						
						if (parsedCommand.length == 2) {
							timeChange.timeToSleep = Integer.parseInt(parsedCommand[1]) * 1000;
							System.out.println("Thread Sleeping Time Is Changed To "+(timeChange.timeToSleep/1000)+"Seconds");
						} else {
							System.out
									.println("As You Are Not Giving Argument .. Registry Will Take Default Value Of "+(timeChange.timeToSleep/1000)+"seconds Only"
											);
						}

				} 
				else {
					System.out.println("InValid Command !.... Sorry");
				}
			} catch (ClassCastException e) {

				System.out
						.println("Command Is Not Suitable For This Place .. Make Sure To Fire Command In Appropriate Registry OR MessagingNode");

			} catch (Exception e) {

				if (counterPrintingError == 0) {
					System.out
							.println("Argument Error OR Sudden Break Down Of Node..");

				}
			}
		}

	}

}
