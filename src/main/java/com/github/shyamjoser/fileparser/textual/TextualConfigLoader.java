package com.github.shyamjoser.fileparser.textual;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Loads and parses YAML configuration files for textual data parsing.
 * Supports multiple sources and record types with field definitions.
 */
public class TextualConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(TextualConfigLoader.class);
    private Map<String, SourceConfig> sources;

    /**
     * Default constructor initializes the sources map.
     */
    public TextualConfigLoader() {
        this.sources = new LinkedHashMap<>();
    }

    /**
     * Loads configuration from a YAML file.
     *
     * @param filePath the path to the YAML configuration file
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if the YAML structure is invalid
     */
    public void loadFromYaml(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Configuration file not found: " + filePath);
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            loadFromStream(fis);
        }
    }

    /**
     * Loads configuration from an InputStream.
     *
     * @param inputStream the input stream containing YAML data
     */
    public void loadFromStream(InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.load(inputStream);

        if (config == null || config.isEmpty()) {
            logger.warn("Configuration file is empty");
            return;
        }

        parseConfiguration(config);
    }

    /**
     * Loads configuration from a YAML string.
     *
     * @param yamlContent the YAML content as a string
     */
    public void loadFromString(String yamlContent) {
        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.load(yamlContent);

        if (config == null || config.isEmpty()) {
            logger.warn("Configuration is empty");
            return;
        }

        parseConfiguration(config);
    }

    /**
     * Parses the loaded YAML configuration map.
     *
     * @param config the configuration map
     */
    @SuppressWarnings("unchecked")
    private void parseConfiguration(Map<String, Object> config) {
        List<Object> sourcesList = (List<Object>) config.get("sources");
        if (sourcesList == null || sourcesList.isEmpty()) {
            logger.warn("No sources defined in configuration");
            return;
        }

        for (Object sourceObj : sourcesList) {
            Map<String, Object> sourceMap = (Map<String, Object>) sourceObj;
            SourceConfig sourceConfig = parseSourceConfig(sourceMap);
            if (sourceConfig != null) {
                sources.put(sourceConfig.getSource(), sourceConfig);
                logger.info("Loaded source: {}", sourceConfig.getSource());
            }
        }
    }

    /**
     * Parses a single source configuration from the map.
     *
     * @param sourceMap the source configuration map
     * @return the parsed SourceConfig, or null if invalid
     */
    @SuppressWarnings("unchecked")
    private SourceConfig parseSourceConfig(Map<String, Object> sourceMap) {
        String sourceName = (String) sourceMap.get("source");
        Object sizeObj = sourceMap.get("size");

        if (sourceName == null || sourceName.isEmpty()) {
            logger.error("Source name is missing in configuration");
            return null;
        }

        int size = 0;
        if (sizeObj instanceof Number) {
            size = ((Number) sizeObj).intValue();
        } else if (sizeObj instanceof String) {
            try {
                size = Integer.parseInt((String) sizeObj);
            } catch (NumberFormatException e) {
                logger.error("Invalid size value for source {}: {}", sourceName, sizeObj);
                return null;
            }
        }

        SourceConfig config = new SourceConfig(sourceName, size);

        List<Object> records = (List<Object>) sourceMap.get("records");
        if (records != null) {
            for (Object recordObj : records) {
                Map<String, Object> recordMap = (Map<String, Object>) recordObj;
                RecordTypeConfig recordTypeConfig = parseRecordTypeConfig(recordMap);
                if (recordTypeConfig != null) {
                    config.addRecordType(recordTypeConfig.getName(), recordTypeConfig);
                }
            }
        }

        return config;
    }

    /**
     * Parses a single record type configuration from the map.
     *
     * @param recordMap the record configuration map
     * @return the parsed RecordTypeConfig, or null if invalid
     */
    @SuppressWarnings("unchecked")
    private RecordTypeConfig parseRecordTypeConfig(Map<String, Object> recordMap) {
        String recordType = (String) recordMap.get("type");
        if (recordType == null || recordType.isEmpty()) {
            logger.error("Record type name is missing in configuration");
            return null;
        }

        RecordTypeConfig config = new RecordTypeConfig(recordType);

        List<Object> fields = (List<Object>) recordMap.get("fields");
        if (fields != null) {
            for (Object fieldObj : fields) {
                Map<String, Object> fieldMap = (Map<String, Object>) fieldObj;
                FieldConfig fieldConfig = parseFieldConfig(fieldMap);
                if (fieldConfig != null) {
                    config.addField(fieldConfig);
                }
            }
        }

        return config;
    }

    /**
     * Parses a single field configuration from the map.
     *
     * @param fieldMap the field configuration map
     * @return the parsed FieldConfig, or null if invalid
     */
    private FieldConfig parseFieldConfig(Map<String, Object> fieldMap) {
        String fieldName = (String) fieldMap.get("name");
        Object lengthObj = fieldMap.get("length");

        if (fieldName == null || fieldName.isEmpty()) {
            logger.error("Field name is missing in configuration");
            return null;
        }

        int length = 0;
        if (lengthObj instanceof Number) {
            length = ((Number) lengthObj).intValue();
        } else if (lengthObj instanceof String) {
            try {
                length = Integer.parseInt((String) lengthObj);
            } catch (NumberFormatException e) {
                logger.error("Invalid length value for field {}: {}", fieldName, lengthObj);
                return null;
            }
        }

        FieldConfig config = new FieldConfig(fieldName, length);

        String type = (String) fieldMap.get("type");
        if (type != null) {
            config.setType(type);
        }

        String regex = (String) fieldMap.get("regex");
        if (regex != null) {
            config.setRegex(regex);
        }

        Object requiredObj = fieldMap.get("required");
        if (requiredObj instanceof Boolean) {
            config.setRequired((Boolean) requiredObj);
        } else if (requiredObj instanceof String) {
            config.setRequired(Boolean.parseBoolean((String) requiredObj));
        }

        String defaultValue = (String) fieldMap.get("default");
        if (defaultValue != null) {
            config.setDefaultValue(defaultValue);
        }

        return config;
    }

    /**
     * Gets a source configuration by name.
     *
     * @param sourceName the name of the source
     * @return the SourceConfig if found, null otherwise
     */
    public SourceConfig getSourceConfig(String sourceName) {
        return sources.get(sourceName);
    }

    /**
     * Gets all loaded sources.
     *
     * @return map of source name to SourceConfig
     */
    public Map<String, SourceConfig> getSources() {
        return Collections.unmodifiableMap(sources);
    }

    /**
     * Checks if a source is loaded.
     *
     * @param sourceName the name of the source
     * @return true if the source exists, false otherwise
     */
    public boolean hasSource(String sourceName) {
        return sources.containsKey(sourceName);
    }

    /**
     * Gets the number of loaded sources.
     *
     * @return the count of sources
     */
    public int getSourceCount() {
        return sources.size();
    }
}

