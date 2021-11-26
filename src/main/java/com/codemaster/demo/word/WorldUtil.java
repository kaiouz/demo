package com.codemaster.demo.word;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class WorldUtil {


    /**
     * 创建表格
     *
     * @param doc            文档对象
     * @param wordTableStyle 表格样式
     * @param data           数据
     */
    public static void createTable(XWPFDocument doc, WordTableStyle wordTableStyle, List<Object> data) {
        createTable(doc, wordTableStyle, data, null);
    }


    /**
     * 创建表格
     *
     * @param doc            文档对象
     * @param wordTableStyle 表格样式
     * @param data           数据
     * @param footer         表尾
     */
    public static void createTable(XWPFDocument doc, WordTableStyle wordTableStyle, List<Object> data,
                                   List<String> footer) {
        List<WordTableColStyle> colStyles = wordTableStyle.getColStyles();
        int nCols = colStyles.size();
        int nRows = data.size() + 1;
        boolean hasFooter = false;
        if (footer != null && !footer.isEmpty()) {
            nRows++;
            hasFooter = true;
        }

        XWPFTable table = doc.createTable(nRows, nCols);
        List<XWPFTableRow> rows = table.getRows();
        int rowCt = 0;
        int colCt = 0;
        for (XWPFTableRow row : rows) {
            // 行属性
            CTTrPr trPr = row.getCtRow().addNewTrPr();
            // 设置行高
            CTHeight ht = trPr.addNewTrHeight();
            ht.setVal(BigInteger.valueOf(wordTableStyle.getRowHeight()));

            List<XWPFTableCell> cells = row.getTableCells();
            // 向每一列添加内容
            for (XWPFTableCell cell : cells) {
                WordTableColStyle colStyle = colStyles.get(colCt);

                // 列属性
                CTTcPr tcpr = cell.getCTTc().addNewTcPr();
                // 垂直居中
                tcpr.addNewVAlign().setVal(STVerticalJc.CENTER);
                // 列宽
                tcpr.addNewTcW().setW(BigInteger.valueOf(colStyle.getWidth()));

                // 设置格子内容
                XWPFParagraph para = cell.getParagraphs().get(0);
                XWPFRun rh = para.createRun();
                rh.setFontSize(wordTableStyle.getFontSize());

                if (rowCt == 0) {
                    // 表头
                    rh.setText(colStyle.getName());
                    rh.setBold(true);
                    para.setAlignment(ParagraphAlignment.CENTER);
                } else if (hasFooter && rowCt == nRows - 1) {
                    // 表尾
                    rh.setText(footer.get(colCt));
                    para.setAlignment(ParagraphAlignment.CENTER);
                } else {
                    // 数据行
                    rh.setText(colStyle.getText(data.get(rowCt - 1)));
                    rh.setBold(colStyle.isBold());
                    para.setAlignment(colStyle.getAlignment());
                }

                colCt++;
            } // for cell
            colCt = 0;
            rowCt++;
        } // for row
    }


    /**
     * 读取模型类中的注解, 生成表格样式
     *
     * @param modelClass 模型类
     * @return 表格样式
     */
    public static WordTableStyle createTableStyle(Class<?> modelClass) {
        WordTable wordTable = modelClass.getDeclaredAnnotation(WordTable.class);
        Objects.requireNonNull(wordTable);

        WordTableStyle wordTableStyle = new WordTableStyle(wordTable);

        Field[] fields = modelClass.getDeclaredFields();
        List<WordTableColStyle> colStyles = Arrays.stream(fields)
                .filter(field -> field.getAnnotation(WordTableCol.class) != null)
                .map(field -> {
                    WordTableCol wordTableCol = field.getAnnotation(WordTableCol.class);
                    field.setAccessible(true);
                    return new WordTableColStyle(wordTableCol, field);
                })
                .sorted(Comparator.comparingInt(WordTableColStyle::getIndex))
                .collect(Collectors.toList());

        wordTableStyle.setColStyles(colStyles);

        return wordTableStyle;
    }


    public static void createTable(XWPFDocument doc,
                                   List<String> header,
                                   int headerFontSize,
                                   List<List<String>> data,
                                   int dataFontSize,
                                   List<String> footer,
                                   int footerFontSize,
                                   int rowHeight,
                                   List<Integer> cellWidth) {
        int nRows = data.size() + 1;
        boolean hasFooter = false;
        if (footer != null && !footer.isEmpty()) {
            nRows++;
            hasFooter = true;
        }
        int nCols = header.size();

        // 列宽
        List<BigInteger> cellWidthBig = cellWidth.stream().map(BigInteger::valueOf).collect(Collectors.toList());
        XWPFTable table = doc.createTable(nRows, nCols);

        int rowCt = 0;
        List<XWPFTableRow> rows = table.getRows();
        // 行最大索引
        int maxRowIndex = nRows - 1;

        for (XWPFTableRow row : rows) {
            // 添加行高
            CTTrPr trPr = row.getCtRow().addNewTrPr();
            CTHeight ht = trPr.addNewTrHeight();
            ht.setVal(BigInteger.valueOf(rowHeight));

            // 表头
            if (rowCt == 0) {
                createTableRow(row, header, cellWidthBig, headerFontSize, true);
            } else if (rowCt == maxRowIndex && hasFooter) {
                // 表尾
                createTableRow(row, footer, cellWidthBig, footerFontSize, false);
            } else {
                // 数据行
                createTableRow(row, data.get(rowCt - 1), cellWidthBig, dataFontSize, false);
            }

            rowCt++;
        }
    }

    private static void createTableRow(XWPFTableRow row,
                                       List<String> data,
                                       List<BigInteger> cellWidth,
                                       int fontSize,
                                       boolean bold) {
        int colCt = 0;

        for (XWPFTableCell cell : row.getTableCells()) {
            // 列属性
            CTTcPr tcpr = cell.getCTTc().addNewTcPr();
            // 列宽
            tcpr.addNewTcW().setW(cellWidth.get(colCt));

            // 垂直居中
            CTVerticalJc va = tcpr.addNewVAlign();
            va.setVal(STVerticalJc.CENTER);

            // 设置内容
            XWPFParagraph para = cell.getParagraphs().get(0);
            XWPFRun rh = para.createRun();
            rh.setText(data.get(colCt));
            rh.setFontSize(fontSize);
            rh.setBold(bold);
            // 水平居中
            para.setAlignment(ParagraphAlignment.CENTER);

            colCt++;
        }
    }


    public static void createTitle(XWPFDocument doc,
                                   String text,
                                   ParagraphAlignment alignment,
                                   boolean bold,
                                   int fontSize) {
        XWPFParagraph paragraph = doc.createParagraph();
        // 对齐方式
        paragraph.setAlignment(alignment);

        XWPFRun run = paragraph.createRun();
        // 内容
        run.setText(text);
        // 加粗
        run.setBold(bold);
        // 字体大小
        run.setFontSize(fontSize);
    }
}
