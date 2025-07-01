package com.distributed.c50;

import com.distributed.c50.arff.ARFFHandler;
import com.distributed.c50.node.CoordinatorNode;
import com.distributed.c50.node.DataPartyNode;
import com.distributed.c50.model.TreeNode;
import com.distributed.c50.parser.ValuesFileParser;
import com.distributed.c50.prediction.TreeTraversal;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main class for the distributed C5.0 implementation with combined ARFF training and values prediction.
 * Provides a command-line interface for running the distributed C5.0 algorithm.
 */
public class DistributedC50CombinedMain {

    /**
     * Main method to run the distributed C5.0 algorithm.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            // Parse command-line arguments
            if (args.length < 1) {
                printUsage();
                return;
            }

            String command = args[0];

            if (command.equals("coordinator")) {
                runCoordinator(args);
            } else if (command.equals("dataparty")) {
                runDataParty(args);
            } else if (command.equals("test")) {
                runTest(args);
            } else if (command.equals("values")) {
                runValuesTest(args);
            } else if (command.equals("predict")) {
                runPrediction(args);
            } else {
                System.out.println("Unknown command: " + command);
                printUsage();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error: Insufficient arguments provided.");
            printUsage();
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Runs the coordinator node.
     *
     * @param args command-line arguments
     * @throws Exception if an error occurs
     */
    private static void runCoordinator(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: DistributedC50CombinedMain coordinator <port>");
            return;
        }

        int port = Integer.parseInt(args[1]);

        // Create and start coordinator node
        CoordinatorNode coordinator = new CoordinatorNode("coordinator", port);
        coordinator.start();

        // Wait for termination signal
        System.out.println("Press Enter to stop the coordinator node...");
        System.in.read();

