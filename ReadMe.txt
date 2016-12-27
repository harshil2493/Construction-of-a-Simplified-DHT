Name 	: Shah, Harshil
CSUID 	: 830387790
(For better looking of ReadMe, open it in gedit (Keep It Maximized))

Index
-----

1. 	Included Files
2. 	How To Run
3. 	Important Notes
4. 	Class Description


1. 	Included Files
--	--------------
(a)	All Source Java Files Included Inside Packages Shown Below ( Folder CS455
 )
		package cs455.overlay.node
		package cs455.overlay.routing
		package cs455.overlay.transport
		package cs455.overlay.util
		package cs455.overlay.wireformats
(b)	Makefile
(c)	ReadMe


2.	How To Run
--	----------
System will have exactly one instance of "Registry" & any number of instances of "MessagingNode". 
Violation in starting up either of the instance will show appropriate error message on respective console.

Firstly, perform "make" operation in current directory.
Secondly, in the Server PC, run Registry.class file by executing 'java cs455.overlay.node.Registry $any_port_number' 
(add desire port_number in commandLine Argument while starting up Registry)
and Lastly, we can make instance of MessagingNode by executing 'java cs455.overlay.node.MessagingNode $host_address $host_port_number' 
(add registry's host_address & host_port_number in commandLine Argument while starting up MessagingNode)

Maximum 128 instances of MessagingNode can be created. ( For more details kindly refer to Section 3 )

While closing consoles, please try to close registry first & then other messagingNodes.



3. 	Important Notes
--	---------------
(a)	Flow of Execution
	-----------------
	Registry instance will be created as described in Section 2 & waits for messaging_node to join the overlay by displaying "Registry Is Online Now..! MessagingNodes Can Send 		Requests!" message on console.
	Created Registry instance will also start listening to commandLine input to its console. 
	( Kindly maximize the console screen of Registry to check output perfectly)
	
	MessagingNode instance will be created as described earlier & sends registration_request to registry. 
	Registry will authenticate the message & provides unique ID to respective MessagingNode instance.
	( Kindly don't create too many instances of MessagingNode on one PC only. Problem may occur due to hardware dependency. At most total of 128 messaging-node can be created )
	
	After all messaging_nodes' registration, the logical overlay can be set up through commandLine. ( Refer Section 3.b - setup-overlay )
	If setup-overlay is done successfully, Registry console will display "Registry Is Now Ready To Initiate Tasks." message.
	(After setting up overlay, it cannot be again set up. Deletion & Addition of any messaging_node will now have no impact from here on )
	
	After that sending messages on overlay between nodes can be started by "start" command in registry ( Refer Section 3.b - start )
	If all nodes in overlay has started task of sending messages, Registry will show "All The Nodes Are Now Communicating.. Req For Traffic Details Will Be Sent In 30seconds 		(Default)".
	30seconds timer is default value. It can be changed at any time in Registry by "change-timer" command ( Refer Section 3.c - change-timer ) 
	
	After predefined timer, traffic_summery of all nodes will be displayed on Registry & also individual messaging_nodes' traffic details will get displayed in respective consoles.
	Counters associate with each messaging_nodes will be set to zero. But overlay will stay active & can be used again to send messages.
	

(b)	Given Commands
	--------------
	
	(i)	list-messsaging-nodes ( For Registry Only )
	---	-------------------------------------------
		All the connected messaging_nodes will be displayed with other personalized information ( i.e. ID, IP, Port )
	
	(ii)	setup-overlay ( For Registry Only )
	----	-----------------------------------
		If no argument is given, Registry will take default value ( i.e. 3 ). 
		Registry will generate routing tables according to given argument & send respective routing table to respective messaging_node.
	
	(iii)	list-routing-tables ( For Registry Only )
	-----	-----------------------------------------
		Routing tables of each connected messaging_nodes will be displayed.
	
	(iv)	start ( For Registry Only )
	----	---------------------------
		Argument to this command must be given. Argument of this command will result in number of messages that each messaging_node needs to be sending.
	
	(v)	exit-overlay ( For MessagingNode Only )
	---	---------------------------------------
		Execution of this command will de-register the node from the overlay.
	
	(vi)	print-counters-and-diagnostics ( For MessagingNode Only )
	----	---------------------------------------------------------
		It will print latest counter details (i.e. messageSent, messageRelayed, messageRecieved, payloadSent & payloadReceived )
	

(c) 	Other Important Commands ( Implemented By Me )
	----------------------------------------------

	(i)	change-timer (For Registry Only)
	---	--------------------------------
		If no argument is given, Registry will take default value ( i.e. 20seconds ). 
		Argument of this command will result in time to wait before Registry sends request for traffic_summery
		( Give argument in seconds only )
	
	(ii)	list-routing-paths (For MessagingNode Only)
	----	-------------------------------------------
		Details about where packet will be redirected from the current messaging_node if destination nodes are all other messaging_nodes.
		( Useful for debugging )
	

4. 	Class Description
--	-----------------
(a)	package cs455.overlay.node
	--------------------------
	interface Node 		:	It will be implemented by Registry & MessagingNode. 
					It contains onEvent(byte[] b) method which will be overridden by Registry & MessagingNode & will be called everytime when any node receives data
	
	class Registry		:	It implements Node interface & According to argument given, it will start listening to given port_number
					It maintains all important data like list_of_messaging_nodes, connectionCache, other important counters & also created routingTable.
					It also listens to console arguments & act accordingly.
					It will handle Registration & DeRegistration request of MesagingNodes & provide according response to them.
					RoutingTable will also be created according to consoleInput & list of appropriate routing entry will be sent to appropriate messaging_node.
					It also handles if any node fails to make connection to other messaging_nodes as given in its routing-tables_entry.
					According to commandLine input, Registry will initialize message sending in overlay.
					After message sending is done, Registry will display traffic summary & stay active for further tasks.
	
	class MessagingNode	:	Just like Registry, It implements Node interface & According to argument given, it will start connection with Registry
					It maintains all important data like connections to routingNodes, recievedMessages, relayedMessages, sentPayload, other important counters & also 						connection to Registry.
					It also listens to console arguments & act accordingly.
					It is also been made thread to prevent Distributed DeadLock.
					On creation of instance of it, instance will start ServerSocket on any available port, as it will play both roles - Server & Client - while sending 						Packets.
					& According to ServerSocket allocated to it, it will send registration request to Registry.
					messaging_node can exit-overlay any time but before Registry setup-overlay.
					On getting routing-tables from Registry, messaging_node makes connections to nodes mentioned in its routing-tables.
					Also, on receiving sending command from Registry, messaging_node generates random messages with random Integer Payloads to be sent to random 						locations within overlay.
					After completion of sending given number of messages, it will inform Registry regarding that & wait for Registry to send traffic_summery request.
					On receiving traffic_summery request, it will display all counters and send all counter details to Registry & reset all the associated counters.
					
(b)	package cs455.overlay.routing
---	-----------------------------
	class RoutingEntry	:	It is responsible to generate routing tables for messaging_nodes.
	
	class RoutingTable	:	It is responsible for sending routing-tables to all messaging_nodes.

(c)	package cs455.overlay.transport
---	-------------------------------
	class TCPConnection	:	It is useful to initialize TCPReceiverThread & TCPSender

	class TCPMessageNodeAsServer :	It is a thread, responsible to listen for communication coming from other messaging_node.

	class TCPReceiverThread	:	It is a thread, always running, to listen incoming messages

	class TCPSender		:	It is useful to send data on given socket.

(d)	package cs455.overlay.util
---	--------------------------
	class InteractiveCommandParser		:	It is thread, to handle console input to any node.

	class StatisticsCollectorAndDisplay	:	It will be used to calculate & display traffic_summery report.


(e)	package cs455.overlay.wireformats
---	---------------------------------
	interface Event		:	It is an interface having methods - getByte() & getType()
					getByte() will return marshelledBytes & getType() will return type of protocol.
	
	class EventFactory 	:	For the entire system, EventFactory will have only one instance. Instance of it will help Node to generate appropriate event.
	
	class Protocol  	: 	It contains declaration of all the needed protocols.
	
	below all classes will implement Event interface, override the methods & handles all the requests accordingly.
	class NodeReportsOverlaySetupStatus 	 
	class OverlayNodeReportsTaskFinished 	 
	class OverlayNodeReportsTrafficSummary 	 
	class OverlayNodeSendsData 	 
	class OverlayNodeSendsDeregistration 	 
	class OverlayNodeSendsRegistration 	 
		 
	class RegistryReportsDeregistrationStatus 	 
	class RegistryReportsRegistrationStatus 	 
	class RegistryRequestsTaskInitiate 	 
	class RegistryRequestsTrafficSummary 	 
	class RegistrySendsNodeManifest
	
