#!/bin/bash

# Path to Weka jar (update this!)
WEKA_JAR="/home/spyros/distributed_c50_combined/weka.jar"

# JVM module system option (for Java 9+)
JVM_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED"

# Main class names
MAIN_CLASS="com.distributed.c50.DistributedC50CombinedMain"
TEST_CLASS="com.distributed.c50.DistributedC50Test"
VALUES_MAIN_CLASS="com.distributed.c50.DistributedC50ValuesMain"
VALUES_TEST_CLASS="com.distributed.c50.DistributedC50ValuesTest"
COORDINATOR_CLASS="com.distributed.c50.node.CoordinatorNode"
DATAPARTY_CLASS="com.distributed.c50.node.DataPartyNode"

# Build step: ./run_distributed_c50_combined.sh build
if [ "$1" == "build" ]; then
    echo "🛠️  Building project..."

    # Create target/classes directory
    mkdir -p target/classes

    # Compile Java sources
    javac -cp "$WEKA_JAR" -d target/classes         src/main/java/com/distributed/c50/*/*.java         src/main/java/com/distributed/c50/*.java

    if [ $? -eq 0 ]; then
        echo "✅ Build successful! Classes are in target/classes"
    else
        echo "❌ Build failed!"
    fi
    exit 0
fi

# Other commands
COMMAND=$1

case "$COMMAND" in
  predict)
    ARFF_FILE=$2
    VALUES_FILE=$3
    java $JVM_OPTS -cp "target/classes:$WEKA_JAR" $MAIN_CLASS predict "$ARFF_FILE" "$VALUES_FILE"
    ;;
  test)
    ARFF_FILE=$2
    java $JVM_OPTS -cp "target/classes:$WEKA_JAR" $TEST_CLASS "$ARFF_FILE"
    ;;
  coordinator)
    PORT=$2
    java $JVM_OPTS -cp "target/classes:$WEKA_JAR" $COORDINATOR_CLASS "$PORT"
    ;;
  dataparty)
    PARTY_NAME=$2
    PARTY_PORT=$3
    COORDINATOR_HOST=$4
    COORDINATOR_PORT=$5
    ARFF_FILE=$6
    java $JVM_OPTS -cp "target/classes:$WEKA_JAR" $DATAPARTY_CLASS "$PARTY_NAME" "$PARTY_PORT" "$COORDINATOR_HOST" "$COORDINATOR_PORT" "$ARFF_FILE"
    ;;
  *)
    echo "Usage:"
    echo "  $0 build                                      # Build the project"
    echo "  $0 predict <train.arff> <predict.values>       # Train & predict"
    echo "  $0 test <train.arff>                          # Test"
    echo "  $0 coordinator <port>                          # Start coordinator node"
    echo "  $0 dataparty <name> <port> <coord_host> <coord_port> <data.arff> # Start data party node"
    ;;
esac
