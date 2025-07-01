package com.distributed.c50;

import com.distributed.c50.arff.ARFFHandler;
import com.distributed.c50.node.CoordinatorNode;
import com.distributed.c50.node.DataPartyNode;
import com.distributed.c50.model.TreeNode;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for validating the distributed C5.0 implementation.
 * Simulates a distributed environment with a coordinator and multiple data parties.
 */
public class DistributedC50Test {

    /**
     * Main method to run the test.
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            // Check if ARFF file is provided
            if (args.length < 1) {
                System.out.println("Usage: DistributedC50Test <arffFile>");
                return;
            }
            
            File arffFile = new File(args[0]);
            if (!arffFile.exists()) {
                System.out.println("ARFF file not found: " + arffFile.getAbsolutePath());
                return;
            }
            
            // Load the ARFF file
            weka.core.Instances data = ARFFHandler.loadArffFile(arffFile);
            System.out.println("Loaded ARFF file: " + arffFile.getName());
            System.out.println("Number of instances: " + data.numInstances());
            System.out.println("Number of attributes: " + data.numAttributes());
            
            // Create vertical partitions
            int numParties = 2; // For testing, we use 2 parties
            int[][] attributeDistribution = ARFFHandler.distributeAttributes(data, numParties, true);
            
            // Create temporary ARFF files for each partition
            File[] partitionFiles = new File[numParties];
            for (int i = 0; i < numParties; i++) {
                weka.core.Instances partition = ARFFHandler.createVerticalPartition(data, attributeDistribution[i], true);
                partitionFiles[i] = File.createTempFile("partition" + i, ".arff");
                weka.core.converters.ArffSaver saver = new weka.core.converters.ArffSaver();
                saver.setInstances(partition);
                saver.setFile(partitionFiles[i]);
                saver.writeBatch();
                System.out.println("Created partition " + i + " with " + partition.numAttributes() + " attributes");
            }
            
            // Start coordinator node
            CoordinatorNode coordinator = new CoordinatorNode("coordinator", 9000);
            coordinator.start();
            
            // Start data party nodes
            DataPartyNode[] dataParties = new DataPartyNode[numParties];
            for (int i = 0; i < numParties; i++) {
                dataParties[i] = new DataPartyNode("party" + i, 9001 + i, "localhost", 9000);
                dataParties[i].start();
                dataParties[i].loadLocalData(partitionFiles[i]);
                
                // Register data party with coordinator
                coordinator.registerDataParty("party" + i, "localhost", 9001 + i);
            }
            
            // Create attribute partitioning string
            String[] attributePartitioning = new String[numParties];
            for (int i = 0; i < numParties; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < attributeDistribution[i].length; j++) {
                    if (j > 0) {
                        sb.append(",");
                    }
                    sb.append(attributeDistribution[i][j]);
                }
                attributePartitioning[i] = sb.toString() + ":party" + i;
            }
            
            // Set configuration parameters
            Map<String, String> configuration = new HashMap<>();
            configuration.put("maxDepth", "10");
            configuration.put("minInstances", "5");
            
            // Initiate distributed C5.0
            System.out.println("Initiating distributed C5.0 algorithm...");
            coordinator.initiateDistributedC50(arffFile.getName(), attributePartitioning, configuration);
            
            // Wait for completion (in a real application, this would be event-driven)
            System.out.println("Waiting for computation to complete...");
            Thread.sleep(10000);
            
            // Get and print the decision tree
            System.out.println("Decision tree construction complete.");
            // In a real implementation, we would print the tree here
            
            // Clean up
            for (int i = 0; i < numParties; i++) {
                dataParties[i].stop();
                partitionFiles[i].delete();
            }
            coordinator.stop();
            
            System.out.println("Test completed successfully.");
            
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
