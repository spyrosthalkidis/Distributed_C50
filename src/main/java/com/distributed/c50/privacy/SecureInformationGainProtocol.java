package com.distributed.c50.privacy;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements secure information gain protocol for privacy-preserving computation
 * of information gain across multiple parties with vertically partitioned data.
 * Based on the approach described in the Vaidya and Clifton paper.
 */
public class SecureInformationGainProtocol {
    private final SecureRandom random;
    private final String nodeId;
    private final SecureSumProtocol secureSumProtocol;
    
    /**
     * Creates a new secure information gain protocol instance.
     * 
     * @param nodeId the unique identifier of this node
     * @param numParties the number of participating parties
     */
    public SecureInformationGainProtocol(String nodeId, int numParties) {
        this.nodeId = nodeId;
        this.random = new SecureRandom();
        this.secureSumProtocol = new SecureSumProtocol(nodeId, numParties);
    }
    
    /**
     * Computes local counts for an attribute's values and class combinations.
     * 
     * @param attributeValues the local attribute values
     * @param classValues the class values
     * @param numAttributeValues the number of distinct attribute values
     * @param numClassValues the number of distinct class values
     * @return a matrix of counts for each attribute-class combination
     */
    public int[][] computeLocalCounts(int[] attributeValues, int[] classValues,
                                     int numAttributeValues, int numClassValues) {
        // Initialize count matrix
        int[][] counts = new int[numAttributeValues][numClassValues];
        
        // Count instances for each attribute-class combination
        for (int i = 0; i < attributeValues.length; i++) {
            if (attributeValues[i] >= 0 && attributeValues[i] < numAttributeValues &&
                classValues[i] >= 0 && classValues[i] < numClassValues) {
                counts[attributeValues[i]][classValues[i]]++;
            }
        }
        
        return counts;
    }
    
    /**
     * Initiates secure computation of global counts as the coordinator.
     * 
     * @param localCounts the local count matrix
     * @return a map of secure sum states for each count
     */
    public Map<String, SecureSumProtocol.SecureSumState> initiateSecureCounts(int[][] localCounts) {
        Map<String, SecureSumProtocol.SecureSumState> states = new HashMap<>();
        
        // Initialize secure sum for each count
        for (int i = 0; i < localCounts.length; i++) {
            for (int j = 0; j < localCounts[i].length; j++) {
                String key = i + "," + j;
                states.put(key, secureSumProtocol.initiateSecureSum(localCounts[i][j]));
            }
        }
        
        return states;
    }
    
    /**
     * Participates in secure computation of global counts.
     * 
     * @param states the current secure sum states
     * @param localCounts the local count matrix
     * @return the updated secure sum states
     */
    public Map<String, SecureSumProtocol.SecureSumState> participateInSecureCounts(
            Map<String, SecureSumProtocol.SecureSumState> states, int[][] localCounts) {
        Map<String, SecureSumProtocol.SecureSumState> updatedStates = new HashMap<>();
        
        // Add local counts to secure sums
        for (Map.Entry<String, SecureSumProtocol.SecureSumState> entry : states.entrySet()) {
            String[] indices = entry.getKey().split(",");
            int i = Integer.parseInt(indices[0]);
            int j = Integer.parseInt(indices[1]);
            
            int localCount = 0;
            if (i < localCounts.length && j < localCounts[i].length) {
                localCount = localCounts[i][j];
            }
            
            updatedStates.put(entry.getKey(), 
                secureSumProtocol.participateInSecureSum(entry.getValue(), localCount));
        }
        
        return updatedStates;
    }
    
