package com.distributed.c50.communication;

import java.util.Map;

/**
 * Message for initiating the distributed C5.0 algorithm.
 */
public class InitiationMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private final String coordinatorId;
    private final String[] participatingNodes;
    private final String datasetName;
    private final String[] attributePartitioning;
    private final Map<String, String> configuration;
    
    /**
     * Creates a new initiation message.
     * 
     * @param sourceId the ID of the source node
     * @param destinationId the ID of the destination node
     * @param coordinatorId the ID of the coordinator node
     * @param participatingNodes the IDs of all participating nodes
     * @param datasetName the name of the dataset
     * @param attributePartitioning the partitioning of attributes among parties
     * @param configuration additional configuration parameters
     */
    public InitiationMessage(String sourceId, String destinationId, String coordinatorId,
                           String[] participatingNodes, String datasetName,
                           String[] attributePartitioning, Map<String, String> configuration) {
        super(sourceId, destinationId, "INITIATION");
        this.coordinatorId = coordinatorId;
        this.participatingNodes = participatingNodes;
        this.datasetName = datasetName;
        this.attributePartitioning = attributePartitioning;
        this.configuration = configuration;
    }
    
    /**
     * Gets the ID of the coordinator node.
     * 
     * @return the coordinator node ID
     */
    public String getCoordinatorId() {
        return coordinatorId;
    }
    
    /**
     * Gets the IDs of all participating nodes.
     * 
     * @return array of node IDs
     */
    public String[] getParticipatingNodes() {
        return participatingNodes;
    }
    
    /**
     * Gets the name of the dataset.
     * 
     * @return the dataset name
     */
    public String getDatasetName() {
        return datasetName;
    }
    
    /**
     * Gets the partitioning of attributes among parties.
     * 
     * @return array of attribute partitioning strings
     */
    public String[] getAttributePartitioning() {
        return attributePartitioning;
    }
    
    /**
     * Gets the additional configuration parameters.
     * 
     * @return map of configuration parameters
     */
    public Map<String, String> getConfiguration() {
        return configuration;
    }
}
