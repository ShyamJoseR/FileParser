# FileParser

A comprehensive Java library for parsing textual, XML, JSON and other types of data files with support for multiple data sources and configurable record formats.

## Features

- **Textual Parser**: Parse fixed-width textual data with configurable schemas
- **YAML Configuration**: Define record types, field layouts, and validation rules in YAML files
- **TextualNode**: Flexible node-based data structure similar to JsonNode
- **Multiple Data Sources**: Support for different data sources with distinct record types
- **Field Validation**: Built-in regex, type, and required field validation
- **Type Conversion**: Automatic parsing of integers, decimals, booleans, and dates
- **Batch Processing**: Parse multiple records from files or streams
- **Record Type Detection**: Dynamic record type detection during parsing

## Project Structure

```
FileParser/
├── src/
│   ├── main/
│   │   ├── java/com/github/shyamjoser/fileparser/
│   │   │   ├── textual/              # Textual parser implementation
│   │   │   │   ├── TextualParser.java
│   │   │   │   ├── TextualNode.java
│   │   │   │   ├── TextualConfigLoader.java
│   │   │   │   ├── SourceConfig.java
│   │   │   │   ├── RecordTypeConfig.java
│   │   │   │   └── FieldConfig.java
│   │   │   ├── json/                 # JSON parser (future)
│   │   │   └── xml/                  # XML parser (future)
│   │   └── resources/
│   │       └── textual-config-example.yaml
│   └── test/
└── pom.xml
```

## Getting Started

### 1. Maven Dependencies

The project uses the following key dependencies:
- **SnakeYAML** (2.0): For YAML configuration parsing
- **SLF4J** (2.0.5): For logging
- **JUnit** (4.13.2): For testing

### 2. YAML Configuration Format

Define your data sources and record types in a YAML configuration file:

```yaml
sources:
  - source: source_1
    size: 40                    # Max record size in characters
    records:
      - type: record_1          # Record type name
        fields:
          - name: id            # Field name
            length: 5           # Field length in characters
            type: INTEGER       # Data type (STRING, INTEGER, DECIMAL, BOOLEAN, DATE)
            required: true      # Whether field is required
            regex: '^\d+$'      # Optional regex validation pattern
            default: '0'        # Optional default value
          - name: name
            length: 15
            type: STRING
            required: true
          - name: email
            length: 20
            type: STRING
            regex: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
```

### 3. Core Components

#### TextualNode
A flexible data structure similar to JsonNode that holds parsed record data:

```java
TextualNode node = parser.parseRecord("source_1", "record_1", recordData);

// Access fields
String id = node.getAsString("id");
int amount = node.getAsInt("amount");
double value = node.getAsDouble("value");
boolean active = node.getAsBoolean("status");

// Check field existence
if (node.has("email")) {
    String email = node.getAsString("email");
}

// Get all fields
Map<String, Object> allFields = node.getFields();
List<String> fieldNames = node.getFieldNames();

// String representation
System.out.println(node.toFormattedString());
```

#### TextualParser
Main parser for processing records:

```java
// Initialize parser with YAML config
TextualParser parser = new TextualParser("path/to/config.yaml");

// Parse a single record
TextualNode node = parser.parseRecord("source_1", "record_1", recordData);

// Parse entire file
List<TextualNode> records = parser.parseFile("source_1", "record_1", "path/to/file.txt");

// Parse with BufferedReader
try (BufferedReader reader = new BufferedReader(new FileReader("file.txt"))) {
    List<TextualNode> records = parser.parseFromReader("source_1", "record_1", reader);
}

// Parse with automatic record type detection
List<TextualNode> records = parser.parseFileWithTypeDetection(
    "source_1", 
    "path/to/file.txt",
    line -> {
        // Auto-detect record type based on line content
        String recordTypeCode = line.substring(0, 2);
        return recordTypeCode.equals("01") ? "record_1" : "record_2";
    }
);

// Validate a record
boolean isValid = parser.validateRecord("source_1", "record_1", recordData);
```

#### TextualConfigLoader
Loads and manages YAML configurations:

```java
TextualConfigLoader loader = new TextualConfigLoader();
loader.loadFromYaml("path/to/config.yaml");

// Get source configuration
SourceConfig sourceConfig = loader.getSourceConfig("source_1");

// Get record type configuration
RecordTypeConfig recordTypeConfig = sourceConfig.getRecordType("record_1");

// Access field configurations
List<FieldConfig> fields = recordTypeConfig.getFields();
FieldConfig fieldConfig = recordTypeConfig.getField("id");
```

#### SourceConfig
Represents a data source:

```java
SourceConfig source = loader.getSourceConfig("source_1");

String sourceName = source.getSource();        // "source_1"
int maxSize = source.getSize();                // 40
int recordTypeCount = source.getRecordTypeCount();
List<String> recordTypes = source.getRecordTypeNames();

// Check if record type exists
if (source.hasRecordType("record_1")) {
    RecordTypeConfig recordType = source.getRecordType("record_1");
}
```

#### RecordTypeConfig
Represents a specific record type:

