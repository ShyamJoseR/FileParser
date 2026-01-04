package com.github.shyamjoser.fileparser.textual;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Represents the configuration for a single field in a record.
 * Includes field name, length, data type, and validation rules.
 */
public class FieldConfig {

    private String name;
    private int length;
    private FieldType type;
    private String regex;
    private boolean required;
    private String defaultValue;
    private int position;

    /**
     * Enum for field data types.
     */
    public enum FieldType {
        STRING, INTEGER, DECIMAL, BOOLEAN, DATE
    }

    /**
     * Default constructor.
     */
    public FieldConfig() {
        this.required = false;
        this.type = FieldType.STRING;
    }

    /**
     * Constructor with field name and length.
     *
     * @param name the field name
     * @param length the field length in characters
     */
    public FieldConfig(String name, int length) {
        this();
        this.name = name;
        this.length = length;
    }

    /**
     * Constructor with all parameters.
     *
     * @param name the field name
     * @param length the field length in characters
     * @param type the field data type
     * @param regex optional regex pattern for validation
     */
    public FieldConfig(String name, int length, FieldType type, String regex) {
        this();
        this.name = name;
        this.length = length;
        this.type = type;
        this.regex = regex;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public void setType(String typeString) {
        try {
            this.type = FieldType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.type = FieldType.STRING;
        }
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Validates the given value against this field's configuration.
     *
     * @param value the value to validate
     * @return true if the value is valid, false otherwise
     */
    public boolean validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return !required;
        }

        // Check regex if provided
        if (regex != null && !regex.isEmpty()) {
            if (!Pattern.matches(regex, value)) {
                return false;
            }
        }

        // Check type-specific validation
        switch (type) {
            case INTEGER:
                try {
                    Integer.parseInt(value.trim());
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case DECIMAL:
                try {
                    Double.parseDouble(value.trim());
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case BOOLEAN:
                String bool = value.trim().toLowerCase();
                return bool.equals("true") || bool.equals("false") ||
                       bool.equals("yes") || bool.equals("no") ||
                       bool.equals("1") || bool.equals("0");
            case STRING:
            case DATE:
                return true;
            default:
                return true;
        }
    }

    /**
     * Parses the value according to the field's type.
     *
     * @param value the string value to parse
     * @return the parsed value object
     */
    public Object parseValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue != null ? defaultValue : null;
        }

        String trimmed = value.trim();

        switch (type) {
            case INTEGER:
                try {
                    return Integer.parseInt(trimmed);
                } catch (NumberFormatException e) {
                    return null;
                }
            case DECIMAL:
                try {
                    return Double.parseDouble(trimmed);
                } catch (NumberFormatException e) {
                    return null;
                }
            case BOOLEAN:
                String bool = trimmed.toLowerCase();
                return bool.equals("true") || bool.equals("yes") || bool.equals("1");
            case STRING:
            case DATE:
            default:
                return trimmed;
        }
    }

    @Override
    public String toString() {
        return "FieldConfig{" +
                "name='" + name + '\'' +
                ", length=" + length +
                ", type=" + type +
                ", regex='" + regex + '\'' +
                ", required=" + required +
                ", position=" + position +
                '}';
    }
}

