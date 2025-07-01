package com.distributed.c50.communication;

import com.distributed.c50.common.Constants;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Manages secure socket connections between nodes in the distributed C5.0 system.
 */
public class SecureSocketManager {
    private final Map<String, Socket> connections;
    private final Map<String, ObjectOutputStream> outputStreams;
    private final Map<String, ObjectInputStream> inputStreams;
    private final Map<String, SecretKey> encryptionKeys;
    
    /**
     * Creates a new secure socket manager.
     * 
     * @throws NoSuchAlgorithmException if the encryption algorithm is not available
     */
    public SecureSocketManager() throws NoSuchAlgorithmException {
        this.connections = new HashMap<>();
        this.outputStreams = new HashMap<>();
        this.inputStreams = new HashMap<>();
        this.encryptionKeys = new HashMap<>();
    }
    
    /**
     * Connects to a remote node.
     * 
     * @param host the hostname or IP address of the remote node
     * @param port the port number of the remote node
     * @param nodeId the ID of the remote node
     * @return true if connected successfully, false otherwise
     */
    public boolean connect(String host, int port, String nodeId) {
        try {
            // Try to connect with retries
            Socket socket = null;
            int retries = 0;
            
            while (retries < Constants.MAX_CONNECTION_RETRIES) {
                try {
                    socket = new Socket(host, port);
                    socket.setSoTimeout(Constants.SOCKET_TIMEOUT);
                    break;
                } catch (IOException e) {
                    retries++;
                    if (retries >= Constants.MAX_CONNECTION_RETRIES) {
                        throw e;
                    }
                    
                    // Wait before retrying
                    try {
                        Thread.sleep(Constants.CONNECTION_RETRY_DELAY);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            
            // Create streams
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            
            // Generate encryption key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey key = keyGen.generateKey();
            
            // Store connection information
            connections.put(nodeId, socket);
            outputStreams.put(nodeId, out);
            inputStreams.put(nodeId, in);
            encryptionKeys.put(nodeId, key);
            
            return true;
        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println("Failed to connect to " + nodeId + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Sends a message to a remote node.
     * 
     * @param nodeId the ID of the remote node
     * @param message the message to send
     * @return true if sent successfully, false otherwise
     */
    public boolean sendMessage(String nodeId, Message message) {
        try {
            ObjectOutputStream out = outputStreams.get(nodeId);
            if (out == null) {
                return false;
            }
            
            // In a real implementation, we would encrypt the message here
            // For simplicity, we just send the message directly
            out.writeObject(message);
            out.flush();
            
            return true;
        } catch (IOException e) {
            System.err.println("Failed to send message to " + nodeId + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Receives a message from a remote node.
     * 
     * @param nodeId the ID of the remote node
     * @return the received message, or null if an error occurred
     */
    public Message receiveMessage(String nodeId) {
        try {
            ObjectInputStream in = inputStreams.get(nodeId);
            if (in == null) {
                return null;
            }
            
            // In a real implementation, we would decrypt the message here
            // For simplicity, we just receive the message directly
            return (Message) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to receive message from " + nodeId + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Closes a connection to a remote node.
     * 
     * @param nodeId the ID of the remote node
     */
    public void closeConnection(String nodeId) {
        try {
            Socket socket = connections.remove(nodeId);
            if (socket != null) {
                socket.close();
            }
            
            ObjectOutputStream out = outputStreams.remove(nodeId);
            if (out != null) {
                out.close();
            }
            
            ObjectInputStream in = inputStreams.remove(nodeId);
            if (in != null) {
                in.close();
            }
            
            encryptionKeys.remove(nodeId);
        } catch (IOException e) {
            System.err.println("Error closing connection to " + nodeId + ": " + e.getMessage());
        }
    }
    
    /**
     * Closes all connections.
     */
    public void closeAllConnections() {
        for (String nodeId : new HashMap<>(connections).keySet()) {
            closeConnection(nodeId);
        }
    }
}
