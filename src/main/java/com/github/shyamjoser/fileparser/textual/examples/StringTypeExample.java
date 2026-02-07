package com.github.shyamjoser.fileparser.textual.examples;

import com.github.shyamjoser.fileparser.textual.annotation.FixedField;
import com.github.shyamjoser.fileparser.textual.annotation.FixedLengthRecord;
import lombok.Data;

@FixedLengthRecord(totalLength = 50, trim = true)
public class StringTypeExample {

    @FixedField(position = 1, length = 10)
    public String firstName;

    @FixedField(position = 11, length = 15, trim = false)
    public String lastName;

    @FixedField(position = 26, length = 25, defaultValue = "email@fm.com")
    public String email;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}