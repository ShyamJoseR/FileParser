# Textual Parser - Developer Guide

## Introduction for Developers

This guide is intended for developers who want to extend, maintain, or contribute to the Textual Parser library.

## Project Setup

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Git (for version control)

### Building the Project

```bash
# Clone the repository
git clone <repository-url>
cd FileParser

# Build the project
mvn clean install

# Run tests
mvn test

# Package as JAR
mvn package
```

## Code Organization

### Package Structure

```
com.github.shyamjoser.fileparser.textual
├── FieldConfig              - Field definition and validation
├── RecordTypeConfig         - Record type structure
├── SourceConfig             - Data source configuration
├── TextualConfigLoader      - Configuration file parsing
├── TextualNode              - Parsed record container
├── TextualParser            - Main parsing engine
└── TextualParserExample     - Usage examples
```

### Class Responsibilities

#### FieldConfig
- **Responsibility**: Represent and manage a single field definition
- **Key Methods**:
  - `validate(String value)`: Validates field value
  - `parseValue(String value)`: Converts string to appropriate type
- **Dependencies**: None (no external dependencies)
- **Test Coverage**: FieldConfig validation and parsing

#### RecordTypeConfig
- **Responsibility**: Represent a record type with multiple fields
- **Key Methods**:
  - `getField(String fieldName)`: Get field by name
  - `getTotalLength()`: Calculate total record length
  - `hasValidLength(String rawRecord)`: Validate record length
- **Dependencies**: FieldConfig
- **Composition**: List of FieldConfig objects

#### SourceConfig
- **Responsibility**: Represent a data source with multiple record types
- **Key Methods**:
  - `getRecordType(String recordType)`: Get record type by name
  - `hasRecordType(String recordType)`: Check if record type exists
- **Dependencies**: RecordTypeConfig
- **Composition**: Map of RecordTypeConfig objects

#### TextualConfigLoader
- **Responsibility**: Load and parse YAML configuration files
- **Key Methods**:
  - `loadFromYaml(String filePath)`: Load from file
  - `loadFromString(String yamlContent)`: Load from string
  - `getSourceConfig(String sourceName)`: Get source configuration
- **Dependencies**: SnakeYAML library
- **Caching**: Stores configurations in Map<String, SourceConfig>

#### TextualParser
- **Responsibility**: Main entry point for parsing operations
- **Key Methods**:
  - `parseRecord(...)`: Parse single record
  - `parseFile(...)`: Parse entire file
  - `parseFileWithTypeDetection(...)`: Parse with type detection
  - `validateRecord(...)`: Validate record structure
- **Dependencies**: TextualConfigLoader, TextualNode
- **Stateless**: Can be used for multiple parsing operations

#### TextualNode
- **Responsibility**: Container for parsed record data
- **Key Methods**:
  - `get(String fieldName)`: Get field value
  - `getAsString/Int/Double/Boolean(...)`: Type-safe accessors
  - `put(String fieldName, Object value)`: Add field
- **Dependencies**: None (data container only)
- **Design**: Maintains field order using LinkedHashMap

## Adding New Features

### Adding a New Field Type

1. **Update FieldConfig.FieldType enum**:
```java
public enum FieldType {
    STRING, INTEGER, DECIMAL, BOOLEAN, DATE, NEWTYPE
}
```

2. **Update parseValue() method**:
```java
case NEWTYPE:
    // Add parsing logic
    return parsedValue;
```

3. **Update validate() method**:
```java
case NEWTYPE:
    // Add validation logic
    return isValid;
```

4. **Update YAML documentation** to include new type

5. **Add test cases** for new type

### Adding Custom Field Validators

Current implementation uses regex. To add custom validators:

1. **Create a FieldValidator interface**:
```java
public interface FieldValidator {
    boolean validate(String value);
}
```

2. **Update FieldConfig** to use validator:
```java
private FieldValidator customValidator;

public boolean validate(String value) {
    if (customValidator != null) {
        return customValidator.validate(value);
    }
    // existing validation
}
```