```java
RecordTypeConfig recordType = source.getRecordType("record_1");

String typeName = recordType.getName();        // "record_1"
int fieldCount = recordType.getFieldCount();   // 5
int totalLength = recordType.getTotalLength(); // Sum of all field lengths

// Validate record length
boolean validLength = recordType.hasValidLength(recordData);

// Get specific field
FieldConfig field = recordType.getField("id");
```

#### FieldConfig
Represents a single field:

```java
FieldConfig field = recordType.getField("id");

String fieldName = field.getName();            // "id"
int length = field.getLength();                // 5
FieldConfig.FieldType type = field.getType();  // INTEGER
String regex = field.getRegex();
boolean required = field.isRequired();

// Validate field value
boolean isValid = field.validate("12345");

// Parse field value
Object parsedValue = field.parseValue("12345");  // Returns Integer: 12345
```

## Example YAML Configuration

See `textual-config-example.yaml` for a complete example with multiple sources and record types.

```yaml
sources:
  - source: source_1
    size: 40
    records:
      - type: record_1
        fields:
          - name: id
            length: 5
            type: INTEGER
            required: true
          - name: name
            length: 15
            type: STRING
            required: true
          - name: email
            length: 15
            type: STRING
            regex: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
          - name: status
            length: 1
            type: STRING
          - name: amount
            length: 9
            type: DECIMAL
```

## Supported Data Types

- **STRING**: Plain text values
- **INTEGER**: Whole numbers
- **DECIMAL**: Decimal numbers (floating point)
- **BOOLEAN**: True/false values (true/false, yes/no, 1/0)
- **DATE**: Date values (parsed as strings, format validation via regex)

## Usage Examples

### Example 1: Parse a Single Record

```java
String recordData = "00001John       john@exam.comAY       1234.56789";

TextualParser parser = new TextualParser("config.yaml");
TextualNode node = parser.parseRecord("source_1", "record_1", recordData);

System.out.println("Record Type: " + node.getRecordType());
System.out.println("ID: " + node.getAsString("id"));
System.out.println("Name: " + node.getAsString("name"));
System.out.println("Amount: " + node.getAsDouble("amount"));
```

### Example 2: Parse an Entire File

```java
TextualParser parser = new TextualParser("config.yaml");
List<TextualNode> records = parser.parseFile("source_1", "record_1", "data.txt");

for (TextualNode record : records) {
    System.out.println("Parsed record: " + record.getAsString("name"));
    record.getFieldNames().forEach(fieldName -> 
        System.out.println("  " + fieldName + ": " + record.get(fieldName))
    );
}
```

### Example 3: Parse with Type Detection

```java
TextualParser parser = new TextualParser("config.yaml");

List<TextualNode> records = parser.parseFileWithTypeDetection(
    "source_1",
    "mixed_records.txt",
    line -> {
        // Determine type based on first 2 characters
        String typeCode = line.substring(0, 2);
        return typeCode.equals("01") ? "record_1" : "record_2";
    }
);

for (TextualNode record : records) {
    System.out.println("Type: " + record.getRecordType());
    System.out.println(record.toFormattedString());
}
```

### Example 4: Validate Records

```java
TextualParser parser = new TextualParser("config.yaml");

String recordData = "00001John       john@exam.comAY       1234.56789";
if (parser.validateRecord("source_1", "record_1", recordData)) {
    System.out.println("Record is valid");
    TextualNode node = parser.parseRecord("source_1", "record_1", recordData);
} else {
    System.out.println("Record validation failed");
}
```

## Configuration Best Practices

1. **Field Lengths**: Ensure field lengths match actual data in your files
2. **Required Fields**: Mark critical fields as required
3. **Regex Validation**: Use regex patterns for format validation (emails, phone numbers, etc.)
4. **Type Specification**: Always specify the correct data type for proper parsing
5. **Default Values**: Provide defaults for optional fields
6. **Documentation**: Add comments in YAML for complex field definitions

## Error Handling

The library logs warnings and errors using SLF4J:

```java
// Enable logging to see validation warnings and parsing errors
// Configure your logging framework (e.g., slf4j-simple, logback)
```

Common issues:
- **Source not found**: Check if source name in YAML matches the parser call
- **Record type not found**: Verify record type name exists in configuration
- **Validation failures**: Check field lengths match actual data and regex patterns are correct
- **Type conversion errors**: Ensure field values match the specified type

## Testing

Run the provided unit tests:

```bash
mvn test
```

## Maven Build

```bash
# Clean and build
mvn clean package

# Run with dependencies
mvn clean compile

# Generate JAR
mvn package
```

## Dependencies

- **org.yaml:snakeyaml:2.0** - YAML parsing
- **org.slf4j:slf4j-api:2.0.5** - Logging API
- **org.slf4j:slf4j-simple:2.0.5** - Logging implementation
- **junit:junit:4.13.2** - Testing (test scope)

## Future Enhancements

- XML parser implementation
- JSON parser implementation
- Database connection for configuration storage
- Performance optimizations for large files
- Custom field validators
- Data export to JSON/XML
- Web service API for parsing

## License

See LICENSE file for details.

## Contributing

Contributions are welcome! Please follow the existing code style and add tests for new features.
