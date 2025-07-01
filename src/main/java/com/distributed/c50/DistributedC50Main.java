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
 * Main class for the distributed C5.0 implementation.
 * Provides a command-line interface for running the distributed C5.0 algorithm.
 */
public class DistributedC50Main {

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
            System.out.println("Usage: DistributedC50Main coordinator <port>");
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
            System.out.println("Usage: DistributedC50Main dataparty <nodeId> <port> <coordinatorHost> <coordinatorPort> [arffFile]");
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
     * Runs a test of the distributed C5.0 algorithm.
     * 
     * @param args command-line arguments
     * @throws Exception if an error occurs
     */
    private static void runTest(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: DistributedC50Main test <arffFile>");
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
     * Prints usage information.
     */
    private static void printUsage() {
        System.out.println("Usage: DistributedC50Main <command> [args...]");
        System.out.println("Commands:");
        System.out.println("  coordinator <port>                                   - Run a coordinator node");
        System.out.println("  dataparty <nodeId> <port> <coordHost> <coordPort> [arffFile] - Run a data party node");
        System.out.println("  test <arffFile>                                      - Run a test of the distributed C5.0 algorithm");
    }
}
