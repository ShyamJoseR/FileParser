package com.github.shyamjoser.fileparser.textual.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.shyamjoser.fileparser.textual.annotation.FixedField;
import com.github.shyamjoser.fileparser.textual.annotation.FixedLengthRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Enterprise-grade parser for fixed-length record formats.
 * 
 * This parser handles serialization and deserialization of fixed-length formatted data
 * into Java objects using annotations and reflection, with support for JSON serialization.
 *
 * Key features:
 * - Stream-based processing for better performance and readability
 * - Functional programming patterns for field extraction and formatting
 * - Reflection caching to optimize repeated parsing operations
 * - JSON serialization support with enterprise-grade Jackson library
 * - Comprehensive error handling and logging
 * - Type-safe generic operations
 * 
 * Thread-safe for concurrent parsing operations.
 */
public class FixedLengthParser {
    
    private static final Logger logger = LoggerFactory.getLogger(FixedLengthParser.class);
    private static final Map<Class<?>, Field[]> fieldCache = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses a fixed-length string into a strongly-typed object instance.
     * 
     * @param <T> The target class type
     * @param data The fixed-length record string
     * @param clazz The class to deserialize into
     * @return A populated instance of the specified class
     * @throws IllegalArgumentException if data is null or input validation fails
     * @throws IllegalAccessException if field access is denied
     * @throws InstantiationException if instance creation fails
     */
    public static <T> T parse(String data, Class<T> clazz) throws Exception {
        validateParseInput(data, clazz);
        logger.debug("Parsing data into class: {}", clazz.getName());
        
        T instance = createInstance(clazz);
        getAnnotatedFields(clazz)
            .forEach(field -> parseField(instance, field, data));
        
        logger.debug("Successfully parsed {} bytes into {}", data.length(), clazz.getSimpleName());
        return instance;
    }

    /**
     * Serializes a Java object instance into a fixed-length formatted string.
     * 
     * @param <T> The source object type
     * @param instance The object to serialize
     * @return A fixed-length formatted string representation
     * @throws IllegalArgumentException if instance class is not annotated or validation fails
     * @throws IllegalAccessException if field access is denied
     */
    public static <T> String serialize(T instance) throws Exception {
        Objects.requireNonNull(instance, "Instance cannot be null");
        
        Class<?> clazz = instance.getClass();
        validateSerializeInput(clazz);
        logger.debug("Serializing instance of class: {}", clazz.getName());
        
        FixedLengthRecord recordAnnotation = clazz.getAnnotation(FixedLengthRecord.class);
        int totalLength = recordAnnotation.totalLength();
        
        StringBuilder result = initializeBuffer(totalLength);
        
        getAnnotatedFields(clazz)
            .forEach(field -> serializeField(instance, field, result, totalLength));
        
        String output = applyTrimming(recordAnnotation, result);
        logger.debug("Successfully serialized to {} characters", output.length());
        return output;
    }

    /**
     * Serializes a Java object instance into a compact JSON representation.
     *
     * This method converts an object containing @FixedField annotated fields into a compact,
     * space-efficient JSON format suitable for high-performance API responses and data interchange.
     * Returns an ObjectNode for zero-copy performance optimization and flexible downstream processing.
     *
     * @param <T> The source object type
     * @param instance The object to serialize to JSON
     * @return An ObjectNode containing only field values in compact format
     * @throws IllegalArgumentException if instance is null or class validation fails
     * @throws IllegalAccessException if field access is denied during reflection
     * @throws IllegalStateException if JSON serialization fails
     *
     * @example
     * FixedLengthParserExamples.Customer customer = new Customer();
     * customer.setId("123");
     * customer.setName("John Doe");
     * ObjectNode json = FixedLengthParser.serializeToJson(customer);
     * // Output: {"id": "123", "name": "John Doe"}
     * // Or as string: json.toString() -> {"id":"123","name":"John Doe"}
     */
    public static <T> ObjectNode serializeToJson(T instance) throws Exception {
        Objects.requireNonNull(instance, "Instance cannot be null");

        Class<?> clazz = instance.getClass();
        validateSerializeInput(clazz);
        logger.debug("Serializing instance to JSON: {}", clazz.getName());

        try {
            ObjectNode rootNode = objectMapper.createObjectNode();

            // Extract field values directly into root node for compact format
            getAnnotatedFields(clazz)
                .forEach(field -> extractFieldToJson(instance, field, rootNode));

            logger.debug("Successfully serialized to ObjectNode with {} fields", rootNode.size());
            logger.trace("ObjectNode: {}", rootNode);

            return rootNode;

        } catch (Exception e) {
            logger.error("Failed to serialize instance to JSON: {}", clazz.getName(), e);
            throw new IllegalStateException(
                "JSON serialization failed for class: " + clazz.getName(), e);
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * Validates parse operation input parameters.
     */
    private static <T> void validateParseInput(String data, Class<T> clazz) {
        if (data == null) {
            logger.error("Parse input validation failed: data is null");
            throw new IllegalArgumentException("Data cannot be null");
        }
        if (clazz == null) {
            logger.error("Parse input validation failed: clazz is null");
            throw new IllegalArgumentException("Class cannot be null");
        }
    }

    /**
     * Validates serialize operation input parameters.
     */
    private static void validateSerializeInput(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(FixedLengthRecord.class)) {
            logger.error("Serialize validation failed: {} is not annotated with @FixedLengthRecord", 
                clazz.getName());
            throw new IllegalArgumentException(
                "Class must be annotated with @FixedLengthRecord: " + clazz.getName());
        }
    }

    /**
     * Creates a new instance of the specified class using default constructor.
     */
    private static <T> T createInstance(Class<T> clazz) throws Exception {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("Failed to instantiate class {}", clazz.getName(), e);
            throw new IllegalArgumentException("Cannot instantiate class: " + clazz.getName(), e);
        }
    }