        // Stop coordinator
        coordinator.stop();
    }

    /**
     * Runs a data party node.
     *
     * @param args command-line arguments
     * @throws Exception if an error occurs
     */
    private static void runDataParty(String[] args) throws Exception {
        if (args.length < 5) {
            System.out.println("Usage: DistributedC50CombinedMain dataparty <nodeId> <port> <coordinatorHost> <coordinatorPort> [arffFile]");
            return;
        }

        String nodeId = args[1];
        int port = Integer.parseInt(args[2]);
        String coordinatorHost = args[3];
        int coordinatorPort = Integer.parseInt(args[4]);

        // Create and start data party node
        DataPartyNode dataParty = new DataPartyNode(nodeId, port, coordinatorHost, coordinatorPort);
        dataParty.start();

        // Load local data if specified
        if (args.length > 5) {
            File arffFile = new File(args[5]);
            dataParty.loadLocalData(arffFile);
        }

        // Wait for termination signal
        System.out.println("Press Enter to stop the data party node...");
        System.in.read();

        // Stop data party
        dataParty.stop();
    }

    /**
     * Runs a test of the distributed C5.0 algorithm with an ARFF file.
     *
     * @param args command-line arguments
     * @throws Exception if an error occurs
     */
    private static void runTest(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: DistributedC50CombinedMain test <arffFile>");
            return;
        }

        // Create a new array with just the ARFF file path
        String[] testArgs = new String[1];
        testArgs[0] = args[1]; // Only pass the ARFF file path to DistributedC50Test

        // Verify the ARFF file exists before proceeding
        File arffFile = new File(testArgs[0]);
        if (!arffFile.exists()) {
            System.err.println("ARFF file not found: " + arffFile.getAbsolutePath());
            return;
        }

        System.out.println("Running test with ARFF file: " + arffFile.getAbsolutePath());
        DistributedC50Test.main(testArgs);
    }

    /**
     * Runs a test of the distributed C5.0 algorithm with .values files.
     *
     * @param args command-line arguments
     * @throws Exception if an error occurs
     */
    private static void runValuesTest(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: DistributedC50CombinedMain values <valuesFileOrDirectory>");
            return;
        }

        // Create a new array with just the values file or directory path
        String[] testArgs = new String[1];
        testArgs[0] = args[1]; // Only pass the values file or directory path

        // Verify the values file or directory exists before proceeding
        File valuesFileOrDir = new File(testArgs[0]);
        if (!valuesFileOrDir.exists()) {
            System.err.println("Values file or directory not found: " + valuesFileOrDir.getAbsolutePath());
            return;
        }

        System.out.println("Running test with values file or directory: " + valuesFileOrDir.getAbsolutePath());
        DistributedC50ValuesTest.main(testArgs);
    }

    /**
     * Runs prediction using an ARFF file for training and a values file for prediction.
     *
     * @param args command-line arguments
     * @throws Exception if an error occurs
     */
    private static void runPrediction(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: DistributedC50CombinedMain predict <arffFile> <valuesFile>");
            return;
        }

        File arffFile = new File(args[1]);
        File valuesFile = new File(args[2]);

        // Verify files exist
        if (!arffFile.exists()) {
            System.err.println("ARFF file not found: " + arffFile.getAbsolutePath());
            return;
        }
        if (!valuesFile.exists()) {
            System.err.println("Values file not found: " + valuesFile.getAbsolutePath());
            return;
        }

        System.out.println("Training with ARFF file: " + arffFile.getAbsolutePath());
        System.out.println("Predicting with values file: " + valuesFile.getAbsolutePath());

        // Load the ARFF file
        weka.core.Instances data = ARFFHandler.loadArffFile(arffFile);
        System.out.println("Loaded ARFF file: " + arffFile.getName());
        System.out.println("Number of instances: " + data.numInstances());
        System.out.println("Number of attributes: " + data.numAttributes());

        // Set the class index to the last attribute if not already set
        if (data.classIndex() < 0) {
            data.setClassIndex(data.numAttributes() - 1);
        }

        // Train a C5.0 decision tree
        System.out.println("Training C5.0 decision tree...");

        // For simplicity, we'll use a simulated tree here
        // In a real implementation, you would use the distributed C5.0 algorithm
        TreeNode root = simulateTrainedTree(data);

        // Use the values file for prediction
        System.out.println("Predicting with values file...");
        String prediction = TreeTraversal.traverseTree(root, valuesFile);

        // Output the prediction
        System.out.println("Prediction: " + prediction);
    }


    /**
     * Simulates a trained decision tree dynamically, based on the actual attributes of the data.
     * In a real implementation, this should use the distributed C5.0 algorithm.
     *
     * @param data the training data
     * @return the root node of the trained decision tree
     */
    private static TreeNode simulateTrainedTree(weka.core.Instances data) {
        if (data.numAttributes() < 2) {
            throw new IllegalArgumentException("Need at least one attribute and one class attribute.");
        }

        // Get first non-class attribute as split
        String splitAttribute = null;
        for (int i = 0; i < data.numAttributes(); i++) {
            if (i != data.classIndex()) {
                splitAttribute = data.attribute(i).name();
                break;
            }
        }
        if (splitAttribute == null) {
            throw new IllegalStateException("No suitable split attribute found.");
        }

        TreeNode root = new TreeNode();
        root.setSplitAttribute(splitAttribute);
        root.setNumericSplit(true); // assume numeric for this simple example
        root.setSplitThreshold(0.5); // dummy threshold

        // Create child nodes with most frequent class label
        String majorityClass = data.classAttribute().value(0); // default to first label
        if (data.numInstances() > 0) {
            // Count class frequencies
            Map<String, Integer> freq = new HashMap<>();
            for (int i = 0; i < data.numInstances(); i++) {
                String label = data.instance(i).stringValue(data.classIndex());
                freq.put(label, freq.getOrDefault(label, 0) + 1);
            }
            // Find most common
            int max = -1;
            for (Map.Entry<String, Integer> e : freq.entrySet()) {
                if (e.getValue() > max) {
                    max = e.getValue();
                    majorityClass = e.getKey();
                }
            }
        }

        TreeNode leftChild = new TreeNode();
        leftChild.setLeaf(true);
        leftChild.setClassLabel(majorityClass);

        TreeNode rightChild = new TreeNode();
        rightChild.setLeaf(true);
        rightChild.setClassLabel(majorityClass);

        root.setLeftChild(leftChild);
        root.setRightChild(rightChild);

        return root;
    }

    /**
     * Prints usage information.
     */
    private static void printUsage() {
        System.out.println("Usage: DistributedC50CombinedMain <command> [args...]");
        System.out.println("Commands:");
        System.out.println("  coordinator <port>                                   - Run a coordinator node");
        System.out.println("  dataparty <nodeId> <port> <coordHost> <coordPort> [arffFile] - Run a data party node");
        System.out.println("  test <arffFile>                                      - Run a test with an ARFF file");
        System.out.println("  values <valuesFileOrDirectory>                       - Run a test with .values files");
        System.out.println("  predict <arffFile> <valuesFile>                      - Train with ARFF and predict with values file");
    }
}
