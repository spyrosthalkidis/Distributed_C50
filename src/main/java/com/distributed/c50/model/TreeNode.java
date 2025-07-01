package com.distributed.c50.model;

import java.io.Serializable;

public class TreeNode implements Serializable {
    private static final long serialVersionUID = 1L;

    // Fields for splitting
    private int attributeIndex;
    private String attributeName;
    private String splitAttribute;     // Feature name used to split
    private boolean isNumericSplit;    // true if numeric split
    private double splitThreshold;     // threshold value for numeric split

    // Children for numeric and nominal splits
    private TreeNode leftChild;        // numeric: < threshold
    private TreeNode rightChild;       // numeric: >= threshold
    private TreeNode[] children;       // nominal splits
    private TreeNode defaultChild;     // fallback child if feature missing

    // Leaf properties
    private boolean isLeaf;
    private String classLabel;
    private int[] classDistribution;

    public TreeNode() {}

    // Leaf flag
    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    // Class label
    public String getClassLabel() {
        return classLabel;
    }

    public void setClassLabel(String classLabel) {
        this.classLabel = classLabel;
    }

    // Class distribution
    public int[] getClassDistribution() {
        return classDistribution;
    }

    public void setClassDistribution(int[] classDistribution) {
        this.classDistribution = classDistribution;
    }

    // Attribute index and name
    public int getAttributeIndex() {
        return attributeIndex;
    }

    public void setAttributeIndex(int attributeIndex) {
        this.attributeIndex = attributeIndex;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    // Split attribute (feature name)
    public String getSplitAttribute() {
        return splitAttribute;
    }

    public void setSplitAttribute(String splitAttribute) {
        this.splitAttribute = splitAttribute;
    }

    // Numeric split flag
    public boolean isNumericSplit() {
        return isNumericSplit;
    }

    public void setNumericSplit(boolean isNumericSplit) {
        this.isNumericSplit = isNumericSplit;
    }

    // Numeric split threshold
    public double getSplitThreshold() {
        return splitThreshold;
    }

    public void setSplitThreshold(double splitThreshold) {
        this.splitThreshold = splitThreshold;
    }

    // Left and right children for numeric split
    public TreeNode getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(TreeNode leftChild) {
        this.leftChild = leftChild;
    }

    public TreeNode getRightChild() {
        return rightChild;
    }

    public void setRightChild(TreeNode rightChild) {
        this.rightChild = rightChild;
    }

    // Children for nominal splits
    public TreeNode[] getChildren() {
        return children;
    }

    public void setChildren(TreeNode[] children) {
        this.children = children;
    }

    public TreeNode getChild(int index) {
        if (children != null && index >= 0 && index < children.length) {
            return children[index];
        }
        return null;
    }

    // Default child
    public TreeNode getDefaultChild() {
        return defaultChild;
    }

    public void setDefaultChild(TreeNode defaultChild) {
        this.defaultChild = defaultChild;
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "attributeIndex=" + attributeIndex +
                ", attributeName='" + attributeName + '\'' +
                ", splitAttribute='" + splitAttribute + '\'' +
                ", isNumericSplit=" + isNumericSplit +
                ", splitThreshold=" + splitThreshold +
                ", isLeaf=" + isLeaf +
                ", classLabel='" + classLabel + '\'' +
                '}';
    }
}

