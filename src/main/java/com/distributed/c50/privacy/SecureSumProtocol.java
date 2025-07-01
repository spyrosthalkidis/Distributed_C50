package com.distributed.c50.privacy;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements secure sum protocol for privacy-preserving computation of sums
 * across multiple parties without revealing individual values.
 * Based on the approach described in the Vaidya and Clifton paper.
 */
public class SecureSumProtocol {
    private final SecureRandom random;
    private final int numParties;
    private final String nodeId;
    
    /**
     * Creates a new secure sum protocol instance.
     * 
     * @param nodeId the unique identifier of this node
     * @param numParties the number of participating parties
     */
    public SecureSumProtocol(String nodeId, int numParties) {
        this.nodeId = nodeId;
        this.numParties = numParties;
        this.random = new SecureRandom();
    }
    
    /**
     * Initiates a secure sum computation as the coordinator.
     * 
     * @param localValue the local value to include in the sum
     * @return a SecureSumState object containing the initial state
     */
    public SecureSumState initiateSecureSum(int localValue) {
        // Generate a random value to mask the actual sum
        BigInteger randomMask = new BigInteger(32, random);
        
        // Initialize the partial sum with local value + random mask
        BigInteger partialSum = BigInteger.valueOf(localValue).add(randomMask);
        
        return new SecureSumState(randomMask, partialSum, 1);
    }
    
    /**
     * Participates in a secure sum computation by adding the local value.
     * 
     * @param state the current state of the secure sum computation
     * @param localValue the local value to add
     * @return the updated state
     */
    public SecureSumState participateInSecureSum(SecureSumState state, int localValue) {
        // Add local value to the partial sum
        BigInteger updatedPartialSum = state.getPartialSum().add(BigInteger.valueOf(localValue));
        
        // Increment the round counter
        int nextRound = state.getCurrentRound() + 1;
        
        return new SecureSumState(state.getRandomMask(), updatedPartialSum, nextRound);
    }
    
    /**
     * Finalizes a secure sum computation as the coordinator.
     * 
     * @param state the final state of the secure sum computation
     * @return the actual sum of all values
     */
    public int finalizeSecureSum(SecureSumState state) {
        // Subtract the random mask to get the actual sum
        BigInteger actualSum = state.getPartialSum().subtract(state.getRandomMask());
        
        return actualSum.intValue();
    }
    
    /**
     * State object for the secure sum protocol.
     */
    public static class SecureSumState {
        private final BigInteger randomMask;
        private final BigInteger partialSum;
        private final int currentRound;
        
        /**
         * Creates a new secure sum state.
         * 
         * @param randomMask the random mask used to hide the actual sum
         * @param partialSum the current partial sum
         * @param currentRound the current round of the protocol
         */
        public SecureSumState(BigInteger randomMask, BigInteger partialSum, int currentRound) {
            this.randomMask = randomMask;
            this.partialSum = partialSum;
            this.currentRound = currentRound;
        }
        
        /**
         * Gets the random mask.
         * 
         * @return the random mask
         */
        public BigInteger getRandomMask() {
            return randomMask;
        }
        
        /**
         * Gets the current partial sum.
         * 
         * @return the partial sum
         */
        public BigInteger getPartialSum() {
            return partialSum;
        }
        
        /**
         * Gets the current round of the protocol.
         * 
         * @return the current round
         */
        public int getCurrentRound() {
            return currentRound;
        }
        
        /**
         * Checks if the protocol has completed all rounds.
         * 
         * @param numParties the total number of parties
         * @return true if all rounds are complete, false otherwise
         */
        public boolean isComplete(int numParties) {
            return currentRound > numParties;
        }
    }
    
    /**
     * Implements a secure sum computation for an array of values.
     * Each party contributes an array of values, and the result is the sum of each position.
     * 
     * @param localValues the local array of values
     * @param valueCount the number of values in the array
     * @return a list of SecureSumState objects, one for each value
     */
    public List<SecureSumState> initiateSecureSumArray(int[] localValues, int valueCount) {
        List<SecureSumState> states = new ArrayList<>(valueCount);
        
        for (int i = 0; i < valueCount; i++) {
            int value = (i < localValues.length) ? localValues[i] : 0;
            states.add(initiateSecureSum(value));
        }
        
        return states;
    }
    
    /**
     * Participates in a secure sum computation for an array of values.
     * 
     * @param states the current states of the secure sum computations
     * @param localValues the local array of values
     * @return the updated states
     */
    public List<SecureSumState> participateInSecureSumArray(List<SecureSumState> states, int[] localValues) {
        List<SecureSumState> updatedStates = new ArrayList<>(states.size());
        
        for (int i = 0; i < states.size(); i++) {
            int value = (i < localValues.length) ? localValues[i] : 0;
            updatedStates.add(participateInSecureSum(states.get(i), value));
        }
        
        return updatedStates;
    }
    
    /**
     * Finalizes a secure sum computation for an array of values.
     * 
     * @param states the final states of the secure sum computations
     * @return the array of actual sums
     */
    public int[] finalizeSecureSumArray(List<SecureSumState> states) {
        int[] sums = new int[states.size()];
        
        for (int i = 0; i < states.size(); i++) {
            sums[i] = finalizeSecureSum(states.get(i));
        }
        
        return sums;
    }
}
