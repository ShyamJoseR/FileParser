package com.github.shyamjoser.fileparser.textual.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FixedLengthRecord {
    int totalLength() default -1;
    boolean trim() default true;
}
