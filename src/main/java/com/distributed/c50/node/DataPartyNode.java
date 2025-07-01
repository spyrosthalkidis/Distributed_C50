package com.distributed.c50.node;

import com.distributed.c50.arff.ARFFHandler;
import com.distributed.c50.common.Constants;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of the Data Party Node in the distributed C5.0 system.
 * Holds a vertical partition of the data and participates in the secure
 * distributed computation process.
 */
public class DataPartyNode implements MessageHandler.MessageProcessor {
    private final String nodeId;
    private final int port;
    private final SecureSocketManager socketManager;
    private final MessageHandler.MessageHandlerImpl messageHandler;
    private final ExecutorService executorService;
    private final String coordinatorHost;
    private final int coordinatorPort;
    private final DistributedC50Core c50Core;
    
    private ServerSocket serverSocket;
    private boolean running;
    private Instances localData;
    private int[][] dataPartition;
    private AttributeMetadata[] attributeMetadata;
    
    /**
     * Creates a new data party node.
     * 
     * @param nodeId the unique identifier of this node
     * @param port the port to listen on
     * @param coordinatorHost the hostname or IP address of the coordinator
     * @param coordinatorPort the port number of the coordinator
     * @throws NoSuchAlgorithmException if the encryption algorithm is not available
     */
    public DataPartyNode(String nodeId, int port, String coordinatorHost, int coordinatorPort) 
            throws NoSuchAlgorithmException {
        this.nodeId = nodeId;
        this.port = port;
        this.socketManager = new SecureSocketManager();
        this.messageHandler = new MessageHandler.MessageHandlerImpl(socketManager, this);
        this.executorService = Executors.newCachedThreadPool();
        this.coordinatorHost = coordinatorHost;
        this.coordinatorPort = coordinatorPort;
        this.c50Core = new DistributedC50Core(nodeId, 0); // Will set actual party count later
    }
    
    /**
     * Starts the data party node.
     * 
     * @return true if started successfully, false otherwise
     */
    public boolean start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            
            // Start accepting connections
            executorService.submit(this::acceptConnections);
            
            // Connect to coordinator
            if (!socketManager.connect(coordinatorHost, coordinatorPort, "coordinator")) {
                System.err.println("Failed to connect to coordinator");
                stop();
                return false;
            }
            
            System.out.println("Data party node started on port " + port);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to start data party node: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Accepts incoming connections.
     */
    private void acceptConnections() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                executorService.submit(() -> handleConnection(socket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Handles an incoming connection.
     * 
     * @param socket the socket for the connection
     */
    private void handleConnection(Socket socket) {
        // TODO: Implement connection handling
        // This would involve:
        // 1. Authenticating the connection
        // 2. Receiving and processing messages
    }
    
    /**
     * Loads local data from an ARFF file.
     * 
     * @param arffFile the ARFF file to load
     * @return true if loaded successfully, false otherwise
     */
    public boolean loadLocalData(File arffFile) {
        try {
            localData = ARFFHandler.loadArffFile(arffFile);
            System.out.println("Loaded local data from " + arffFile.getAbsolutePath());
            System.out.println("Number of instances: " + localData.numInstances());
            System.out.println("Number of attributes: " + localData.numAttributes());
            return true;
        } catch (IOException e) {
            System.err.println("Failed to load local data: " + e.getMessage());
            return false;
        }
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
     * Stops the data party node.
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
        
        // Close all connections
        socketManager.closeAllConnections();
        
        // Shutdown executor service
        executorService.shutdown();
        
        System.out.println("Data party node stopped");
    }
    
    /**
     * Main method for testing the data party node.
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            // Parse command-line arguments
            if (args.length < 4) {
                System.out.println("Usage: DataPartyNode <nodeId> <port> <coordinatorHost> <coordinatorPort> [arffFile]");
                return;
            }
            
            String nodeId = args[0];
            int port = Integer.parseInt(args[1]);
            String coordinatorHost = args[2];
            int coordinatorPort = Integer.parseInt(args[3]);
            
            // Create and start data party node
            DataPartyNode dataParty = new DataPartyNode(nodeId, port, coordinatorHost, coordinatorPort);
            dataParty.start();
            
            // Load local data if specified
            if (args.length > 4) {
                File arffFile = new File(args[4]);
                dataParty.loadLocalData(arffFile);
            }
            
            // Wait for termination signal
            System.out.println("Press Enter to stop the data party node...");
            System.in.read();
            
            // Stop data party
            dataParty.stop();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
