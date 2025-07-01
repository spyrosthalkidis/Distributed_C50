package com.distributed.c50.model;

import java.io.Serializable;

public class AttributeMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TYPE_NUMERIC = "numeric";
    public static final String TYPE_NOMINAL = "nominal";

    private String name;
    private String type; // "numeric" or "nominal"
    private String[] nominalValues;

    public AttributeMetadata(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public AttributeMetadata(String name, String type, String[] nominalValues) {
        this.name = name;
        this.type = type;
        this.nominalValues = nominalValues;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String[] getNominalValues() {
        return nominalValues;
    }

    /**
     * Returns number of values if nominal, else returns 0.
     */
    public int getNumValues() {
        return (nominalValues != null) ? nominalValues.length : 0;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNominalValues(String[] nominalValues) {
        this.nominalValues = nominalValues;
    }

    @Override
    public String toString() {
        return "AttributeMetadata{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", nominalValues=" + (nominalValues != null ? String.join(",", nominalValues) : "null") +
                '}';
    }
}

