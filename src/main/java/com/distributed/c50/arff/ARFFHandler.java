package com.distributed.c50.arff;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Handles ARFF file operations for the distributed C5.0 implementation.
 * Provides methods for loading, partitioning, and processing ARFF files using the Weka library.
 */
public class ARFFHandler {
    
    /**
     * Loads an ARFF file using Weka.
     * 
     * @param arffFile the ARFF file to load
     * @return the loaded instances
     * @throws IOException if an I/O error occurs
     */
    public static Instances loadArffFile(File arffFile) throws IOException {
        ArffLoader loader = new ArffLoader();
        loader.setFile(arffFile);
        return loader.getDataSet();
    }
    
    /**
     * Distributes attributes among parties for vertical partitioning.
     * 
     * @param data the dataset
     * @param numParties the number of parties
     * @param includeClass whether to include the class attribute in each partition
     * @return a 2D array where each row contains the attribute indices for a party
     */
    public static int[][] distributeAttributes(Instances data, int numParties, boolean includeClass) {
        int numAttributes = data.numAttributes();
        int classIndex = data.classIndex();
        if (classIndex < 0) {
            classIndex = numAttributes - 1; // Assume last attribute is class if not set
        }
        
        // Count non-class attributes
        int nonClassAttributes = numAttributes - 1;
        
        // Calculate attributes per party (approximately equal)
        int attributesPerParty = nonClassAttributes / numParties;
        int remainder = nonClassAttributes % numParties;
        
        // Create distribution array
        int[][] distribution = new int[numParties][];
        
        // Distribute attributes
        int currentAttribute = 0;
        for (int i = 0; i < numParties; i++) {
            // Calculate number of attributes for this party
            int partyAttributes = attributesPerParty;
            if (i < remainder) {
                partyAttributes++;
            }
            
            // Add class attribute if required
            if (includeClass) {
                distribution[i] = new int[partyAttributes + 1];
            } else {
                distribution[i] = new int[partyAttributes];
            }
            
            // Assign attributes
            int index = 0;
            for (int j = 0; j < numAttributes; j++) {
                if (j == classIndex) {
                    continue; // Skip class attribute for now
                }
                
                if (index < partyAttributes) {
                    distribution[i][index++] = j;
                    currentAttribute++;
                    
                    if (currentAttribute >= nonClassAttributes) {
                        break;
                    }
                }
            }
            
            // Add class attribute if required
            if (includeClass) {
                distribution[i][partyAttributes] = classIndex;
            }
        }
        
        return distribution;
    }
    
    /**
     * Creates a vertical partition of the dataset with the specified attributes.
     * 
     * @param data the original dataset
     * @param attributeIndices the indices of attributes to include in the partition
     * @param includeClass whether the class attribute is included in the partition
     * @return the partitioned dataset
     */
    public static Instances createVerticalPartition(Instances data, int[] attributeIndices, boolean includeClass) {
        // Create attribute list for the partition
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int index : attributeIndices) {
            attributes.add(data.attribute(index));
        }
        
        // Create dataset structure
        Instances partition = new Instances(data.relationName() + "_partition", attributes, data.numInstances());
        
        // Set class index if included
        int classIndex = data.classIndex();
        if (classIndex >= 0 && includeClass) {
            for (int i = 0; i < attributeIndices.length; i++) {
                if (attributeIndices[i] == classIndex) {
                    partition.setClassIndex(i);
                    break;
                }
            }
        }
        
        // Add instances
        for (int i = 0; i < data.numInstances(); i++) {
            Instance original = data.instance(i);
            double[] values = new double[attributeIndices.length];
            
            for (int j = 0; j < attributeIndices.length; j++) {
                values[j] = original.value(attributeIndices[j]);
            }
            
            partition.add(new DenseInstance(1.0, values));
        }
        
