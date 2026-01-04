package com.github.shyamjoser.fileparser.textual;

import java.util.*;

/**
 * Represents the configuration for a specific record type.
 * Contains the list of fields that make up this record type.
 */
public class RecordTypeConfig {

    private String name;
    private List<FieldConfig> fields;

    /**
     * Default constructor.
     */
    public RecordTypeConfig() {
        this.fields = new ArrayList<>();
    }

    /**
     * Constructor with record type name.
     *
     * @param name the name of the record type
     */
    public RecordTypeConfig(String name) {
        this();
        this.name = name;
    }

    /**
     * Gets the record type name.
     *
     * @return the record type name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the record type name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the list of fields in this record type.
     *
     * @return list of FieldConfig objects
     */
    public List<FieldConfig> getFields() {
        return fields;
    }

    /**
     * Sets the list of fields for this record type.
     *
     * @param fields the list of fields to set
     */
    public void setFields(List<FieldConfig> fields) {
        this.fields = fields != null ? fields : new ArrayList<>();
    }

    /**
     * Adds a field configuration to this record type.
     *
     * @param field the field configuration to add
     * @return this RecordTypeConfig for method chaining
     */
    public RecordTypeConfig addField(FieldConfig field) {
        field.setPosition(fields.size());
        fields.add(field);
        return this;
    }

    /**
     * Gets a field configuration by name.
     *
     * @param fieldName the name of the field
     * @return the FieldConfig if found, null otherwise
     */
    public FieldConfig getField(String fieldName) {
        return fields.stream()
                .filter(f -> f.getName().equals(fieldName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the total length required for records of this type.
     *
     * @return the sum of all field lengths
     */
    public int getTotalLength() {
        return fields.stream().mapToInt(FieldConfig::getLength).sum();
    }

    /**
     * Gets the number of fields in this record type.
     *
     * @return the number of fields
     */
    public int getFieldCount() {
        return fields.size();
    }

    /**
     * Validates whether a raw record string has the correct length.
     *
     * @param rawRecord the raw record string
     * @return true if the length matches, false otherwise
     */
    public boolean hasValidLength(String rawRecord) {
        return rawRecord != null && rawRecord.length() == getTotalLength();
    }

    @Override
    public String toString() {
        return "RecordTypeConfig{" +
                "name='" + name + '\'' +
                ", fieldCount=" + fields.size() +
                ", totalLength=" + getTotalLength() +
                '}';
    }
}

