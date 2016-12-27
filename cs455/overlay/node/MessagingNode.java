package cs455.overlay.node;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPMessageNodeAsServerThread;
import cs455.overlay.util.InteractiveCommandParser;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.NodeReportsOverlaySetupStatus;
import cs455.overlay.wireformats.OverlayNodeSendsData;
import cs455.overlay.wireformats.Protocol;

public class MessagingNode extends Thread implements Node {
	public EventFactory eventFactory = EventFactory.getInstance();
	public int messageRecieved = 0;
	public int messageSent = 0;
	public int messageRelayed = 0;
	public long payloadSent = 0;
	public long payloadRecieved = 0;
	public ServerSocket serverSocketForSending = new ServerSocket(0);
	public TCPConnection tcpConnectionMessagingNode;
	public int nodeID = -1;
	private Queue<OverlayNodeSendsData> relayQueue = new LinkedList<OverlayNodeSendsData>();
	public Map routingConnections = new HashMap();
	int[] generatedSendingMessages;
	int[] destinationOfGeneratedMessages;
	List<Integer> connectedNodesSorted = new ArrayList<Integer>();
	List<Integer> connectedNodes = new ArrayList<Integer>();
	List<Integer> backConnectedNodes = new ArrayList<Integer>();
	public List<Integer> listOfAllNodes = new ArrayList<Integer>();

