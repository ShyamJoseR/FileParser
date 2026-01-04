package com.github.shyamjoser.fileparser.textual;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Main parser for textual fixed-width data files.
 * Parses records according to YAML-defined schemas and returns TextualNode objects.
 */
public class TextualParser {

    private static final Logger logger = LoggerFactory.getLogger(TextualParser.class);
    private TextualConfigLoader configLoader;

    /**
     * Creates a TextualParser with a configuration file.
     *
     * @param configFilePath the path to the YAML configuration file
     * @throws IOException if the configuration file cannot be loaded
     */
    public TextualParser(String configFilePath) throws IOException {
        this.configLoader = new TextualConfigLoader();
        this.configLoader.loadFromYaml(configFilePath);
        logger.info("TextualParser initialized with configuration from: {}", configFilePath);
    }

    /**
     * Creates a TextualParser with a pre-loaded configuration loader.
     *
     * @param configLoader the TextualConfigLoader instance
     */
    public TextualParser(TextualConfigLoader configLoader) {
        this.configLoader = configLoader;
        logger.info("TextualParser initialized with provided configuration loader");
    }

    /**
     * Parses a single record string according to the specified record type.
     *
     * @param source the source name
     * @param recordType the record type name
     * @param recordData the raw record string data
     * @return a TextualNode containing parsed fields, or null if parsing fails
     */
    public TextualNode parseRecord(String source, String recordType, String recordData) {
        if (recordData == null || recordData.isEmpty()) {
            logger.warn("Empty record data provided");
            return null;
        }

        SourceConfig sourceConfig = configLoader.getSourceConfig(source);
        if (sourceConfig == null) {
            logger.error("Source configuration not found: {}", source);
            return null;
        }

        RecordTypeConfig recordTypeConfig = sourceConfig.getRecordType(recordType);
        if (recordTypeConfig == null) {
            logger.error("Record type configuration not found: {} for source: {}", recordType, source);
            return null;
        }

        return parseRecordInternal(recordData, recordTypeConfig);
    }

    /**
     * Internal method to parse a record using the configuration.
     *
     * @param recordData the raw record string
     * @param recordTypeConfig the record type configuration
     * @return the parsed TextualNode
     */
    private TextualNode parseRecordInternal(String recordData, RecordTypeConfig recordTypeConfig) {
        TextualNode node = new TextualNode(recordTypeConfig.getName());

        int position = 0;
        for (FieldConfig fieldConfig : recordTypeConfig.getFields()) {
            int length = fieldConfig.getLength();

            // Extract field data
            if (position + length > recordData.length()) {
                logger.warn("Record data is shorter than expected. Expected at least {} characters, got {}",
                        position + length, recordData.length());
                String fieldValue = recordData.substring(position).trim();
                if (!fieldValue.isEmpty()) {
                    position = recordData.length();
                } else {
                    break;
                }
            } else {
                String fieldValue = recordData.substring(position, position + length);
                position += length;

                // Validate field
                if (!fieldConfig.validate(fieldValue)) {
                    logger.warn("Field {} failed validation. Value: '{}'", fieldConfig.getName(), fieldValue);
                }

                // Parse and add field to node
                Object parsedValue = fieldConfig.parseValue(fieldValue);
                node.put(fieldConfig.getName(), parsedValue);
            }
        }

        return node;
    }

    /**
     * Parses multiple records from a file.
     *
     * @param source the source name
     * @param recordType the record type name
     * @param filePath the path to the file containing records
     * @return a list of parsed TextualNode objects
     * @throws IOException if the file cannot be read
     */
    public List<TextualNode> parseFile(String source, String recordType, String filePath) throws IOException {
        List<TextualNode> nodes = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            TextualNode node = parseRecord(source, recordType, line);
            if (node != null) {
                nodes.add(node);
            }
        }

        logger.info("Parsed {} records from file: {}", nodes.size(), filePath);
        return nodes;
    }

    /**
     * Parses records from a BufferedReader.
     *
     * @param source the source name
     * @param recordType the record type name
     * @param reader the BufferedReader containing records
     * @return a list of parsed TextualNode objects
     * @throws IOException if reading fails
     */
    public List<TextualNode> parseFromReader(String source, String recordType, BufferedReader reader) throws IOException {
        List<TextualNode> nodes = new ArrayList<>();
        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (line.trim().isEmpty()) {
                continue;
            }

            TextualNode node = parseRecord(source, recordType, line);
            if (node != null) {
                nodes.add(node);
            } else {
                logger.warn("Failed to parse record at line {}", lineNumber);
            }
        }

        logger.info("Parsed {} records from reader", nodes.size());
        return nodes;
    }

    /**
     * Parses multiple record types from a file.
     * Uses a function to determine the record type for each line.
     *
     * @param source the source name
     * @param filePath the path to the file
     * @param recordTypeDetector a function that determines record type from line data
     * @return a list of parsed TextualNode objects
     * @throws IOException if the file cannot be read
     */
    public List<TextualNode> parseFileWithTypeDetection(String source, String filePath,
                                                         RecordTypeDetector recordTypeDetector) throws IOException {
        List<TextualNode> nodes = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }

            String recordType = recordTypeDetector.detectType(line);
            if (recordType != null) {
                TextualNode node = parseRecord(source, recordType, line);
                if (node != null) {
                    nodes.add(node);
                }
            } else {
                logger.warn("Could not determine record type for line: {}", line);
            }
        }

        logger.info("Parsed {} records from file with type detection: {}", nodes.size(), filePath);
        return nodes;
    }

    /**
     * Gets the configuration loader.
     *
     * @return the TextualConfigLoader instance
     */
    public TextualConfigLoader getConfigLoader() {
        return configLoader;
    }

    /**
     * Validates a record against its schema.
     *
     * @param source the source name
     * @param recordType the record type name
     * @param recordData the raw record string
     * @return true if the record is valid, false otherwise
     */
    public boolean validateRecord(String source, String recordType, String recordData) {
        SourceConfig sourceConfig = configLoader.getSourceConfig(source);
        if (sourceConfig == null) {
            return false;
        }

        RecordTypeConfig recordTypeConfig = sourceConfig.getRecordType(recordType);
        if (recordTypeConfig == null) {
            return false;
        }

        // Check if record length is valid
        if (!recordTypeConfig.hasValidLength(recordData)) {
            logger.warn("Record length mismatch for type {}. Expected {}, got {}",
                    recordType, recordTypeConfig.getTotalLength(), recordData.length());
            return false;
        }

        // Validate each field
        int position = 0;
        for (FieldConfig fieldConfig : recordTypeConfig.getFields()) {
            int length = fieldConfig.getLength();
            String fieldValue = recordData.substring(position, Math.min(position + length, recordData.length()));
            position += length;

            if (!fieldConfig.validate(fieldValue)) {
                logger.warn("Field {} validation failed for value: '{}'", fieldConfig.getName(), fieldValue);
                return false;
            }
        }

        return true;
    }

    /**
     * Functional interface for detecting record type from line data.
     */
    @FunctionalInterface
    public interface RecordTypeDetector {
        /**
         * Detects the record type from the given line data.
         *
         * @param lineData the raw line data
         * @return the record type name, or null if unable to determine
         */
        String detectType(String lineData);
    }
}