    /**
     * Retrieves all fields annotated with @FixedField using Stream API.
     * Results are cached for improved performance on repeated calls.
     */
    private static Stream<Field> getAnnotatedFields(Class<?> clazz) {
        return Arrays.stream(getCachedFields(clazz))
            .filter(field -> field.isAnnotationPresent(FixedField.class));
    }

    /**
     * Gets or caches the array of declared fields for a class.
     */
    private static Field[] getCachedFields(Class<?> clazz) {
        return fieldCache.computeIfAbsent(clazz, c -> {
            Field[] fields = c.getDeclaredFields();
            logger.trace("Cached {} fields for class {}", fields.length, c.getName());
            return fields;
        });
    }

    /**
     * Parses a single annotated field from the input data string.
     */
    private static <T> void parseField(T instance, Field field, String data) {
        try {
            FixedField annotation = field.getAnnotation(FixedField.class);
            int position = annotation.position();
            int length = annotation.length();
            
            field.setAccessible(true);
            String value = extractFieldValue(data, position, length, annotation.trim());
            
            if (value.isEmpty() && !annotation.defaultValue().isEmpty()) {
                field.set(instance, annotation.defaultValue());
                logger.trace("Applied default value for field: {}", field.getName());
            } else {
                field.set(instance, value);
                logger.trace("Parsed field {}: length={}", field.getName(), value.length());
            }
        } catch (IllegalAccessException e) {
            logger.error("Failed to set field value: {}", field.getName(), e);
            throw new RuntimeException("Field access error: " + field.getName(), e);
        }
    }

    /**
     * Extracts a substring from data based on field position and length.
     */
    private static String extractFieldValue(String data, int position, int length, boolean shouldTrim) {
        if (position <= 0 || position > data.length()) {
            return "";
        }
        
        int start = position - 1;  // Convert to 0-based index
        int end = Math.min(start + length, data.length());
        String value = data.substring(start, end);
        
        return shouldTrim ? value.trim() : value;
    }

    /**
     * Serializes a single annotated field and inserts it into the output buffer.
     */
    private static <T> void serializeField(T instance, Field field, StringBuilder buffer, int totalLength) {
        try {
            FixedField annotation = field.getAnnotation(FixedField.class);
            field.setAccessible(true);
            
            Object fieldValue = field.get(instance);
            String formattedValue = formatFieldValue(fieldValue, annotation.length());
            
            int position = annotation.position();
            insertIntoBuffer(buffer, position, formattedValue, totalLength);
            logger.trace("Serialized field {}: value={}", field.getName(), formattedValue);
            
        } catch (IllegalAccessException e) {
            logger.error("Failed to get field value: {}", field.getName(), e);
            throw new RuntimeException("Field access error: " + field.getName(), e);
        }
    }

    /**
     * Formats a field value: pad to fixed length and convert to string.
     */
    private static String formatFieldValue(Object value, int length) {
        String strValue = value != null ? value.toString() : "";
        
        // Left-align and pad with spaces
        strValue = String.format("%-" + length + "s", strValue);
        
        // Truncate if exceeds field length
        if (strValue.length() > length) {
            strValue = strValue.substring(0, length);
        }
        
        return strValue;
    }

    /**
     * Initializes the output buffer with the specified total length.
     */
    private static StringBuilder initializeBuffer(int totalLength) {
        StringBuilder buffer = new StringBuilder();
        if (totalLength > 0) {
            buffer.setLength(totalLength);
            // Fill with spaces
            for (int i = 0; i < totalLength; i++) {
                buffer.setCharAt(i, ' ');
            }
        }
        return buffer;
    }

    /**
     * Inserts a formatted field value into the buffer at the specified position.
     */
    private static void insertIntoBuffer(StringBuilder buffer, int position, String value, int totalLength) {
        int start = position - 1;  // Convert to 0-based index
        int end = start + value.length();
        
        if (totalLength > 0) {
            // Replace within fixed-size buffer
            int adjustedEnd = Math.min(end, totalLength);
            buffer.replace(start, adjustedEnd, value);
        } else {
            // Append to dynamic buffer
            buffer.append(value);
        }
    }

    /**
     * Applies trimming to the final output based on record annotation settings.
     */
    private static String applyTrimming(FixedLengthRecord annotation, StringBuilder buffer) {
        String result = buffer.toString();
        return annotation.trim() ? result.trim() : result;
    }

    /**
     * Extracts a single annotated field value and inserts it into the JSON object node.
     * Handles null values gracefully and logs extraction details for debugging.
     */
    private static <T> void extractFieldToJson(T instance, Field field, ObjectNode fieldsNode) {
        try {
            FixedField annotation = field.getAnnotation(FixedField.class);
            field.setAccessible(true);

            Object fieldValue = field.get(instance);
            String fieldName = field.getName();

            if (fieldValue == null) {
                fieldsNode.putNull(fieldName);
                logger.trace("Extracted field {} as null", fieldName);
            } else {
                fieldsNode.put(fieldName, fieldValue.toString());
                logger.trace("Extracted field {}: value={}", fieldName, fieldValue);
            }

        } catch (IllegalAccessException e) {
            logger.error("Failed to extract field for JSON serialization: {}", field.getName(), e);
            throw new RuntimeException("Field access error during JSON extraction: " + field.getName(), e);
        }
    }
}