# Textual Parser - Quick Reference Card

## 📌 One-Page Reference

### Installation & Setup

```bash
# Build
mvn clean install

# Test
mvn test

# Package
mvn package
```

### Basic Usage

```java
// Create parser
TextualParser parser = new TextualParser("config.yaml");

// Parse single record
TextualNode node = parser.parseRecord("source", "type", data);

// Parse file
List<TextualNode> records = parser.parseFile("source", "type", "file.txt");
```

### YAML Configuration Template

```yaml
sources:
  - source: my_source
    size: 100
    records:
      - type: my_record
        fields:
          - name: field_name
            length: 10
            type: STRING
            required: true
            regex: '.*'
            default: ''
```

### TextualNode API

| Method | Returns | Description |
|--------|---------|-------------|
| `get(name)` | Object | Get field value |
| `getAsString(name)` | String | Get as string |
| `getAsInt(name)` | int | Get as integer |
| `getAsDouble(name)` | double | Get as double |
| `getAsBoolean(name)` | boolean | Get as boolean |
| `has(name)` | boolean | Check field exists |
| `put(name, value)` | TextualNode | Add field (fluent) |
| `getFieldNames()` | List | Get all field names |
| `getFields()` | Map | Get all fields |
| `getRecordType()` | String | Get record type |
| `size()` | int | Get field count |
| `isEmpty()` | boolean | Check if empty |
| `toString()` | String | String representation |
| `toFormattedString()` | String | Formatted output |

### TextualParser API

| Method | Returns | Description |
|--------|---------|-------------|
| `parseRecord(src, type, data)` | TextualNode | Parse single |
| `parseFile(src, type, path)` | List | Parse file |
| `parseFromReader(src, type, reader)` | List | Parse stream |
| `validateRecord(src, type, data)` | boolean | Validate record |
| `getConfigLoader()` | ConfigLoader | Get config |

### Field Types

| Type | Format | Example |
|------|--------|---------|
| STRING | Text | "John Smith" |
| INTEGER | Numbers | "12345" |
| DECIMAL | Float | "123.45" |
| BOOLEAN | true/false | "true", "yes", "1" |
| DATE | String | "2026-01-04" |

### Common Patterns

#### Pattern 1: Parse and Iterate
```java
List<TextualNode> records = parser.parseFile("source", "type", "file.txt");
for (TextualNode record : records) {
    String id = record.getAsString("id");
    int amount = record.getAsInt("amount");
}
```

#### Pattern 2: Validate Before Parse
```java
if (parser.validateRecord("source", "type", data)) {
    TextualNode node = parser.parseRecord("source", "type", data);
}
```

#### Pattern 3: Type Detection
```java
List<TextualNode> records = parser.parseFileWithTypeDetection(
    "source", 
    "file.txt",
    line -> line.substring(0, 2).equals("01") ? "type1" : "type2"
);
```

#### Pattern 4: Stream Processing
```java
try (BufferedReader reader = new BufferedReader(new FileReader("file.txt"))) {
    List<TextualNode> records = parser.parseFromReader("source", "type", reader);
}
```

### Error Handling

```java
TextualNode node = parser.parseRecord("source", "type", data);
if (node == null) {
    // Handle parsing error
    System.err.println("Failed to parse record");
} else {
    // Process successfully parsed record
}
```

### Configuration Keys

| Key | Required | Type | Example |
|-----|----------|------|---------|
| source | Yes | String | "source_1" |
| size | Yes | Integer | 40 |
| records | Yes | Array | [...] |
| type | Yes | String | "record_1" |
| fields | Yes | Array | [...] |
| name | Yes | String | "id" |
| length | Yes | Integer | 5 |
| type | No | String | "INTEGER" |
| required | No | Boolean | true |
| regex | No | String | "^\d{5}$" |
| default | No | String | "0" |

### Validation Rules

```
Field Validation:
├── Required check (if required=true)
├── Regex validation (if regex defined)
├── Type validation (INTEGER, DECIMAL format)
└── Default applied if empty
```

