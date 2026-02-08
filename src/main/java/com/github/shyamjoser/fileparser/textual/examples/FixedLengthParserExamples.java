package com.github.shyamjoser.fileparser.textual.examples;

import com.github.shyamjoser.fileparser.textual.annotation.*;
import com.github.shyamjoser.fileparser.textual.parser.FixedLengthParser;

/**
 * Comprehensive examples covering all combinations of the FixedLengthParser.
 *
 * This class demonstrates:
 * - Parsing and serializing all supported data types
 * - Using default values
 * - Handling different field positions and lengths
 * - Working with both primitive and wrapper types
 */
public class FixedLengthParserExamples {

    public static void main(String[] args) throws Exception {
        System.out.println("========== FixedLengthParser Examples ==========\n");
        // String data = "Shyam JoseR             Shyam JOse Richard is a software engineer who is playing around with code now.         ";
        String data = "Shyam JoseR            ";
        // String data = "Shyam JoseR              shyamjose.r@gmail.com     ";
        System.out.println("Input Data: '" + data + "'\n");
        StringTypeExample example = FixedLengthParser.parse(data, StringTypeExample.class);
        System.out.println("Parsed StringTypeExample:");
        System.out.println("First Name: " + example.firstName);
        System.out.println("Last Name: " + example.lastName);
        System.out.println("Email: " + example.email);

        String serialized = FixedLengthParser.serialize(example);
        System.out.println("\nSerialized StringTypeExample: '" + serialized + "'");

        var jsonNode = FixedLengthParser.serializeToJson(example);
        System.out.println("\nSerialized JSON (Compact): " + jsonNode);
    }
}