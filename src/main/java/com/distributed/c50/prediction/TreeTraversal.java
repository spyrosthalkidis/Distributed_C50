package com.distributed.c50.prediction;

import com.distributed.c50.model.TreeNode;
import com.distributed.c50.parser.ValuesFileParser;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Class for traversing a decision tree with a values file input.
 * This class handles the prediction process using a trained decision tree
 * and a values file containing a single instance.
 */
public class TreeTraversal {
    
    /**
     * Traverses a decision tree with the values from a values file.
     * 
     * @param root the root node of the decision tree
     * @param valuesFile the values file containing a single instance
     * @return the predicted class label
     * @throws IOException if an I/O error occurs
     */
    public static String traverseTree(TreeNode root, File valuesFile) throws IOException {
        // Parse the values file
        Map<String, Double> featureValues = ValuesFileParser.parseValuesFile(valuesFile);
        
        // Traverse the tree
        return traverseTree(root, featureValues);
    }
    
    /**
     * Traverses a decision tree with the given feature values.
     * 
     * @param node the current node in the tree
     * @param featureValues the feature values for the instance
     * @return the predicted class label
     */
    private static String traverseTree(TreeNode node, Map<String, Double> featureValues) {
        // If this is a leaf node, return the class label
        if (node.isLeaf()) {
            return node.getClassLabel();
        }
        
        // Get the feature value for the current node's split attribute
        String featureName = node.getSplitAttribute();
        Double featureValue = featureValues.get(featureName);
        
        // If the feature value is missing, use the default branch
        if (featureValue == null) {
            System.out.println("Warning: Feature '" + featureName + "' not found in values file. Using default branch.");
            return traverseTree(node.getDefaultChild(), featureValues);
        }
        
        // Determine which child to traverse to based on the feature value
        TreeNode nextNode = null;
        
        // For numeric attributes
        if (node.isNumericSplit()) {
            double threshold = node.getSplitThreshold();
            if (featureValue <= threshold) {
                nextNode = node.getLeftChild();
            } else {
                nextNode = node.getRightChild();
            }
        } 
        // For categorical attributes
        else {
            // Find the matching branch for the categorical value
            // This is simplified - in a real implementation, you would need to handle
            // categorical values properly based on your tree structure
            int valueIndex = (int) Math.round(featureValue);
            nextNode = node.getChild(valueIndex);
            
            // If no matching branch, use default
            if (nextNode == null) {
                nextNode = node.getDefaultChild();
            }
        }
        
        // If no valid next node, use default
        if (nextNode == null) {
            nextNode = node.getDefaultChild();
        }
        
        // Continue traversal with the next node
        return traverseTree(nextNode, featureValues);
    }
}
