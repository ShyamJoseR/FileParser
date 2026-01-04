package com.github.shyamjoser.fileparser.textual;

import java.util.*;

/**
 * Represents the configuration for a data source.
 * Contains source name, maximum record length, and multiple record types.
 */
public class SourceConfig {

    private String source;
    private int size;
    private Map<String, RecordTypeConfig> records;

    /**
     * Default constructor.
     */
    public SourceConfig() {
        this.records = new LinkedHashMap<>();
    }

    /**
     * Constructor with source name and size.
     *
     * @param source the source name
     * @param size the maximum record size in characters
     */
    public SourceConfig(String source, int size) {
        this();
        this.source = source;
        this.size = size;
    }

    /**
     * Gets the source name.
     *
     * @return the source name
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the source name.
     *
     * @param source the source name to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Gets the maximum record size for this source.
     *
     * @return the size in characters
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the maximum record size for this source.
     *
     * @param size the size in characters
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Gets all record type configurations for this source.
     *
     * @return map of record type name to RecordTypeConfig
     */
    public Map<String, RecordTypeConfig> getRecords() {
        return records;
    }

    /**
     * Sets all record type configurations for this source.
     *
     * @param records the map of record configurations
     */
    public void setRecords(Map<String, RecordTypeConfig> records) {
        this.records = records != null ? records : new LinkedHashMap<>();
    }

    /**
     * Adds a record type configuration to this source.
     *
     * @param recordType the record type name
     * @param config the RecordTypeConfig
     * @return this SourceConfig for method chaining
     */
    public SourceConfig addRecordType(String recordType, RecordTypeConfig config) {
        records.put(recordType, config);
        return this;
    }

    /**
     * Gets a record type configuration by name.
     *
     * @param recordType the name of the record type
     * @return the RecordTypeConfig if found, null otherwise
     */
    public RecordTypeConfig getRecordType(String recordType) {
        return records.get(recordType);
    }

    /**
     * Checks if a record type exists in this source.
     *
     * @param recordType the record type name
     * @return true if the record type exists, false otherwise
     */
    public boolean hasRecordType(String recordType) {
        return records.containsKey(recordType);
    }

    /**
     * Gets all record type names in this source.
     *
     * @return list of record type names
     */
    public List<String> getRecordTypeNames() {
        return new ArrayList<>(records.keySet());
    }

    /**
     * Gets the number of record types in this source.
     *
     * @return the count of record types
     */
    public int getRecordTypeCount() {
        return records.size();
    }

    @Override
    public String toString() {
        return "SourceConfig{" +
                "source='" + source + '\'' +
                ", size=" + size +
                ", recordTypeCount=" + records.size() +
                '}';
    }
}