### Performance Tips

1. **Reuse parser**: Create once, use multiple times
2. **Batch operations**: Use parseFile() instead of loop
3. **Validate sample**: Check one record before bulk processing
4. **Handle large files**: Use parseFromReader() for memory efficiency

### Debugging

```java
// Enable logging
System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");

// Check configuration
TextualConfigLoader loader = parser.getConfigLoader();
System.out.println(loader.getSources());

// Inspect parsed data
System.out.println(node.toFormattedString());
System.out.println(node.getFields());
```

### Common Issues

| Issue | Solution |
|-------|----------|
| NullPointerException | Check source/type names match config |
| Record too short | Verify field lengths sum correctly |
| Validation fails | Check field data matches type |
| Type conversion error | Ensure field value matches type |
| File not found | Verify file path is correct |

### File Locations

```
FileParser/
├── Config: src/main/resources/textual-config-example.yaml
├── Tests: src/test/java/.../TextualParserTest.java
├── Examples: src/main/java/.../TextualParserExample.java
├── Docs: README.md, QUICKSTART.md, TECHNICAL_SPECIFICATION.md
└── Build: pom.xml
```

### Maven Commands

```bash
# Clean build
mvn clean install

# Run tests
mvn test

# Skip tests
mvn clean install -DskipTests

# Run specific test
mvn test -Dtest=TextualParserTest

# Package JAR
mvn package

# Build documentation
mvn javadoc:javadoc
```

### Class Diagram

```
┌─────────────────────────────────────┐
│      TextualParser                  │
│  (Main entry point)                 │
└────────────┬────────────────────────┘
             │
    ┌────────┴─────────┐
    │                  │
┌───▼──────────────────▼───────┐
│   TextualConfigLoader        │
│   (Loads YAML)               │
└────────┬────────────────────┘
         │
    ┌────▼──────┐
    │SourceConfig│
    └────┬──────┘
         │
    ┌────▼────────────┐
    │RecordTypeConfig │
    └────┬────────────┘
         │
    ┌────▼────────┐
    │FieldConfig  │
    └─────────────┘
    
    ┌──────────────────┐
    │ TextualNode      │
    │ (Data container) │
    └──────────────────┘
```

### Data Flow

```
Raw Record String
    ↓
TextualParser.parseRecord()
    ↓
Get SourceConfig
    ↓
Get RecordTypeConfig
    ↓
For each FieldConfig:
  - Extract substring
  - Validate
  - Parse to type
    ↓
Create TextualNode
    ↓
Return TextualNode
```

### Links

- **User Guide**: README.md
- **Quick Start**: TEXTUAL_PARSER_QUICKSTART.md
- **Technical Spec**: TECHNICAL_SPECIFICATION.md
- **Developer Guide**: DEVELOPER_GUIDE.md
- **Examples**: TextualParserExample.java
- **Tests**: TextualParserTest.java
- **Config Example**: textual-config-example.yaml

### Key Points to Remember

✅ **YAML Configuration**: Define sources, types, and fields
✅ **TextualNode**: Container for parsed data (like JsonNode)
✅ **Type-Safe Access**: Use getAsInt(), getAsString(), etc.
✅ **Validation**: Built-in regex and type checking
✅ **Batch Processing**: Use parseFile() for efficiency
✅ **Error Handling**: Check for null and log issues
✅ **Logging**: Use SLF4J for debugging

---

## Quick Decision Tree

```
Need to parse fixed-width data?
    ├─ YES: Use TextualParser ✓
    │   ├─ Single record? → parseRecord()
    │   ├─ Entire file? → parseFile()
    │   ├─ Stream? → parseFromReader()
    │   └─ Auto-detect type? → parseFileWithTypeDetection()
    │
    └─ Configure first:
        └─ Create YAML file
            ├─ Define sources
            ├─ Define record types
            └─ Define fields (name, length, type)
```

---

**Print this page for quick reference while developing!**

For detailed information, refer to the complete documentation.

Last Updated: January 4, 2026

