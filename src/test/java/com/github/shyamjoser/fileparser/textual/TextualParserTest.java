package com.github.shyamjoser.fileparser.textual;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for the TextualParser and related components.
 */
public class TextualParserTest {

    private TextualConfigLoader configLoader;
    private TextualParser parser;
    private String yamlConfig;

    @Before
    public void setUp() {
        // Create a test YAML configuration
        yamlConfig = "sources:\n" +
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
                "            type: DECIMAL\n" +
                "      - type: record_2\n" +
                "        fields:\n" +
                "          - name: type_code\n" +
                "            length: 2\n" +
                "            type: STRING\n" +
                "            required: true\n" +
                "          - name: field_1\n" +
                "            length: 5\n" +
                "            type: STRING\n" +
                "          - name: field_2\n" +
                "            length: 5\n" +
                "            type: STRING\n" +
                "          - name: field_3\n" +
                "            length: 5\n" +
                "            type: STRING\n" +
                "          - name: field_4\n" +
                "            length: 5\n" +
                "            type: STRING\n" +
                "          - name: field_5\n" +
                "            length: 5\n" +
                "            type: STRING\n" +
                "          - name: field_6\n" +
                "            length: 3\n" +
                "            type: INTEGER\n" +
                "          - name: field_7\n" +
                "            length: 5\n" +
                "            type: DECIMAL";

        configLoader = new TextualConfigLoader();
        configLoader.loadFromString(yamlConfig);
        parser = new TextualParser(configLoader);
    }

    @Test
    public void testConfigLoaderLoadFromString() {
        assertTrue("Configuration should be loaded", configLoader.hasSource("source_1"));
        assertEquals("Should have one source", 1, configLoader.getSourceCount());
    }

    @Test
    public void testSourceConfigLoading() {
        SourceConfig source = configLoader.getSourceConfig("source_1");
        assertNotNull("Source should be found", source);
        assertEquals("Source name should match", "source_1", source.getSource());
        assertEquals("Source size should be 40", 40, source.getSize());
        assertEquals("Should have 2 record types", 2, source.getRecordTypeCount());
    }

    @Test
    public void testRecordTypeConfigLoading() {
        SourceConfig source = configLoader.getSourceConfig("source_1");
        RecordTypeConfig record = source.getRecordType("record_1");

        assertNotNull("Record type should be found", record);
        assertEquals("Record type name should match", "record_1", record.getName());
        assertEquals("Should have 5 fields", 5, record.getFieldCount());
        assertEquals("Total length should be 40", 40, record.getTotalLength());
    }

    @Test
    public void testFieldConfigLoading() {
        SourceConfig source = configLoader.getSourceConfig("source_1");
        RecordTypeConfig record = source.getRecordType("record_1");
        FieldConfig field = record.getField("id");

        assertNotNull("Field should be found", field);
        assertEquals("Field name should match", "id", field.getName());
        assertEquals("Field length should be 5", 5, field.getLength());
        assertEquals("Field type should be INTEGER", FieldConfig.FieldType.INTEGER, field.getType());
        assertTrue("Field should be required", field.isRequired());
    }

    @Test
    public void testParseRecord() {
        String recordData = "00001John       john@exam.comAY       1234.56789";
        TextualNode node = parser.parseRecord("source_1", "record_1", recordData);

        assertNotNull("Node should be parsed", node);
        assertEquals("Record type should match", "record_1", node.getRecordType());
        assertEquals("ID should be 1", 1, node.getAsInt("id"));
        assertEquals("Name should match", "John       ", node.getAsString("name").substring(0, 10));
    }

    @Test
    public void testTextualNodeFieldAccess() {
        String recordData = "00001John       john@exam.comAY       1234.56789";
        TextualNode node = parser.parseRecord("source_1", "record_1", recordData);

        assertTrue("Node should have id field", node.has("id"));
        assertTrue("Node should have name field", node.has("name"));
        assertFalse("Node should not have unknown field", node.has("unknown"));

        assertEquals("Should have 5 fields", 5, node.size());
        assertFalse("Node should not be empty", node.isEmpty());
    }

    @Test
    public void testTextualNodeFieldNames() {
        String recordData = "00001John       john@exam.comAY       1234.56789";
        TextualNode node = parser.parseRecord("source_1", "record_1", recordData);

        List<String> fieldNames = node.getFieldNames();
        assertEquals("Should have 5 field names", 5, fieldNames.size());
        assertEquals("First field should be id", "id", fieldNames.get(0));
        assertEquals("Second field should be name", "name", fieldNames.get(1));
    }

    @Test
    public void testIntegerFieldParsing() {
        String recordData = "00001John       john@exam.comAY       1234.56789";
        TextualNode node = parser.parseRecord("source_1", "record_1", recordData);

        int id = node.getAsInt("id");
        assertEquals("ID should be parsed as integer", 1, id);
    }

    @Test
    public void testDecimalFieldParsing() {
        String recordData = "00001John       john@exam.comAY       1234.56789";
        TextualNode node = parser.parseRecord("source_1", "record_1", recordData);

        double amount = node.getAsDouble("amount");
        assertEquals("Amount should be parsed as double", 1234.56789, amount, 0.00001);
    }

    @Test
    public void testStringFieldParsing() {
        String recordData = "00001John       john@exam.comAY       1234.56789";
        TextualNode node = parser.parseRecord("source_1", "record_1", recordData);

        String name = node.getAsString("name");
        assertNotNull("Name should not be null", name);
        assertTrue("Name should contain John", name.contains("John"));
    }

