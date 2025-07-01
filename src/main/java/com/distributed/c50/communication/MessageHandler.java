package com.distributed.c50.communication;

/**
 * Interface for processing messages received from other nodes.
 */
public interface MessageHandler {
    
    /**
     * Sends a message to a remote node.
     * 
     * @param message the message to send
     * @return true if sent successfully, false otherwise
     */
    boolean sendMessage(Message message);
    
    /**
     * Interface for message processors.
     */
    interface MessageProcessor {
        /**
         * Processes a received message.
         * 
         * @param message the received message
         * @return true if processed successfully, false otherwise
         */
        boolean processMessage(Message message);
    }
    
    /**
     * Implementation of the MessageHandler interface.
     */
    class MessageHandlerImpl implements MessageHandler {
        private final SecureSocketManager socketManager;
        private final MessageProcessor processor;
        
        /**
         * Creates a new message handler.
         * 
         * @param socketManager the secure socket manager
         * @param processor the message processor
         */
        public MessageHandlerImpl(SecureSocketManager socketManager, MessageProcessor processor) {
            this.socketManager = socketManager;
            this.processor = processor;
        }
        
        /**
         * Sends a message to a remote node.
         * 
         * @param message the message to send
         * @return true if sent successfully, false otherwise
         */
        @Override
        public boolean sendMessage(Message message) {
            return socketManager.sendMessage(message.getDestinationId(), message);
        }
        
        /**
         * Receives and processes a message from a remote node.
         * 
         * @param nodeId the ID of the remote node
         * @return true if received and processed successfully, false otherwise
         */
        public boolean receiveAndProcessMessage(String nodeId) {
            Message message = socketManager.receiveMessage(nodeId);
            if (message == null) {
                return false;
            }
            
            return processor.processMessage(message);
        }
    }
}
