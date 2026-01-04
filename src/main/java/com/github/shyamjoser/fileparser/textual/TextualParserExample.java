package com.github.shyamjoser.fileparser.textual;

import java.io.*;
import java.util.List;

/**
 * Practical examples demonstrating how to use the TextualParser.
 */
public class TextualParserExample {

    public static void main(String[] args) {
        try {
            // Example 1: Basic single record parsing
            exampleParseingleRecord();

            // Example 2: Parsing from a file
            // exampleParseFile();

            // Example 3: Parsing with type detection
            // exampleParseWithTypeDetection();

            // Example 4: Validating records
            exampleValidateRecord();

            // Example 5: Working with TextualNode
            exampleWorkingWithNodes();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Example 1: Parse a single record
     */
    static void exampleParseingleRecord() throws IOException {
        System.out.println("========== Example 1: Parse Single Record ==========\n");

        // Create a test YAML configuration
        String yamlConfig = "sources:\n" +
                "  - source: source_1\n" +
                "    size: 40\n" +
                "    records:\n" +
                "      - type: record_1\n" +
                "        fields:\n" +
                "          - name: id\n" +
                "            length: 5\n" +
                "            type: INTEGER\n" +
                "            required: true\n" +
                "          - name: name\n" +
                "            length: 15\n" +
                "            type: STRING\n" +
                "            required: true\n" +
                "          - name: email\n" +
                "            length: 15\n" +
                "            type: STRING\n" +
                "          - name: status\n" +
                "            length: 1\n" +
                "            type: STRING\n" +
                "          - name: amount\n" +
                "            length: 9\n" +
                "            type: DECIMAL\n";

        // Initialize parser with YAML config
        TextualConfigLoader loader = new TextualConfigLoader();
        loader.loadFromString(yamlConfig);
        TextualParser parser = new TextualParser(loader);

        // Create sample record data
        String recordData = "00001John Smith    john@exam.comAY       1234.56789";

        // Parse the record
        TextualNode node = parser.parseRecord("source_1", "record_1", recordData);

        if (node != null) {
            System.out.println("Record parsed successfully!");
            System.out.println("Record Type: " + node.getRecordType());
            System.out.println("ID: " + node.getAsInt("id"));
            System.out.println("Name: " + node.getAsString("name"));
            System.out.println("Email: " + node.getAsString("email"));
            System.out.println("Status: " + node.getAsString("status"));
            System.out.println("Amount: " + node.getAsDouble("amount"));
        }

        System.out.println("\n");
    }

    /**
     * Example 2: Parse multiple records from a file
     */
    static void exampleParseFile() throws IOException {
        System.out.println("========== Example 2: Parse File ==========\n");

        // Initialize parser (assuming config file exists)
        // TextualParser parser = new TextualParser("config/textual-config.yaml");

        // String filePath = "data/records.txt";
        // List<TextualNode> records = parser.parseFile("source_1", "record_1", filePath);

        // System.out.println("Parsed " + records.size() + " records");
        // for (TextualNode record : records) {
        //     System.out.println("ID: " + record.getAsInt("id") +
        //                       " | Name: " + record.getAsString("name"));
        // }

        System.out.println("(Skipped in example - requires actual file)");
        System.out.println("\n");
    }

    /**
     * Example 3: Parse with automatic record type detection
     */
    static void exampleParseWithTypeDetection() throws IOException {
        System.out.println("========== Example 3: Parse with Type Detection ==========\n");

        // Initialize parser
        // TextualParser parser = new TextualParser("config/textual-config.yaml");

        // List<TextualNode> records = parser.parseFileWithTypeDetection(
        //     "source_1",
        //     "data/mixed_records.txt",
        //     line -> {
        //         // Auto-detect record type based on first 2 characters
        //         String typeCode = line.substring(0, 2);
        //         return typeCode.equals("01") ? "record_1" : "record_2";
        //     }
        // );

        // for (TextualNode record : records) {
        //     System.out.println("Type: " + record.getRecordType() + " | " +
        //                       record.getFieldNames());
        // }

        System.out.println("(Skipped in example - requires actual file)");
        System.out.println("\n");
    }

    /**
     * Example 4: Validate records before parsing
     */
    static void exampleValidateRecord() throws IOException {
        System.out.println("========== Example 4: Validate Records ==========\n");

        // Create a test YAML configuration
        String yamlConfig = "sources:\n" +
                "  - source: source_1\n" +
                "    size: 40\n" +
                "    records:\n" +
                "      - type: record_1\n" +
                "        fields:\n" +
                "          - name: id\n" +
                "            length: 5\n" +
                "            type: INTEGER\n" +
                "            required: true\n" +
                "          - name: name\n" +
                "            length: 15\n" +
                "            type: STRING\n" +
                "            required: true\n" +
                "          - name: email\n" +
                "            length: 15\n" +
                "            type: STRING\n" +
                "          - name: status\n" +
                "            length: 1\n" +
                "            type: STRING\n" +
                "          - name: amount\n" +
                "            length: 9\n" +
                "            type: DECIMAL\n";

        TextualConfigLoader loader = new TextualConfigLoader();
        loader.loadFromString(yamlConfig);
        TextualParser parser = new TextualParser(loader);

        // Valid record
        String validRecord = "00001John Smith    john@exam.comAY       1234.56789";

        // Invalid record (too short)
        String invalidRecord = "Invalid";

        System.out.println("Validating records...");

        if (parser.validateRecord("source_1", "record_1", validRecord)) {
            System.out.println("✓ Valid record passes validation");
        } else {
            System.out.println("✗ Valid record failed validation");
        }

        if (!parser.validateRecord("source_1", "record_1", invalidRecord)) {
            System.out.println("✓ Invalid record correctly rejected");
        } else {
            System.out.println("✗ Invalid record incorrectly accepted");
        }

        System.out.println("\n");
    }

    /**
     * Example 5: Working with TextualNode
     */
    static void exampleWorkingWithNodes() throws IOException {
        System.out.println("========== Example 5: Working with TextualNode ==========\n");

        // Create a test YAML configuration
        String yamlConfig = "sources:\n" +
                "  - source: source_1\n" +
                "    size: 40\n" +
                "    records:\n" +
                "      - type: record_1\n" +
                "        fields:\n" +
                "          - name: id\n" +
                "            length: 5\n" +
                "            type: INTEGER\n" +
                "            required: true\n" +
                "          - name: name\n" +
                "            length: 15\n" +
                "            type: STRING\n" +
                "            required: true\n" +
                "          - name: email\n" +
                "            length: 15\n" +
                "            type: STRING\n" +
                "          - name: status\n" +
                "            length: 1\n" +
                "            type: STRING\n" +
                "          - name: amount\n" +
                "            length: 9\n" +
                "            type: DECIMAL\n";

        TextualConfigLoader loader = new TextualConfigLoader();
        loader.loadFromString(yamlConfig);
        TextualParser parser = new TextualParser(loader);

        String recordData = "00001John Smith    john@exam.comAY       1234.56789";
        TextualNode node = parser.parseRecord("source_1", "record_1", recordData);

        if (node != null) {
            // Access individual fields
            System.out.println("--- Access Individual Fields ---");
            System.out.println("ID (as int): " + node.getAsInt("id"));
            System.out.println("Name (as string): " + node.getAsString("name").trim());
            System.out.println("Amount (as double): " + node.getAsDouble("amount"));
            System.out.println();

            // Check field existence
            System.out.println("--- Check Field Existence ---");
            System.out.println("Has 'name' field: " + node.has("name"));
            System.out.println("Has 'unknown' field: " + node.has("unknown"));
            System.out.println();

            // Get all field information
            System.out.println("--- All Field Information ---");
            System.out.println("Record Type: " + node.getRecordType());
            System.out.println("Number of Fields: " + node.size());
            System.out.println("Field Names: " + node.getFieldNames());
            System.out.println();

            // Iterate through all fields
            System.out.println("--- Iterate Through All Fields ---");
            for (String fieldName : node.getFieldNames()) {
                Object value = node.get(fieldName);
                System.out.println(fieldName + " = " + value);
            }
            System.out.println();

            // Get formatted output
            System.out.println("--- Formatted Output ---");
            System.out.println(node.toFormattedString());
        }
    }
}