	public MessagingNode(String host, String portNumber) throws Exception {
		// TODO Auto-generated constructor stub
		try {
			Socket socket = new Socket(InetAddress.getByName(host),
					Integer.parseInt(portNumber));

			TCPMessageNodeAsServerThread messagingNodeAsServerThread = new TCPMessageNodeAsServerThread(
					serverSocketForSending, this);
			Thread threadServerRunning = new Thread(messagingNodeAsServerThread);
			threadServerRunning.start();

			tcpConnectionMessagingNode = new TCPConnection(this, socket);

			Thread sendingMessageToNodeThread = new Thread(this);
			sendingMessageToNodeThread.start();
			InteractiveCommandParser interactiveCommandParser = new InteractiveCommandParser(
					this);
			Thread threadClientConsoleIO = new Thread(interactiveCommandParser);
			threadClientConsoleIO.start();

			Event event = eventFactory
					.createEvent(Protocol.OVERLAY_NODE_SENDS_REGISTRATION);
			event.setNode(this);
			tcpConnectionMessagingNode.sender.sendData(event.getByte());
		} catch (Exception e) {
			System.out
					.println("Desire Registry Is Not Online .. Make Sure Registry Is Live");
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		while (true) {
			OverlayNodeSendsData relayMsg;
			synchronized (relayQueue) {
				relayMsg = relayQueue.poll();
			}

			if (relayMsg != null) {
				try {
					relayMsg.routingPackets();

				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}
	}

	@Override
	public void onEvent(byte[] receivedData) throws Exception {
		// TODO Auto-generated method stub
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(
				receivedData);
		DataInputStream dataInputStream = new DataInputStream(
				new BufferedInputStream(baInputStream));
		byte[] type = new byte[1];
		dataInputStream.readFully(type);
		switch (type[0]) {
		case Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS:

			nodeID = dataInputStream.readInt();
			System.out.println("Node Registration Successful.. Node ID is: "
					+ nodeID);
			int lengthInComing = dataInputStream.readByte();
			byte[] messageToNode = new byte[lengthInComing];
			dataInputStream.readFully(messageToNode);
			System.out.println(new String(messageToNode));
			break;

		case Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS:

			System.out.println();
			System.out.println("Node DeRegistration Successful.. Node ID is: "
					+ dataInputStream.readInt());
			int lengthInComingDer = dataInputStream.readByte();
			byte[] deRegMessage = new byte[lengthInComingDer];
			dataInputStream.readFully(deRegMessage);
			System.out.println(new String(deRegMessage));
			break;

		case Protocol.REGISTRY_SENDS_NODE_MANIFEST:
			try {
				storeRoutingTables(dataInputStream);

				Event event = eventFactory
						.createEvent(Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS);
				event.setNode(this);
				NodeReportsOverlaySetupStatus nodeReportsOverlaySetupStatus = (NodeReportsOverlaySetupStatus) event;

				connectedNodes.addAll(routingConnections.keySet());

				Collections.sort(connectedNodes);

				List<Integer> tempList = new ArrayList<Integer>();
				for (int i : connectedNodes) {
					if (i > nodeID) {
						connectedNodesSorted.add(i);
					} else {
						tempList.add(i);
					}
				}
				connectedNodesSorted.addAll(tempList);

				backConnectedNodes.addAll(connectedNodesSorted);
				Collections.reverse(backConnectedNodes);

				tcpConnectionMessagingNode.sender.sendData(event.getByte());
				System.out.println();
				System.out
						.println("Messaging Node Has Established Connection To Other Nodes Successfully.");
				System.out
						.println("You Can View Routing Path To Each Node By Firing \"list-routing-paths\" Here..");
			} catch (Exception e) {
				Event eventFailed = eventFactory
						.createEvent(Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS);
				eventFailed.setNode(this);
				NodeReportsOverlaySetupStatus nodeReportsOverlaySetupStatusFailure = (NodeReportsOverlaySetupStatus) eventFailed;
				System.out.println();
				System.out
						.println("Messaging Node Has Failed Established Connection To Other Nodes Successfully.");
				tcpConnectionMessagingNode.sender
						.sendData(nodeReportsOverlaySetupStatusFailure
								.getFailByte());
			}
			break;

		case Protocol.REGISTRY_REQUESTS_TASK_INITIATE:

			int numberOfMessages = dataInputStream.readInt();
			messageSent = numberOfMessages;
			generatePackets(numberOfMessages);

			int x = 0;
			for (int i = 0; i < generatedSendingMessages.length; i++) {
				byte[] marshalledBytes = null;
				ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
				DataOutputStream dout = new DataOutputStream(
						new BufferedOutputStream(baOutputStream));
				dout.write(Protocol.OVERLAY_NODE_SENDS_DATA);

				dout.writeInt(destinationOfGeneratedMessages[i]);
				dout.writeInt(nodeID);
				dout.writeInt(generatedSendingMessages[i]);

				dout.writeInt(x);
				dout.flush();
				marshalledBytes = baOutputStream.toByteArray();
				baOutputStream.close();
				dout.close();

				OverlayNodeSendsData nodeSendsData = new OverlayNodeSendsData(
						this, marshalledBytes,
						generateDestination(destinationOfGeneratedMessages[i]));
				synchronized (relayQueue) {
					relayQueue.add(nodeSendsData);
				}
			}
			System.out.println("Node Has Started Sending Messages .. ");
			Event informRegAboutTaskFinished = eventFactory
					.createEvent(Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED);
			informRegAboutTaskFinished.setNode(this);
			tcpConnectionMessagingNode.sender
					.sendData(informRegAboutTaskFinished.getByte());

			break;

		case Protocol.OVERLAY_NODE_SENDS_DATA:
			int dest = dataInputStream.readInt();

			if (dest == nodeID) {
				dataInputStream.readInt();
				increaseRecievedPayload(dataInputStream.readInt());
				increaseRecievedMessages();

			} else {
				increaseRelayedMessages();

				OverlayNodeSendsData nodeSendsDataRelayed = new OverlayNodeSendsData(
						this, receivedData, generateDestination(dest));
				synchronized (relayQueue) {
					relayQueue.add(nodeSendsDataRelayed);
				}

			}
			break;

		case Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
			Event trafficSummary = eventFactory
					.createEvent(Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY);
			trafficSummary.setNode(this);

			displayTrafficDetails();

			System.out.println();
			System.out
					.println("Traffic Details Sent To Registry & All Associated Counters Were Set To Zero!!");
			tcpConnectionMessagingNode.sender
					.sendData(trafficSummary.getByte());

			resetEverything();
			break;
		}
		baInputStream.close();
		dataInputStream.close();
	}

	private byte[] generateReceivedData(int dest,
			DataInputStream dataInputStream) throws Exception {
		// TODO Auto-generated method stub
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.OVERLAY_NODE_SENDS_DATA);
		dout.writeInt(dest);
		dout.writeInt(dataInputStream.readInt());
		dout.writeInt(dataInputStream.readInt());
		int size = dataInputStream.readInt() + 1;

		dout.writeInt(size);
		for (int loop = 1; loop < size; loop++)
			dout.writeInt(dataInputStream.readInt());
		dout.writeInt(nodeID);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	private void resetEverything() {
		// TODO Auto-generated method stub
		messageRecieved = 0;
		messageSent = 0;
		messageRelayed = 0;
		payloadSent = 0;
		payloadRecieved = 0;

	}

	private void displayTrafficDetails() {
		// TODO Auto-generated method stub

		System.out.println();
		System.out.println("Node ID: " + nodeID);

		System.out
				.println("Sent\tRelayed\tRecieved\tSumOfPayloadSent\tSumOfPayloadRecieved");
		System.out.println(messageSent + "\t" + messageRelayed + "\t"
				+ messageRecieved + "\t\t" + payloadSent + "\t\t"
				+ payloadRecieved);

	}

	private synchronized void increaseRecievedPayload(int readInt) {
		// TODO Auto-generated method stub
		payloadRecieved = payloadRecieved + readInt;

	}

	private synchronized void increaseRelayedMessages() {
		// TODO Auto-generated method stub
		messageRelayed++;
	}

	private synchronized void increaseRecievedMessages() {
		// TODO Auto-generated method stub
		messageRecieved++;

	}

	public int generateDestination(int destinationNode) {
		// TODO Auto-generated method stub
		if (connectedNodes.contains(destinationNode)) {
			return destinationNode;
		} else {
			int bestLoc = 0;
			if (destinationNode > nodeID) {
				for (int loc : connectedNodes) {
					if (loc < destinationNode) {
						bestLoc = loc;
					}

					else {
						break;
					}
				}

			} else {
				int given = 0;
				for (int loc : backConnectedNodes) {
					if (loc > destinationNode) {

					}

					else {
						bestLoc = loc;
						given = 1;
						break;
					}
				}
				if (given == 0)
					bestLoc = Collections.max(backConnectedNodes);
			}

			return bestLoc;
		}
	}

	public void generatePackets(int sizeMessages) {

		// TODO Auto-generated method stub

		Random r = new Random();
		int counter = 0;
		int maxCounterSize = listOfAllNodes.size();
		generatedSendingMessages = new int[sizeMessages];
		destinationOfGeneratedMessages = new int[sizeMessages];
		for (int i = 0; i < sizeMessages; i++) {

			counter = r.nextInt(listOfAllNodes.size());
			while (listOfAllNodes.get(counter) == nodeID) {

				counter = r.nextInt(listOfAllNodes.size());
			}
			int number = r.nextInt();
			payloadSent = number + payloadSent;

			destinationOfGeneratedMessages[i] = listOfAllNodes.get(counter);
			generatedSendingMessages[i] = number;

		}

	}

	private void storeRoutingTables(DataInputStream dataInputStream)
			throws Exception {
		// TODO Auto-generated method stub
		byte[] sizeTable = new byte[1];
		dataInputStream.readFully(sizeTable);
		int sizeInt = sizeTable[0];

		for (int looping = 0; looping < sizeInt; looping++) {

			int connectionNodeID = dataInputStream.readInt();
			byte[] IPLength = new byte[1];
			dataInputStream.readFully(IPLength);

			byte[] IPToStore = new byte[IPLength[0]];
			dataInputStream.readFully(IPToStore);
			String IPInString = new String(IPToStore);
			IPInString = IPInString.substring(1);
			Socket newConnection = new Socket(
					InetAddress.getByName(IPInString),
					dataInputStream.readInt());
			TCPConnection connection = new TCPConnection(this, newConnection);
			routingConnections.put(connectionNodeID, connection);

		}
		int totalNodes = dataInputStream.readByte();
		for (int storeValue = 0; storeValue < totalNodes; storeValue++) {
			listOfAllNodes.add(dataInputStream.readInt());
		}

	}

	public static void main(String[] args) throws Exception {

		if (args.length == 2) {
			MessagingNode messagingNode = new MessagingNode(args[0], args[1]);

		} else {
			System.out
					.println("Some Problem In Argument.. Give Appropriate HostID & HostPortNumber");
		}

	}

	@Override
	public ServerSocket returnServerSocket() {
		// TODO Auto-generated method stub
		return serverSocketForSending;
	}

}
