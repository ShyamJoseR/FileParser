package com.github.shyamjoser.fileparser.textual.parser;

import com.github.shyamjoser.fileparser.textual.annotation.FixedField;
import com.github.shyamjoser.fileparser.textual.annotation.FixedLengthRecord;

import java.lang.reflect.Field;
import java.util.*;

public class FixedLengthParser {

    public static <T> T parse(String data, Class<T> clazz) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        T instance = clazz.getDeclaredConstructor().newInstance();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(FixedField.class)) {
                FixedField annotation = field.getAnnotation(FixedField.class);
                int start = annotation.position();

                if (start < data.length()) {
                    int end = Math.min(start + annotation.length() - 1, data.length());
                    String value = data.substring(start - 1, end);
                    if (annotation.trim()) {
                        value = value.trim();
                    }
                    field.setAccessible(true);
                    field.set(instance, value);
                } else {
                    field.setAccessible(true);
                    field.set(instance, annotation.defaultValue());
                }
            }
        }
        return instance;
    }

    public static <T> String serialize(T instance) throws Exception {
        Class<?> clazz = instance.getClass();

        if (!clazz.isAnnotationPresent(FixedLengthRecord.class)) {
            throw new IllegalArgumentException("Class must be annotated with @FixedLengthRecord");
        }

        FixedLengthRecord recordAnnotation = clazz.getAnnotation(FixedLengthRecord.class);
        int totalLength = recordAnnotation.totalLength();

        StringBuilder result = new StringBuilder();
        if (totalLength > 0) {
            result.setLength(totalLength);
            for (int i = 0; i < totalLength; i++) {
                result.setCharAt(i, ' ');
            }
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(FixedField.class)) {
                FixedField annotation = field.getAnnotation(FixedField.class);
                field.setAccessible(true);
                Object value = field.get(instance);

                String strValue = value != null ? value.toString() : "";
                int start = annotation.position();
                int length = annotation.length();

                // Pad or truncate to fit the field length
                strValue = String.format("%-" + length + "s", strValue);
                if (strValue.length() > length) {
                    strValue = strValue.substring(0, length);
                }

                if (totalLength > 0) {
                    result.replace(start - 1, start + length - 1, strValue);
                } else {
                    result.append(strValue);
                }
            }
        }
        if (recordAnnotation.trim()) {
            return result.toString().trim();   
        }
        return result.toString();
    }
}