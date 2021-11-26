package com.codemaster.demo.word;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WordTableCol {

    String name() default "";

    int width() default 1000;

    boolean bold() default false;

    int index() default 0;

    ParagraphAlignment alignment() default ParagraphAlignment.CENTER;
}
