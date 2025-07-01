package com.distributed.c50.communication;

import java.io.Serializable;

/**
 * Base class for all messages exchanged between nodes in the distributed C5.0 system.
 */
public abstract class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String sourceId;
    private final String destinationId;
    private final String messageType;
    
    /**
     * Creates a new message.
     * 
     * @param sourceId the ID of the source node
     * @param destinationId the ID of the destination node
     * @param messageType the type of message
     */
    public Message(String sourceId, String destinationId, String messageType) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.messageType = messageType;
    }
    
    /**
     * Gets the ID of the source node.
     * 
     * @return the source node ID
     */
    public String getSourceId() {
        return sourceId;
    }
    
    /**
     * Gets the ID of the destination node.
     * 
     * @return the destination node ID
     */
    public String getDestinationId() {
        return destinationId;
    }
    
    /**
     * Gets the type of message.
     * 
     * @return the message type
     */
    public String getMessageType() {
        return messageType;
    }
}
