package com.distributed.c50.core;

import com.distributed.c50.common.Constants;
import com.distributed.c50.model.AttributeMetadata;
import com.distributed.c50.model.TreeNode;
import com.distributed.c50.privacy.SecureInformationGainProtocol;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core implementation of the distributed C5.0 algorithm for vertically partitioned data.
 * Implements the privacy-preserving decision tree building process based on the
 * Vaidya and Clifton approach.
 */
public class DistributedC50Core {
    private final String nodeId;
    private final int numParties;
    private final SecureInformationGainProtocol gainProtocol;
    
    /**
     * Creates a new distributed C5.0 core instance.
     * 
     * @param nodeId the unique identifier of this node
     * @param numParties the number of participating parties
     */
    public DistributedC50Core(String nodeId, int numParties) {
        this.nodeId = nodeId;
        this.numParties = numParties;
        this.gainProtocol = new SecureInformationGainProtocol(nodeId, numParties);
    }
    
    /**
     * Builds a distributed decision tree using the C5.0 algorithm.
     * 
     * @param dataPartition the local data partition
     * @param attributeMetadata metadata about all attributes in the dataset
     * @param classAttributeIndex the index of the class attribute
     * @return the root node of the decision tree
     */
    public TreeNode buildDecisionTree(int[][] dataPartition, 
                                     AttributeMetadata[] attributeMetadata,
                                     int classAttributeIndex) {
        // Create root node
        TreeNode root = new TreeNode();
        
        // Build tree recursively
        buildTreeRecursive(root, dataPartition, attributeMetadata, classAttributeIndex, 
                          new ArrayList<>(), 0);
        
        return root;
    }
    
    /**
     * Recursively builds a decision tree.
     * 
     * @param node the current tree node
     * @param dataPartition the local data partition
     * @param attributeMetadata metadata about all attributes in the dataset
     * @param classAttributeIndex the index of the class attribute
     * @param usedAttributes list of attributes already used in the path
     * @param depth current depth in the tree
     */
    private void buildTreeRecursive(TreeNode node, int[][] dataPartition,
                                   AttributeMetadata[] attributeMetadata,
                                   int classAttributeIndex, List<Integer> usedAttributes,
                                   int depth) {
        // Check stopping criteria
        if (depth >= Constants.MAX_TREE_DEPTH || 
            dataPartition.length < Constants.MIN_INSTANCES_PER_LEAF ||
            isHomogeneous(dataPartition, classAttributeIndex) ||
            usedAttributes.size() >= attributeMetadata.length - 1) {
            
            // Create leaf node
            node.setLeaf(true);
            node.setClassDistribution(computeClassDistribution(dataPartition, classAttributeIndex,
                                                             attributeMetadata[classAttributeIndex].getNumValues()));
            return;
        }
        
        // Find best attribute to split on
        BestSplitResult bestSplit = findBestSplit(dataPartition, attributeMetadata, 
                                                classAttributeIndex, usedAttributes);
        
        if (bestSplit == null || bestSplit.getGainRatio() < Constants.MIN_GAIN_THRESHOLD) {
            // No good split found, create leaf node
            node.setLeaf(true);
            node.setClassDistribution(computeClassDistribution(dataPartition, classAttributeIndex,
                                                             attributeMetadata[classAttributeIndex].getNumValues()));
            return;
        }
        
        // Set node information
        node.setAttributeIndex(bestSplit.getAttributeIndex());
        node.setAttributeName(attributeMetadata[bestSplit.getAttributeIndex()].getName());
        
        // Create child nodes
        int numValues = attributeMetadata[bestSplit.getAttributeIndex()].getNumValues();
        node.setChildren(new TreeNode[numValues]);
        
        // Add attribute to used list
        List<Integer> updatedUsedAttributes = new ArrayList<>(usedAttributes);
        updatedUsedAttributes.add(bestSplit.getAttributeIndex());
        
        // Create and process child nodes
        for (int valueIndex = 0; valueIndex < numValues; valueIndex++) {
            // Create child node
            TreeNode child = new TreeNode();
            node.getChildren()[valueIndex] = child;
            
            // Filter data for this child
            int[][] childData = filterDataByAttributeValue(dataPartition, 
                                                         bestSplit.getAttributeIndex(),
                                                         valueIndex);
            
            // Recursively build subtree
            buildTreeRecursive(child, childData, attributeMetadata, classAttributeIndex,
                              updatedUsedAttributes, depth + 1);
        }
    }
    
