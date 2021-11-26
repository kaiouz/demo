package com.codemaster.demo.word;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DGSX {

    public static void main(String[] args) throws IOException {

        try (XWPFDocument doc = new XWPFDocument()) {
            // 公司名称标题
            WorldUtil.createTitle(doc, "故乡的云（苏州）数据技术有限公司", ParagraphAlignment.CENTER, true, 10);
            // 标题
            WorldUtil.createTitle(doc, "顶岗实习计划定向申报表", ParagraphAlignment.CENTER, true, 12);

            WorldUtil.createTitle(doc, "学校计划 (扬州技师学院)", ParagraphAlignment.LEFT, true, 8);

            List<Object> models = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                Model model = new Model("计算机系", "信息管理", "2016", "30", "2019.02.20", "2019.06.20", "扬州市", "王凯", "18661131145");
                models.add(model);
            }
            // 表尾
            List<String> footer = Arrays.asList("合计", "--", "--", "100", "--", "--", "--", "--", "--");

            WordTableStyle wordTableStyle = WorldUtil.createTableStyle(Model.class);
            WorldUtil.createTable(doc, wordTableStyle, models, footer);

//            // 表格
//            // 表头
//            List<String> header = Arrays.asList("系部", "专业", "级", "人数", "开始时间", "截止时间", "实习地区", "负责人", "联系电话");
//            // 列宽
//            List<Integer> widths = Arrays.asList(900, 900, 900, 900, 1200, 1200, 900, 900, 1300);
//            // 表尾
//            List<String> footer = Arrays.asList("合计", "--", "--", "100", "--", "--", "--", "--", "--");
//
//            // 数据, 可以用mapper把对象转成list
//            List<List<String>> data = new ArrayList<>();
//            for (int i = 0; i < 20; i++) {
//                List<String> row = Arrays.asList("计算机系", "信息管理", "2016", "30", "2019.02.20", "2019.06.20", "扬州市", "王凯", "18661131145");
//                data.add(row);
//            }
//
//            WorldUtil.createTable(doc, header, 8, data, 8, footer, 8, 200, widths);

            // 写入文件
            try (OutputStream out = Files.newOutputStream(Paths.get("dgsx.docx"))) {
                doc.write(out);
            }
        }
    }
}
