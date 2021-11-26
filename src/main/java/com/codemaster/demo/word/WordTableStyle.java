package com.codemaster.demo.word;

import java.util.List;

public class WordTableStyle {

    private int rowHeight;

    private int fontSize;

    List<WordTableColStyle> colStyles;


    public WordTableStyle(WordTable wordTable) {
        this.rowHeight = wordTable.rowHeight();
        this.fontSize = wordTable.fontSize();
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }

    public List<WordTableColStyle> getColStyles() {
        return colStyles;
    }

    public void setColStyles(List<WordTableColStyle> colStyles) {
        this.colStyles = colStyles;
    }
}