    /**
     * Finds the best attribute to split on using secure information gain computation.
     * 
     * @param dataPartition the local data partition
     * @param attributeMetadata metadata about all attributes in the dataset
     * @param classAttributeIndex the index of the class attribute
     * @param usedAttributes list of attributes already used in the path
     * @return the best split result, or null if no good split was found
     */
    private BestSplitResult findBestSplit(int[][] dataPartition, 
                                        AttributeMetadata[] attributeMetadata,
                                        int classAttributeIndex,
                                        List<Integer> usedAttributes) {
        BestSplitResult bestSplit = null;
        double bestGainRatio = -1;
        
        // Try each attribute
        for (int attrIndex = 0; attrIndex < attributeMetadata.length; attrIndex++) {
            // Skip class attribute and already used attributes
            if (attrIndex == classAttributeIndex || usedAttributes.contains(attrIndex)) {
                continue;
            }
            
            // Skip non-nominal attributes (for simplicity)
            if (attributeMetadata[attrIndex].getType() != AttributeMetadata.TYPE_NOMINAL) {
                continue;
            }
            
            // Extract attribute and class values
            int[] attributeValues = extractAttributeValues(dataPartition, attrIndex);
            int[] classValues = extractAttributeValues(dataPartition, classAttributeIndex);
            
            // Compute local counts
            int[][] localCounts = gainProtocol.computeLocalCounts(
                attributeValues, classValues,
                attributeMetadata[attrIndex].getNumValues(),
                attributeMetadata[classAttributeIndex].getNumValues());
            
            // Perform secure computation of global counts
            // Note: In a real implementation, this would involve communication with other parties
            // For simplicity, we assume the global counts are the same as local counts
            int[][] globalCounts = localCounts;
            
            // Compute information gain and gain ratio
            int totalInstances = dataPartition.length;
            double infoGain = gainProtocol.computeInformationGain(globalCounts, totalInstances);
            double gainRatio = gainProtocol.computeGainRatio(infoGain, globalCounts, totalInstances);
            
            // Update best split if this is better
            if (gainRatio > bestGainRatio) {
                bestGainRatio = gainRatio;
                bestSplit = new BestSplitResult(attrIndex, infoGain, gainRatio);
            }
        }
        
        return bestSplit;
    }
    
    /**
     * Extracts values for a specific attribute from the data partition.
     * 
     * @param dataPartition the data partition
     * @param attributeIndex the index of the attribute
     * @return array of attribute values
     */
    private int[] extractAttributeValues(int[][] dataPartition, int attributeIndex) {
        int[] values = new int[dataPartition.length];
        
        for (int i = 0; i < dataPartition.length; i++) {
            values[i] = dataPartition[i][attributeIndex];
        }
        
        return values;
    }
    
    /**
     * Filters data by attribute value.
     * 
     * @param dataPartition the data partition
     * @param attributeIndex the index of the attribute
     * @param valueIndex the value index to filter by
     * @return filtered data partition
     */
    private int[][] filterDataByAttributeValue(int[][] dataPartition, 
                                             int attributeIndex, 
                                             int valueIndex) {
        // Count matching instances
        int count = 0;
        for (int i = 0; i < dataPartition.length; i++) {
            if (dataPartition[i][attributeIndex] == valueIndex) {
                count++;
            }
        }
        
        // Create filtered data
        int[][] filteredData = new int[count][];
        int index = 0;
        
        for (int i = 0; i < dataPartition.length; i++) {
            if (dataPartition[i][attributeIndex] == valueIndex) {
                filteredData[index++] = dataPartition[i];
            }
        }
        
        return filteredData;
    }
    
    /**
     * Checks if all instances in the data partition have the same class value.
     * 
     * @param dataPartition the data partition
     * @param classAttributeIndex the index of the class attribute
     * @return true if all instances have the same class value, false otherwise
     */
    private boolean isHomogeneous(int[][] dataPartition, int classAttributeIndex) {
        if (dataPartition.length == 0) {
            return true;
        }
        
        int firstClassValue = dataPartition[0][classAttributeIndex];
        
        for (int i = 1; i < dataPartition.length; i++) {
            if (dataPartition[i][classAttributeIndex] != firstClassValue) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Computes the class distribution for a data partition.
     * 
     * @param dataPartition the data partition
     * @param classAttributeIndex the index of the class attribute
     * @param numClassValues the number of distinct class values
     * @return array of class counts
     */
    private int[] computeClassDistribution(int[][] dataPartition, 
                                         int classAttributeIndex,
                                         int numClassValues) {
        int[] distribution = new int[numClassValues];
        
        for (int i = 0; i < dataPartition.length; i++) {
            int classValue = dataPartition[i][classAttributeIndex];
            if (classValue >= 0 && classValue < numClassValues) {
                distribution[classValue]++;
            }
        }
        
        return distribution;
    }
    
    /**
     * Class representing the result of finding the best split.
     */
    private static class BestSplitResult {
        private final int attributeIndex;
        private final double informationGain;
        private final double gainRatio;
        
        /**
         * Creates a new best split result.
         * 
         * @param attributeIndex the index of the best attribute
         * @param informationGain the information gain of the split
         * @param gainRatio the gain ratio of the split
         */
        public BestSplitResult(int attributeIndex, double informationGain, double gainRatio) {
            this.attributeIndex = attributeIndex;
            this.informationGain = informationGain;
            this.gainRatio = gainRatio;
        }
        
        /**
         * Gets the attribute index.
         * 
         * @return the attribute index
         */
        public int getAttributeIndex() {
            return attributeIndex;
        }
        
        /**
         * Gets the information gain.
         * 
         * @return the information gain
         */
        public double getInformationGain() {
            return informationGain;
        }
        
        /**
         * Gets the gain ratio.
         * 
         * @return the gain ratio
         */
        public double getGainRatio() {
            return gainRatio;
        }
    }
}
