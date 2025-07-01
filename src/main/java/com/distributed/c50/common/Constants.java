package com.distributed.c50.common;

/**
 * Constants used throughout the distributed C5.0 implementation.
 */
public class Constants {
    /**
     * Default port for the coordinator node.
     */
    public static final int DEFAULT_COORDINATOR_PORT = 9000;
    
    /**
     * Default base port for data party nodes.
     */
    public static final int DEFAULT_DATA_PARTY_BASE_PORT = 9001;
    
    /**
     * Maximum depth of the decision tree.
     */
    public static final int MAX_TREE_DEPTH = 10;
    
    /**
     * Minimum number of instances required for a leaf node.
     */
    public static final int MIN_INSTANCES_PER_LEAF = 5;
    
    /**
     * Minimum gain ratio threshold for splitting.
     */
    public static final double MIN_GAIN_THRESHOLD = 0.01;
    
    /**
     * Socket timeout in milliseconds.
     */
    public static final int SOCKET_TIMEOUT = 30000;
    
    /**
     * Buffer size for socket communication.
     */
    public static final int BUFFER_SIZE = 8192;
    
    /**
     * Maximum number of connection retries.
     */
    public static final int MAX_CONNECTION_RETRIES = 3;
    
    /**
     * Delay between connection retries in milliseconds.
     */
    public static final int CONNECTION_RETRY_DELAY = 1000;
}
