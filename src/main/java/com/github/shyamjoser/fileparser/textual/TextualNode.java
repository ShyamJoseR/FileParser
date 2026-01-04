package com.github.shyamjoser.fileparser.textual;

import java.util.*;

/**
 * TextualNode represents a parsed record from textual data.
 * It provides a flexible structure to hold parsed fields and their values,
 * similar to JsonNode in JSON parsing libraries.
 */
public class TextualNode {

    private final Map<String, Object> fields;
    private final String recordType;
    private final List<String> fieldOrder;

    /**
     * Creates a new TextualNode with specified record type.
     *
     * @param recordType the type of record being parsed
     */
    public TextualNode(String recordType) {
        this.recordType = recordType;
        this.fields = new LinkedHashMap<>();
        this.fieldOrder = new ArrayList<>();
    }

    /**
     * Adds a field to the node with its parsed value.
     *
     * @param fieldName the name of the field
     * @param value the value of the field
     * @return this TextualNode for method chaining
     */
    public TextualNode put(String fieldName, Object value) {
        fields.put(fieldName, value);
        if (!fieldOrder.contains(fieldName)) {
            fieldOrder.add(fieldName);
        }
        return this;
    }

    /**
     * Retrieves a field value by name.
     *
     * @param fieldName the name of the field
     * @return the value of the field, or null if not present
     */
    public Object get(String fieldName) {
        return fields.get(fieldName);
    }

    /**
     * Retrieves a field value as a String.
     *
     * @param fieldName the name of the field
     * @return the value as String, or empty string if not present
     */
    public String getAsString(String fieldName) {
        Object value = fields.get(fieldName);
        return value != null ? value.toString() : "";
    }

    /**
     * Retrieves a field value as an Integer.
     *
     * @param fieldName the name of the field
     * @return the value as Integer, or 0 if not present or not numeric
     */
    public int getAsInt(String fieldName) {
        Object value = fields.get(fieldName);
        if (value == null) return 0;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Retrieves a field value as a Double.
     *
     * @param fieldName the name of the field
     * @return the value as Double, or 0.0 if not present or not numeric
     */
    public double getAsDouble(String fieldName) {
        Object value = fields.get(fieldName);
        if (value == null) return 0.0;
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Retrieves a field value as a Boolean.
     *
     * @param fieldName the name of the field
     * @return the value as Boolean, or false if not present
     */
    public boolean getAsBoolean(String fieldName) {
        Object value = fields.get(fieldName);
        if (value == null) return false;
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * Checks if a field exists in the node.
     *
     * @param fieldName the name of the field
     * @return true if the field exists, false otherwise
     */
    public boolean has(String fieldName) {
        return fields.containsKey(fieldName);
    }

    /**
     * Gets the record type of this node.
     *
     * @return the record type
     */
    public String getRecordType() {
        return recordType;
    }

    /**
     * Gets all field names in the order they were added.
     *
     * @return list of field names
     */
    public List<String> getFieldNames() {
        return new ArrayList<>(fieldOrder);
    }

    /**
     * Gets all fields as a map.
     *
     * @return unmodifiable map of all fields
     */
    public Map<String, Object> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    /**
     * Gets the number of fields in this node.
     *
     * @return the number of fields
     */
    public int size() {
        return fields.size();
    }

    /**
     * Checks if the node is empty.
     *
     * @return true if no fields are present, false otherwise
     */
    public boolean isEmpty() {
        return fields.isEmpty();
    }

    /**
     * Returns a string representation of the TextualNode.
     *
     * @return string representation in format: TextualNode{recordType='...', fields={...}}
     */
    @Override
    public String toString() {
        return "TextualNode{" +
                "recordType='" + recordType + '\'' +
                ", fields=" + fields +
                '}';
    }

    /**
     * Returns a formatted string with field names and values.
     *
     * @return formatted string representation
     */
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Record Type: ").append(recordType).append("\n");
        for (String fieldName : fieldOrder) {
            sb.append(fieldName).append(": ").append(fields.get(fieldName)).append("\n");
        }
        return sb.toString();
    }
}

