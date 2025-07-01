## Distributed C5.0 Algorithm with Combined ARFF Training and Values Prediction

This project implements a distributed version of the C5.0 decision tree algorithm for vertically partitioned data, based on the privacy-preserving approach described in the Vaidya and Clifton paper. The implementation now supports both ARFF files for training and the .values format for prediction.

### Features

- Privacy-preserving distributed computation
- Support for vertically partitioned data
- Java socket-based communication
- ARFF file handling with Weka integration
- Support for .values file format for prediction
- Combined workflow: Train with ARFF, predict with .values

### File Formats

#### ARFF File Format (for Training)
The ARFF (Attribute-Relation File Format) is the standard Weka format for datasets, containing header information and data instances.

#### .values File Format (for Prediction)
The .values file format is a simple key-value format where:
- Each line contains a feature name and its value, separated by a tab
- Example:
  ```
  petalwidth	1.0
  petallength	5
  ```

### Directory Structure

```
distributed_c50_combined/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── distributed/
│                   └── c50/
│                       ├── model/
│                       │   ├── TreeNode.java
│                       │   └── AttributeMetadata.java
│                       ├── common/
│                       │   └── Constants.java
│                       ├── communication/
│                       │   ├── Message.java
│                       │   ├── MessageHandler.java
│                       │   ├── InitiationMessage.java
│                       │   └── SecureSocketManager.java
│                       ├── privacy/
│                       │   ├── SecureSumProtocol.java
│                       │   └── SecureInformationGainProtocol.java
│                       ├── core/
│                       │   └── DistributedC50Core.java
│                       ├── arff/
│                       │   └── ARFFHandler.java
│                       ├── parser/
│                       │   └── ValuesFileParser.java
│                       ├── prediction/
│                       │   └── TreeTraversal.java
│                       ├── node/
│                       │   ├── CoordinatorNode.java
│                       │   └── DataPartyNode.java
│                       ├── DistributedC50Main.java
│                       ├── DistributedC50Test.java
│                       ├── DistributedC50ValuesMain.java
│                       ├── DistributedC50ValuesTest.java
│                       └── DistributedC50CombinedMain.java
└── run_distributed_c50_combined.sh
```

### Usage

1. Edit the `run_distributed_c50_combined.sh` script to set the path to your Weka JAR file.
2. Make the script executable:
   ```bash
   chmod +x run_distributed_c50_combined.sh
   ```
3. Compile the code:
   ```bash
   mkdir -p target/classes
   javac -cp /path/to/weka.jar -d target/classes src/main/java/com/distributed/c50/*/*.java src/main/java/com/distributed/c50/*.java
   ```
4. Run the program using the script:
   ```bash
   # To train with an ARFF file and predict with a values file
   ./run_distributed_c50_combined.sh predict iris.arff iris.values
   
   # To run a test with an ARFF file only
   ./run_distributed_c50_combined.sh test iris.arff
   
   # To run a coordinator node
   ./run_distributed_c50_combined.sh coordinator 9000
   
   # To run a data party node
   ./run_distributed_c50_combined.sh dataparty party1 9001 localhost 9000 data.arff
   ```

### Requirements

- Java Development Kit (JDK) 8 or higher
- Weka library (weka.jar)

### Note on Java Module System

If you're using Java 9 or higher, the script automatically adds the necessary `--add-opens` argument to handle Java module system restrictions.

### Example Workflow

1. Prepare your ARFF file for training (e.g., iris.arff)
2. Prepare your values file for prediction (e.g., iris.values)
3. Run the prediction command:
   ```bash
   ./run_distributed_c50_combined.sh predict iris.arff iris.values
   ```
4. The system will:
   - Train a C5.0 decision tree using the ARFF file
   - Use the trained tree to make a prediction based on the values file
   - Output the final prediction/classification