    /**
     * Finalizes secure computation of global counts as the coordinator.
     * 
     * @param states the final secure sum states
     * @param numAttributeValues the number of distinct attribute values
     * @param numClassValues the number of distinct class values
     * @return the global count matrix
     */
    public int[][] finalizeSecureCounts(Map<String, SecureSumProtocol.SecureSumState> states,
                                      int numAttributeValues, int numClassValues) {
        int[][] globalCounts = new int[numAttributeValues][numClassValues];
        
        // Finalize secure sums to get global counts
        for (Map.Entry<String, SecureSumProtocol.SecureSumState> entry : states.entrySet()) {
            String[] indices = entry.getKey().split(",");
            int i = Integer.parseInt(indices[0]);
            int j = Integer.parseInt(indices[1]);
            
            if (i < globalCounts.length && j < globalCounts[i].length) {
                globalCounts[i][j] = secureSumProtocol.finalizeSecureSum(entry.getValue());
            }
        }
        
        return globalCounts;
    }
    
    /**
     * Computes information gain from global counts.
     * 
     * @param globalCounts the global count matrix
     * @param totalInstances the total number of instances
     * @return the information gain
     */
    public double computeInformationGain(int[][] globalCounts, int totalInstances) {
        if (totalInstances == 0) {
            return 0.0;
        }
        
        // Calculate class entropy
        double classEntropy = 0.0;
        int[] classTotals = new int[globalCounts[0].length];
        
        // Sum up class totals
        for (int i = 0; i < globalCounts.length; i++) {
            for (int j = 0; j < globalCounts[i].length; j++) {
                classTotals[j] += globalCounts[i][j];
            }
        }
        
        // Calculate class entropy
        for (int j = 0; j < classTotals.length; j++) {
            if (classTotals[j] > 0) {
                double p = (double) classTotals[j] / totalInstances;
                classEntropy -= p * Math.log(p) / Math.log(2);
            }
        }
        
        // Calculate conditional entropy
        double conditionalEntropy = 0.0;
        int[] attributeTotals = new int[globalCounts.length];
        
        // Sum up attribute totals
        for (int i = 0; i < globalCounts.length; i++) {
            for (int j = 0; j < globalCounts[i].length; j++) {
                attributeTotals[i] += globalCounts[i][j];
            }
        }
        
        // Calculate conditional entropy
        for (int i = 0; i < globalCounts.length; i++) {
            if (attributeTotals[i] > 0) {
                double attributeEntropy = 0.0;
                for (int j = 0; j < globalCounts[i].length; j++) {
                    if (globalCounts[i][j] > 0) {
                        double p = (double) globalCounts[i][j] / attributeTotals[i];
                        attributeEntropy -= p * Math.log(p) / Math.log(2);
                    }
                }
                conditionalEntropy += (double) attributeTotals[i] / totalInstances * attributeEntropy;
            }
        }
        
        // Information gain = class entropy - conditional entropy
        return classEntropy - conditionalEntropy;
    }
    
    /**
     * Computes gain ratio from information gain and split information.
     * 
     * @param informationGain the information gain
     * @param globalCounts the global count matrix
     * @param totalInstances the total number of instances
     * @return the gain ratio
     */
    public double computeGainRatio(double informationGain, int[][] globalCounts, int totalInstances) {
        if (totalInstances == 0) {
            return 0.0;
        }
        
        // Calculate split information
        double splitInfo = 0.0;
        int[] attributeTotals = new int[globalCounts.length];
        
        // Sum up attribute totals
        for (int i = 0; i < globalCounts.length; i++) {
            for (int j = 0; j < globalCounts[i].length; j++) {
                attributeTotals[i] += globalCounts[i][j];
            }
        }
        
        // Calculate split information
        for (int i = 0; i < attributeTotals.length; i++) {
            if (attributeTotals[i] > 0) {
                double p = (double) attributeTotals[i] / totalInstances;
                splitInfo -= p * Math.log(p) / Math.log(2);
            }
        }
        
        // Avoid division by zero
        if (splitInfo < 1e-10) {
            return 0.0;
        }
        
        // Gain ratio = information gain / split information
        return informationGain / splitInfo;
    }
}