    @Test
    public void testValidateRecord() {
        String recordData = "00001John       john@exam.comAY       1234.56789";
        boolean isValid = parser.validateRecord("source_1", "record_1", recordData);
        assertTrue("Record should be valid", isValid);
    }

    @Test
    public void testValidateInvalidRecord() {
        String recordData = "InvalidData"; // Too short
        boolean isValid = parser.validateRecord("source_1", "record_1", recordData);
        assertFalse("Record should be invalid", isValid);
    }

    @Test
    public void testParseMultipleRecords() throws Exception {
        String data = "00001John       john@exam.comAY       1234.56789\n" +
                     "00002Jane       jane@exam.comNY       5678.90123";

        BufferedReader reader = new BufferedReader(new StringReader(data));
        List<TextualNode> records = parser.parseFromReader("source_1", "record_1", reader);

        assertEquals("Should parse 2 records", 2, records.size());
        assertEquals("First record ID should be 1", 1, records.get(0).getAsInt("id"));
        assertEquals("Second record ID should be 2", 2, records.get(1).getAsInt("id"));
    }

    @Test
    public void testFieldValidation() {
        SourceConfig source = configLoader.getSourceConfig("source_1");
        RecordTypeConfig record = source.getRecordType("record_1");
        FieldConfig idField = record.getField("id");

        assertTrue("Valid integer should pass", idField.validate("12345"));
        assertTrue("Valid integer string should pass", idField.validate("00001"));
    }

    @Test
    public void testTextualNodeToString() {
        String recordData = "00001John       john@exam.comAY       1234.56789";
        TextualNode node = parser.parseRecord("source_1", "record_1", recordData);

        String toString = node.toString();
        assertNotNull("toString should not be null", toString);
        assertTrue("toString should contain record type", toString.contains("record_1"));
    }

    @Test
    public void testTextualNodeFormattedString() {
        String recordData = "00001John       john@exam.comAY       1234.56789";
        TextualNode node = parser.parseRecord("source_1", "record_1", recordData);

        String formatted = node.toFormattedString();
        assertNotNull("Formatted string should not be null", formatted);
        assertTrue("Should contain record type", formatted.contains("Record Type"));
        assertTrue("Should contain field names", formatted.contains("id:"));
    }

    @Test
    public void testRecord2Parsing() {
        String recordData = "01Field1Field2Field3Field4Field5123 12.34";
        TextualNode node = parser.parseRecord("source_1", "record_2", recordData);

        assertNotNull("Node should be parsed", node);
        assertEquals("Record type should be record_2", "record_2", node.getRecordType());
        assertEquals("Type code should be 01", "01", node.getAsString("type_code"));
    }

    @Test
    public void testNullRecordHandling() {
        TextualNode node = parser.parseRecord("source_1", "record_1", null);
        assertNull("Null record should return null", node);
    }

    @Test
    public void testNonExistentSourceHandling() {
        TextualNode node = parser.parseRecord("non_existent", "record_1", "somedata");
        assertNull("Non-existent source should return null", node);
    }

    @Test
    public void testNonExistentRecordTypeHandling() {
        String recordData = "00001John       john@exam.comAY       1234.56789";
        TextualNode node = parser.parseRecord("source_1", "non_existent_type", recordData);
        assertNull("Non-existent record type should return null", node);
    }

    @Test
    public void testGetConfigLoader() {
        TextualConfigLoader loader = parser.getConfigLoader();
        assertNotNull("Config loader should not be null", loader);
        assertTrue("Loader should have source_1", loader.hasSource("source_1"));
    }

    @Test
    public void testTextualNodeGetFields() {
        String recordData = "00001John       john@exam.comAY       1234.56789";
        TextualNode node = parser.parseRecord("source_1", "record_1", recordData);

        Map<String, Object> fields = node.getFields();
        assertNotNull("Fields map should not be null", fields);
        assertEquals("Should have 5 fields", 5, fields.size());
        assertTrue("Should contain id field", fields.containsKey("id"));
    }

    @Test
    public void testBooleanFieldParsing() {
        String yamlWithBoolean = yamlConfig.replace(
                "          - name: status\n" +
                "            length: 1\n" +
                "            type: STRING",
                "          - name: status\n" +
                "            length: 1\n" +
                "            type: BOOLEAN"
        );

        TextualConfigLoader loader = new TextualConfigLoader();
        loader.loadFromString(yamlWithBoolean);
        TextualParser testParser = new TextualParser(loader);

        String recordData = "00001John       john@exam.comTY       1234.56789";
        TextualNode node = testParser.parseRecord("source_1", "record_1", recordData);

        boolean status = node.getAsBoolean("status");
        assertTrue("Status should be true", status);
    }

    @Test
    public void testGetAsStringWithNull() {
        TextualNode node = new TextualNode("test");
        String value = node.getAsString("nonexistent");
        assertEquals("Should return empty string for null", "", value);
    }

    @Test
    public void testGetAsIntWithInvalidValue() {
        TextualNode node = new TextualNode("test");
        node.put("notanumber", "abc");
        int value = node.getAsInt("notanumber");
        assertEquals("Should return 0 for invalid number", 0, value);
    }

    @Test
    public void testRecordTypeConfigTotalLength() {
        SourceConfig source = configLoader.getSourceConfig("source_1");
        RecordTypeConfig record = source.getRecordType("record_1");

        int totalLength = record.getTotalLength();
        assertEquals("Total length should be 40", 40, totalLength);
    }
}