        return partition;
    }
    
    /**
     * Converts Weka instances to a 2D integer array for the C5.0 algorithm.
     * 
     * @param data the dataset
     * @return a 2D array where each row represents an instance and each column an attribute
     */
    public static int[][] instancesToArray(Instances data) {
        int numInstances = data.numInstances();
        int numAttributes = data.numAttributes();
        int[][] array = new int[numInstances][numAttributes];
        
        for (int i = 0; i < numInstances; i++) {
            Instance instance = data.instance(i);
            for (int j = 0; j < numAttributes; j++) {
                if (data.attribute(j).isNominal()) {
                    array[i][j] = (int) instance.value(j);
                } else {
                    // For numeric attributes, we need to discretize
                    // This is a simple discretization; more sophisticated methods could be used
                    double value = instance.value(j);
                    array[i][j] = discretizeNumericValue(value);
                }
            }
        }
        
        return array;
    }
    
    /**
     * Simple discretization of numeric values.
     * 
     * @param value the numeric value
     * @return a discretized integer value
     */
    private static int discretizeNumericValue(double value) {
        // Simple discretization into 10 bins
        // More sophisticated methods could be used
        if (Double.isNaN(value)) {
            return 0;
        }
        
        if (value <= 0.0) {
            return 0;
        } else if (value >= 1.0) {
            return 9;
        } else {
            return (int) (value * 10);
        }
    }
    
    /**
     * Splits a dataset into training and testing sets.
     * 
     * @param data the dataset
     * @param trainPercent the percentage of instances to use for training
     * @param seed the random seed for reproducibility
     * @return an array containing the training set at index 0 and testing set at index 1
     */
    public static Instances[] splitData(Instances data, double trainPercent, int seed) {
        data.randomize(new Random(seed));
        int trainSize = (int) Math.round(data.numInstances() * trainPercent / 100);
        int testSize = data.numInstances() - trainSize;
        
        Instances train = new Instances(data, 0, trainSize);
        Instances test = new Instances(data, trainSize, testSize);
        
        return new Instances[] { train, test };
    }
    
    /**
     * Merges multiple vertical partitions back into a single dataset.
     * 
     * @param partitions the array of partitions to merge
     * @param attributeMappings the attribute mappings for each partition
     * @return the merged dataset
     */
    public static Instances mergeVerticalPartitions(Instances[] partitions, int[][] attributeMappings) {
        if (partitions.length == 0) {
            return null;
        }
        
        // Determine total number of attributes and instances
        int totalAttributes = 0;
        for (int[] mapping : attributeMappings) {
            totalAttributes += mapping.length;
        }
        
        // Remove duplicates (e.g., class attribute might be in multiple partitions)
        boolean[] included = new boolean[totalAttributes];
        ArrayList<Attribute> attributes = new ArrayList<>();
        
        for (int p = 0; p < partitions.length; p++) {
            for (int i = 0; i < attributeMappings[p].length; i++) {
                int globalIndex = attributeMappings[p][i];
                if (!included[globalIndex]) {
                    included[globalIndex] = true;
                    attributes.add(partitions[p].attribute(i));
                }
            }
        }
        
        // Create merged dataset structure
        Instances merged = new Instances("merged", attributes, partitions[0].numInstances());
        
        // Set class index if available
        int classIndex = partitions[0].classIndex();
        if (classIndex >= 0) {
            merged.setClassIndex(classIndex);
        }
        
        // Add instances
        for (int i = 0; i < partitions[0].numInstances(); i++) {
            double[] values = new double[attributes.size()];
            Arrays.fill(values, Double.NaN); // Initialize with missing values
            
            // Fill in values from each partition
            for (int p = 0; p < partitions.length; p++) {
                Instance instance = partitions[p].instance(i);
                for (int j = 0; j < attributeMappings[p].length; j++) {
                    int globalIndex = attributeMappings[p][j];
                    values[globalIndex] = instance.value(j);
                }
            }
            
            merged.add(new DenseInstance(1.0, values));
        }
        
        return merged;
    }
}