3. **Update TextualConfigLoader** to support custom validators

4. **Add tests** for custom validators

### Adding Record-Level Validation

Current implementation validates individual fields. For record-level validation:

1. **Create RecordValidator interface**:
```java
public interface RecordValidator {
    boolean validate(TextualNode record);
}
```

2. **Add to TextualParser**:
```java
private List<RecordValidator> recordValidators;

private TextualNode parseRecordInternal(...) {
    TextualNode node = ...;
    for (RecordValidator validator : recordValidators) {
        if (!validator.validate(node)) {
            logger.warn("Record validation failed");
        }
    }
    return node;
}
```

## Testing Guide

### Unit Test Structure

```java
@Before
public void setUp() {
    // Create test configuration
    // Initialize parser
}

@Test
public void testSomethingSpecific() {
    // Arrange: Set up test data
    String testData = "...";
    
    // Act: Execute the code being tested
    TextualNode result = parser.parseRecord(...);
    
    // Assert: Verify the result
    assertNotNull(result);
    assertEquals(expected, result.getAsInt("field"));
}
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TextualParserTest

# Run specific test method
mvn test -Dtest=TextualParserTest#testParseRecord

# Run with coverage
mvn test jacoco:report
```

### Test Checklist

- [ ] All public methods have tests
- [ ] Success paths are tested
- [ ] Error paths are tested (null, invalid input, etc.)
- [ ] Edge cases are covered (empty strings, boundary values)
- [ ] Type conversions are tested
- [ ] Configuration loading is tested
- [ ] File I/O is tested

## Code Style and Conventions

### Naming Conventions

- **Classes**: PascalCase (TextualParser, FieldConfig)
- **Methods**: camelCase (parseRecord, validateField)
- **Constants**: UPPER_SNAKE_CASE (if any)
- **Variables**: camelCase (recordData, fieldValue)

### Javadoc Requirements

Every public class and method must have Javadoc:

```java
/**
 * Brief description of what this class does.
 * 
 * Longer description if needed, explaining any important details,
 * usage patterns, or implementation notes.
 */
public class ClassName {
    
    /**
     * Describes what the method does.
     *
     * @param paramName description of parameter
     * @return description of return value
     * @throws ExceptionType when this exception is thrown
     */
    public ReturnType methodName(String paramName) {
        // ...
    }
}
```

### Logging Best Practices

```java
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);

// Use appropriate levels
logger.debug("Detailed information for debugging");
logger.info("General informational messages");
logger.warn("Warning messages for recoverable issues");
logger.error("Error messages for problems", exception);
```

## Performance Optimization

### Current Performance

- **Configuration Loading**: O(s×r×f) - linear in sources, record types, fields
- **Record Parsing**: O(f) - linear in number of fields
- **File Parsing**: O(n×f) - linear in records and fields

### Optimization Opportunities

1. **Lazy Regex Compilation**: Cache compiled Pattern objects
   ```java
   private static final Map<String, Pattern> REGEX_CACHE = new HashMap<>();
   ```

2. **Field Position Caching**: Pre-calculate field positions
   ```java
   private int endPosition;  // For each field
   ```

3. **Batch Processing**: Process records in batches for memory efficiency

4. **Parallel Parsing**: Use parallel streams for large files
   ```java
   lines.parallelStream()
        .map(line -> parseRecord(...))
        .collect(Collectors.toList())
   ```

## Debugging Guide

### Enable Detailed Logging

Create `src/main/resources/simplelogger.properties`:

```properties
org.slf4j.simpleLogger.defaultLogLevel=DEBUG
org.slf4j.simpleLogger.log.com.github.shyamjoser.fileparser.textual=DEBUG
org.slf4j.simpleLogger.showDateTime=true
org.slf4j.simpleLogger.dateTimeFormat=yyyy-MM-dd HH:mm:ss
```

