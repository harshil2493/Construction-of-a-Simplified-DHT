package cs455.overlay.util;

import java.util.Map;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Node;
import cs455.overlay.node.Registry;

public class StatisticsCollectorAndDisplay {
	Node nodePrint;

	public void setNode(Node statsDisplay) {
		// TODO Auto-generated constructor stub
		nodePrint = statsDisplay;
	}

	private static StatisticsCollectorAndDisplay statisticsCollectorAndDisplay = new StatisticsCollectorAndDisplay();

	private StatisticsCollectorAndDisplay() {

	}

	public static StatisticsCollectorAndDisplay getInstance() {
		return statisticsCollectorAndDisplay;
	}

	public void display() {
		// TODO Auto-generated method stub
		MessagingNode node = (MessagingNode) nodePrint;
		System.out.println("Node ID: " + node.nodeID);
		System.out.println();
		System.out
				.println("MessagesSent\tMessagesRelayed\tMessagesRecieved\tSumOfPayloadSent\tSumOfPayloadRecieved");
		System.out.println(node.messageSent + "\t\t" + node.messageRelayed
				+ "\t\t" + node.messageRecieved + "\t\t\t" + node.payloadSent
				+ "\t\t" + node.payloadRecieved);

	}

	public void printStats() {
		// TODO Auto-generated method stub
		Registry registryNew = (Registry) nodePrint;
		int sentM = 0;
		int recM = 0;
		long sentP = 0;
		long recP = 0;
		int relM = 0;
		System.out
				.println("Node\tSendByNode\tPayloadSentByNode\tMessagesRelayed\tRecievedByNode\tPayloadRecievedByNode");
		for (Object key : registryNew.sentMessages.keySet()) {
			// System.out.println(key + " " + registryNew.sentMessages.get(key)
			// + " " + registryNew.recievedMessages.get(key));
			Map send = (Map) registryNew.sentMessages.get(key);
			Map rec = (Map) registryNew.recievedMessages.get(key);

			int mSent = 0;
			int mRec = 0;
			long pSent = 0;
			long pRec = 0;
			int mRel = 0;
			mRel = (int) registryNew.relayedMessages.get(key);
			relM = mRel + relM;
			for (Object sentMessages : send.keySet()) {
				mSent = (int) sentMessages;
				sentM = mSent + sentM;
				pSent = (long) send.get(sentMessages);
				sentP = sentP + pSent;
			}
			for (Object recievedMessages : rec.keySet()) {
				mRec = (int) recievedMessages;
				recM = mRec + recM;
				pRec = (long) rec.get(recievedMessages);
				recP = recP + pRec;
			}
			System.out.println(key + "\t" + mSent + "\t\t" + pSent + "\t\t"
					+ mRel + "\t\t" + mRec + "\t\t" + pRec);
		}
		System.out.println();
		System.out.println("Total :\t" + sentM + "\t\t" + sentP + "\t\t" + relM
				+ "\t\t" + recM + "\t\t" + recP);
	}

}
