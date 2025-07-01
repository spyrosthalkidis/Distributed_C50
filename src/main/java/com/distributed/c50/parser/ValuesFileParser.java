package com.distributed.c50.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * Parser for the .values file format.
 * This format consists of tab-separated key-value pairs, where each line
 * represents a feature name and its value.
 */
public class ValuesFileParser {

    /**
     * Parses a .values file and returns a map of feature names to values.
     *
     * @param file the .values file to parse
     * @return a map of feature names to values
     * @throws IOException if an I/O error occurs
     */
    public static Map<String, Double> parseValuesFile(File file) throws IOException {
        Map<String, Double> featureValues = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Split the line by tab
                String[] parts = line.split("\\t");
                if (parts.length != 2) {
                    throw new IOException("Invalid line format: " + line);
                }

                String featureName = parts[0].trim();
                double featureValue;
                double ivalues = 0;
                try {
                    String svalues=parts[1].trim();

                    if (svalues.equals("f")){
                        ivalues=0;
                    }
                    if (svalues.equals("t")){
                        ivalues=1;
                    }
                    featureValue = Double.parseDouble(ivalues+"");
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid feature value: " + parts[1], e);
                }

                featureValues.put(featureName, featureValue);
            }
        }

        return featureValues;
    }

    /**
     * Converts a .values file to a Weka Instances object.
     *
     * @param file the .values file to convert
     * @param dataset the dataset to use as a template (for attribute structure)
     * @return a Weka Instances object containing the data from the .values file
     * @throws IOException if an I/O error occurs
     */
    public static Instances convertValuesFileToInstance(File file, Instances dataset) throws IOException {
        // Parse the .values file
        Map<String, Double> featureValues = parseValuesFile(file);

        // Create a new dataset with the same structure as the template
        Instances newDataset = new Instances(dataset, 0);

        // Create instance
        double[] values = new double[newDataset.numAttributes()];

        // Fill in the values
        for (int i = 0; i < newDataset.numAttributes(); i++) {
            Attribute attr = newDataset.attribute(i);
            if (attr.isNumeric()) {
                String attrName = attr.name();
                Double value = featureValues.get(attrName);
                if (value != null) {
                    values[i] = value;
                } else {
                    // Use missing value if not specified
                    values[i] = weka.core.Utils.missingValue();
                    System.out.println("Warning: Feature '" + attrName + "' not found in values file. Using missing value.");
                }
            } else if (attr.isNominal()) {
                // For nominal attributes, we need to handle differently
                // This is simplified - in a real implementation, you would need to map
                // the value to the correct index in the nominal values list
                String attrName = attr.name();
                Double value = featureValues.get(attrName);
                if (value != null) {
                    int index = value.intValue();
                    if (index >= 0 && index < attr.numValues()) {
                        values[i] = index;
                    } else {
                        values[i] = weka.core.Utils.missingValue();
                        System.out.println("Warning: Invalid value for nominal attribute '" + attrName + "'. Using missing value.");
                    }
                } else {
                    values[i] = weka.core.Utils.missingValue();
                    System.out.println("Warning: Feature '" + attrName + "' not found in values file. Using missing value.");
                }
            }
        }

        // Add instance to dataset
        DenseInstance instance = new DenseInstance(1.0, values);
        instance.setDataset(newDataset);
        newDataset.add(instance);

        return newDataset;
    }
}