### Common Issues and Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| NullPointerException on parse | Source/type not found | Check configuration and source/type names |
| ValidationException | Field length mismatch | Verify field lengths sum to expected record size |
| Type conversion error | Invalid field data | Check field type matches actual data |
| File not found | Wrong file path | Verify file path is correct and file exists |
| ClassNotFoundException | Missing dependency | Run `mvn clean install` |

## Contributing Guide

### Before Starting

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Set up local development environment

### During Development

1. Follow code style guidelines (see above)
2. Add/update tests for changes
3. Update documentation if needed
4. Ensure all tests pass: `mvn test`
5. Check code coverage

### Before Submitting Pull Request

1. Run full build: `mvn clean package`
2. Run tests: `mvn test`
3. Update README.md if needed
4. Add Javadoc for all public methods
5. Rebase against main branch
6. Create descriptive pull request

## Release Guide

### Version Numbering

Follow semantic versioning: MAJOR.MINOR.PATCH

- **MAJOR**: Breaking API changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes

### Release Checklist

- [ ] Update version in pom.xml
- [ ] Update CHANGELOG.md
- [ ] Run full test suite
- [ ] Generate API documentation
- [ ] Tag release in Git
- [ ] Create GitHub release
- [ ] Deploy to Maven Central (if applicable)

## Documentation

### Updating Documentation

1. **README.md**: User-facing documentation
2. **TEXTUAL_PARSER_QUICKSTART.md**: Quick start guide
3. **TECHNICAL_SPECIFICATION.md**: Architecture and design
4. **Javadoc**: Inline code documentation
5. **IMPLEMENTATION_SUMMARY.md**: Implementation details

### Documentation Requirements

- Keep README up to date with API changes
- Update Javadoc for any modified methods
- Add examples for new features
- Update TECHNICAL_SPECIFICATION if architecture changes

## Troubleshooting

### Build Issues

```bash
# Clean Maven cache
mvn clean
rm -rf ~/.m2/repository

# Rebuild
mvn install
```

### Test Failures

```bash
# Run single failing test with output
mvn test -Dtest=TestClass#testMethod -X

# Skip tests and build
mvn clean package -DskipTests
```

### IDE Setup (IntelliJ IDEA)

1. Open project: File > Open > select project directory
2. Maven should auto-configure
3. Run tests: Right-click on test > Run
4. Debug: Set breakpoint and run with Debug

## Architecture Decisions

### Why LinkedHashMap for TextualNode?

- Preserves field insertion order
- Important for debugging and logging
- Allows iteration in defined order

### Why SnakeYAML?

- Well-maintained YAML parser
- Simple API for our use case
- Good error handling and logging

### Why SLF4J?

- Flexible logging framework
- Can use different backends (logback, log4j, etc.)
- Standard in Java projects

## Future Roadmap

### Version 1.1 (Planned)

- [ ] Performance optimizations (parallel processing)
- [ ] Custom field validators
- [ ] Record-level validation
- [ ] Data transformation utilities

### Version 1.2 (Planned)

- [ ] JSON output from TextualNode
- [ ] XML output from TextualNode
- [ ] Batch validation reports
- [ ] Configuration UI generator

### Version 2.0 (Future)

- [ ] Variable-length field support
- [ ] Embedded delimiter handling
- [ ] Database configuration storage
- [ ] REST API wrapper

## Resources

- [Maven Documentation](https://maven.apache.org/)
- [SLF4J Documentation](http://www.slf4j.org/)
- [SnakeYAML Documentation](https://bitbucket.org/asomov/snakeyaml)
- [Java 11 Documentation](https://docs.oracle.com/en/java/javase/11/)

## Contact and Support

For questions or issues:
1. Check existing GitHub issues
2. Create a new issue with detailed description
3. Include configuration, sample data, and error messages
4. Submit pull request with fix if possible

---

**Last Updated**: January 4, 2026
**Maintained By**: shyamjoser
**License**: See LICENSE file

