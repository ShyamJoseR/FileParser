package com.github.shyamjoser.fileparser.textual.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FixedField {
    int position();
    int length();
    boolean trim() default true;
    String defaultValue() default "";
}