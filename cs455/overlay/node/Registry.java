package cs455.overlay.node;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPConnectionsCache;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.InteractiveCommandParser;
import cs455.overlay.util.StatisticsCollectorAndDisplay;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.RegistryReportsDeregistrationStatus;
import cs455.overlay.wireformats.RegistryReportsRegistrationStatus;
import cs455.overlay.wireformats.RegistryRequestsTaskInitiate;
import cs455.overlay.wireformats.RegistrySendsNodeManifest;

public class Registry implements Node {
	EventFactory eventFactory = EventFactory.getInstance();
	public TCPConnectionsCache tcpConnectionCache = TCPConnectionsCache
			.getInstance();
	public RoutingEntry routingEntry = RoutingEntry.getInstance();
	public Map listOfMessagingNodes = new HashMap();
	public Map listOfConnections = new HashMap();
	public Map listOfDeletedConnections = new HashMap();
	public Map routingTableCreated = new HashMap();
	public int sizeOfRoutingTable;
	public int numberOfMessages = 0;
	public Map sentMessages = new HashMap();
	public Map recievedMessages = new HashMap();
	public Map relayedMessages = new HashMap();
	public List<Integer> identifier = new ArrayList<Integer>();
	int liveConnections = 0;
	public int timeToSleep = 20000;
	@Override
	public void onEvent(byte[] receivedData) throws Exception {
		// TODO Auto-generated method stub
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(
				receivedData);
		DataInputStream din = new DataInputStream(new BufferedInputStream(
				baInputStream));
		byte[] type = new byte[1];
		din.readFully(type);
		switch (type[0]) {
		case Protocol.OVERLAY_NODE_SENDS_REGISTRATION:
			Event event = eventFactory
					.createEvent(Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS);
			event.setNode(this);
			RegistryReportsRegistrationStatus registrationStatus = (RegistryReportsRegistrationStatus) event;
			registrationStatus.setStream(din);

			registrationStatus.createConnection();
			break;

		case Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION:

			Event eventForDeReg = eventFactory
					.createEvent(Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS);
			eventForDeReg.setNode(this);
			RegistryReportsDeregistrationStatus deRegistrationStatus = (RegistryReportsDeregistrationStatus) eventForDeReg;
			deRegistrationStatus.setStream(din);

			deRegistrationStatus.createConnection();

			break;

		case Protocol.REGISTRY_SENDS_NODE_MANIFEST:

			Event sendTables = eventFactory
					.createEvent(Protocol.REGISTRY_SENDS_NODE_MANIFEST);
			sendTables.setNode(this);
			RegistrySendsNodeManifest registrySendsNodeManifest = (RegistrySendsNodeManifest) sendTables;

			registrySendsNodeManifest.sendTableToNodes();

			break;

		case Protocol.REGISTRY_REQUESTS_TASK_INITIATE:
			Event initiateTask = eventFactory
					.createEvent(Protocol.REGISTRY_REQUESTS_TASK_INITIATE);
			initiateTask.setNode(this);
			RegistryRequestsTaskInitiate registryRequestsTaskInitiate = (RegistryRequestsTaskInitiate) initiateTask;

			registryRequestsTaskInitiate.performTask(numberOfMessages);

			for (Object key : listOfConnections.keySet()) {
				TCPConnection connection = (TCPConnection) listOfConnections
						.get(key);

				connection.sender.sendData(registryRequestsTaskInitiate
						.getByte());

			}

			break;

		case Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS:

			increamentLiveConnections(din.readInt());

			break;

		case Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED:
			increamentLiveForTaskFinishConnections();

			break;

		case Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:

			reportStatusStore(din);
			increamentOverlayTrafficLiveConnections();

			break;

		}
	}

	private synchronized void increamentOverlayTrafficLiveConnections() {
		// TODO Auto-generated method stub
		liveConnections++;
		if (liveConnections == identifier.size()) {
			liveConnections = 0;

			StatisticsCollectorAndDisplay display = StatisticsCollectorAndDisplay
					.getInstance();
			display.setNode(this);
			display.printStats();
			resetData();
			System.out.println();
			System.out
					.println("Overlay Is Still Active.. You Can Initiate Sending Messages By \"start \" Argument Again..");

		}
	}

	private synchronized void increamentLiveForTaskFinishConnections()
			throws InterruptedException {
		// TODO Auto-generated method stub
		liveConnections++;
		if (liveConnections == identifier.size()) {
			liveConnections = 0;

			System.out.println();

			System.out
					.println("All The Nodes Are Now Communicating.. Req For Traffic Details Will Be Sent In "+(timeToSleep/1000)+"seconds (Default)");
			Thread.sleep(timeToSleep);
			System.out.println("Request For Traffic Details Is Sent!!!!");
			byte[] invoke = { Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY };
			for (Object key : listOfConnections.keySet()) {
				TCPConnection connection = (TCPConnection) listOfConnections
						.get(key);

				connection.sender.sendData(invoke);

			}
		}

	}

	private void resetData() {
		// TODO Auto-generated method stub
		sentMessages.clear();
		relayedMessages.clear();
		recievedMessages.clear();
	}

	private synchronized void increamentLiveConnections(int status) {
		// TODO Auto-generated method stub
		if (status == -1) {
			System.out.println("Overlay Is Not Fully Connected . ");
			System.out.println("If Possible, Kindly Restart Everything");
		} else {
			liveConnections++;

			if (liveConnections == identifier.size()) {
				liveConnections = 0;

				System.out.println();
				System.out
						.println("Overlay Connection Has Been Successfully Established.. ");
				System.out.println("Registry Is Now Ready To Initiate Tasks.");
			}
		}
	}

	private synchronized void reportStatusStore(DataInputStream din)
			throws Exception {
		// TODO Auto-generated method stub
		int ID = din.readInt();
		int sentM = din.readInt();
		long sentP = din.readLong();
		int recM = din.readInt();
		long recp = din.readLong();

		int relM = din.readInt();
		Map sentMap = new HashMap();
		sentMap.put(sentM, sentP);
		Map recMap = new HashMap();
		recMap.put(recM, recp);
		sentMessages.put(ID, sentMap);
		recievedMessages.put(ID, recMap);
		relayedMessages.put(ID, relM);

	}

	public Registry(String portNumber) {
		// TODO Auto-generated constructor stub
		TCPServerThread serverThread = new TCPServerThread(portNumber, this);
		Thread threadServerRunning = new Thread(serverThread);
		threadServerRunning.start();

		InteractiveCommandParser interactiveCommandParser = new InteractiveCommandParser(
				this);
		Thread threadConsoleIO = new Thread(interactiveCommandParser);
		threadConsoleIO.start();

	}

	public static void main(String[] args) {

		if (args.length == 1) {
			Registry registry = new Registry(args[0]);

		} else {
			System.out
					.println("Some Problem In Argument.. Give Appropriate PortNumber");
		}

	}

	@Override
	public ServerSocket returnServerSocket() {
		// TODO Auto-generated method stub
		return null;
	}

}
