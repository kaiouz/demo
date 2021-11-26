package com.codemaster.demo.word;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;

import java.lang.reflect.Field;

public class WordTableColStyle {

    private String name;

    private int width;

    private boolean bold;

    private int index;

    private ParagraphAlignment alignment;

    private Field field;

    public WordTableColStyle(WordTableCol wordTableCol, Field field) {
        this.name = wordTableCol.name();
        this.width = wordTableCol.width();
        this.bold = wordTableCol.bold();
        this.index = wordTableCol.index();
        this.alignment = wordTableCol.alignment();
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ParagraphAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(ParagraphAlignment alignment) {
        this.alignment = alignment;
    }

    public String getText(Object o) {
        try {
            return (String) field.get(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
