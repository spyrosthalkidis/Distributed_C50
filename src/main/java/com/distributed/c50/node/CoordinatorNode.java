package com.distributed.c50.node;

import com.distributed.c50.arff.ARFFHandler;
import com.distributed.c50.common.Constants;
import com.distributed.c50.communication.InitiationMessage;
import com.distributed.c50.communication.Message;
import com.distributed.c50.communication.MessageHandler;
import com.distributed.c50.communication.SecureSocketManager;
import com.distributed.c50.core.DistributedC50Core;
import com.distributed.c50.model.AttributeMetadata;
import com.distributed.c50.model.TreeNode;

import weka.core.Instances;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of the Coordinator Node in the distributed C5.0 system.
 * Manages the overall distributed computation process and coordinates
 * communication between data parties.
 */
public class CoordinatorNode implements MessageHandler.MessageProcessor {
    private final String nodeId;
    private final int port;
    private final SecureSocketManager socketManager;
    private final MessageHandler.MessageHandlerImpl messageHandler;
    private final Map<String, String> dataPartyAddresses;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, Socket> clientSockets;
    private final DistributedC50Core c50Core;
    
    private ServerSocket serverSocket;
    private boolean running;
    private TreeNode decisionTree;
    
    /**
     * Creates a new coordinator node.
     * 
     * @param nodeId the unique identifier of this node
     * @param port the port to listen on
     * @throws NoSuchAlgorithmException if the encryption algorithm is not available
     */
    public CoordinatorNode(String nodeId, int port) throws NoSuchAlgorithmException {
        this.nodeId = nodeId;
        this.port = port;
        this.socketManager = new SecureSocketManager();
        this.messageHandler = new MessageHandler.MessageHandlerImpl(socketManager, this);
        this.dataPartyAddresses = new HashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.clientSockets = new ConcurrentHashMap<>();
        this.c50Core = new DistributedC50Core(nodeId, 0); // Will set actual party count later
    }
    
    /**
     * Starts the coordinator node.
     * 
     * @return true if started successfully, false otherwise
     */
    public boolean start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            
            // Start accepting client connections
            executorService.submit(this::acceptClients);
            
            System.out.println("Coordinator node started on port " + port);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to start coordinator node: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Accepts client connections.
     */
    private void acceptClients() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Handles a client connection.
     * 
     * @param clientSocket the client socket
     */
    private void handleClient(Socket clientSocket) {
        // TODO: Implement client handling
        // This would involve:
        // 1. Authenticating the client
        // 2. Registering the client as a data party
        // 3. Receiving and processing messages from the client
    }
    
    /**
     * Registers a data party.
     * 
     * @param partyId the unique identifier of the data party
     * @param host the hostname or IP address of the data party
     * @param port the port number of the data party
     */
    public void registerDataParty(String partyId, String host, int port) {
        dataPartyAddresses.put(partyId, host + ":" + port);
    }
    
    /**
     * Initiates the distributed C5.0 algorithm.
     * 
     * @param datasetName the name of the dataset
     * @param attributePartitioning the partitioning of attributes among parties
     * @param configuration additional configuration parameters
     * @return true if initiated successfully, false otherwise
     */
    public boolean initiateDistributedC50(String datasetName, String[] attributePartitioning,
                                         Map<String, String> configuration) {
        // Connect to all data parties
        for (Map.Entry<String, String> entry : dataPartyAddresses.entrySet()) {
            String partyId = entry.getKey();
            String[] hostPort = entry.getValue().split(":");
            String host = hostPort[0];
            int port = Integer.parseInt(hostPort[1]);
            
            if (!socketManager.connect(host, port, partyId)) {
                System.err.println("Failed to connect to data party: " + partyId);
                return false;
            }
        }
        
        // Send initiation message to all data parties
        String[] participatingNodes = dataPartyAddresses.keySet().toArray(new String[0]);
        
        for (String partyId : participatingNodes) {
            InitiationMessage initMessage = new InitiationMessage(
                nodeId, partyId, nodeId, participatingNodes, datasetName,
                attributePartitioning, configuration);
            
            if (!messageHandler.sendMessage(initMessage)) {
                System.err.println("Failed to send initiation message to data party: " + partyId);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Processes a received message.
     * 
     * @param message the received message
     * @return true if processed successfully, false otherwise
     */
    @Override
    public boolean processMessage(Message message) {
        // TODO: Implement message processing
        // This would involve:
        // 1. Identifying the message type
        // 2. Processing the message based on its type
        // 3. Sending appropriate responses
        
        return true;
    }
    
    /**
     * Stops the coordinator node.
     */
    public void stop() {
        running = false;
        
        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
        
        // Close all client sockets
        for (Socket socket : clientSockets.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
        
        // Close all connections
        socketManager.closeAllConnections();
        
        // Shutdown executor service
        executorService.shutdown();
        
        System.out.println("Coordinator node stopped");
    }
    
    /**
     * Gets the decision tree built by the distributed C5.0 algorithm.
     * 
     * @return the decision tree
     */
    public TreeNode getDecisionTree() {
        return decisionTree;
    }
    
    /**
     * Main method for testing the coordinator node.
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            // Create and start coordinator node
            CoordinatorNode coordinator = new CoordinatorNode("coordinator", Constants.DEFAULT_COORDINATOR_PORT);
            coordinator.start();
            
            // Register data parties
            coordinator.registerDataParty("party1", "localhost", 9001);
            coordinator.registerDataParty("party2", "localhost", 9002);
            
            // Initiate distributed C5.0
            String[] attributePartitioning = {"0,1,2:party1", "3,4,5:party2"};
            Map<String, String> configuration = new HashMap<>();
            configuration.put("maxDepth", "10");
            configuration.put("minInstances", "5");
            
            coordinator.initiateDistributedC50("sample", attributePartitioning, configuration);
            
            // Wait for completion (in a real application, this would be event-driven)
            Thread.sleep(10000);
            
            // Stop coordinator
            coordinator.stop();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
