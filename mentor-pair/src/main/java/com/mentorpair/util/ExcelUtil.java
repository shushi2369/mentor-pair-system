package com.mentorpair.util;

import com.mentorpair.common.BusinessException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 成绩单 Excel 解析：首行表头固定为 学号、姓名、成绩；逐行校验容错 */
public class ExcelUtil {

    public static class GradeRow {
        public int line;
        public String studentNo;
        public String studentName;
        public BigDecimal score;
    }

    public static class ParseResult {
        public List<GradeRow> rows = new ArrayList<>();
        public List<String> errors = new ArrayList<>();
    }

    public static class StudentRow {
        public int line;
        public String studentNo;
        public String studentName;
        public String password;
        public String major;
        public String className;
    }

    public static class StudentParseResult {
        public List<StudentRow> rows = new ArrayList<>();
        public List<String> errors = new ArrayList<>();
    }

    /** 学员名单解析：首行表头固定为 学号、姓名、密码、专业、班级（除学号姓名外值可空，密码留空默认 123456） */
    public static StudentParseResult parseStudentExcel(InputStream in) throws IOException {
        Workbook wb;
        try {
            wb = WorkbookFactory.create(in);
        } catch (Exception e) {
            throw new BusinessException("无法解析该文件，请上传有效的 .xls/.xlsx 学员名单");
        }
        try {
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null) {
                throw new BusinessException("表格为空：首行需为表头（学号、姓名、密码、专业、班级）");
            }
            String[] heads = {"学号", "姓名", "密码", "专业", "班级"};
            for (int i = 0; i < heads.length; i++) {
                if (!heads[i].equals(cellText(header, i))) {
                    throw new BusinessException("表头不正确：首行需依次为 学号、姓名、密码、专业、班级");
                }
            }
            if (sheet.getLastRowNum() > 1000) {
                throw new BusinessException("学员行数超过上限（单次最多导入 1000 行），请拆分文件");
            }
            StudentParseResult result = new StudentParseResult();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String no = cellText(row, 0);
                String name = cellText(row, 1);
                String pwd = cellText(row, 2);
                String major = cellText(row, 3);
                String cls = cellText(row, 4);
                if (no.isEmpty() && name.isEmpty() && pwd.isEmpty() && major.isEmpty() && cls.isEmpty()) {
                    continue;
                }
                int line = i + 1;
                if (no.isEmpty()) {
                    result.errors.add("第" + line + "行：学号为空");
                    continue;
                }
                if (name.isEmpty()) {
                    result.errors.add("第" + line + "行：姓名为空");
                    continue;
                }
                StudentRow sr = new StudentRow();
                sr.line = line;
                sr.studentNo = no;
                sr.studentName = name;
                sr.password = pwd;
                sr.major = major;
                sr.className = cls;
                result.rows.add(sr);
            }
            return result;
        } finally {
            wb.close();
        }
    }

    public static boolean isExcelFileName(String name) {
        if (name == null) {
            return false;
        }
        String n = name.toLowerCase();
        return n.endsWith(".xls") || n.endsWith(".xlsx");
    }

    public static ParseResult parseGradeExcel(InputStream in) throws IOException {
        Workbook wb;
        try {
            wb = WorkbookFactory.create(in);
        } catch (Exception e) {
            throw new BusinessException("无法解析该文件，请上传有效的 .xls/.xlsx 成绩单");
        }
        try {
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null) {
                throw new BusinessException("表格为空：首行需为表头（学号、姓名、成绩）");
            }
            if (!"学号".equals(cellText(header, 0)) || !"姓名".equals(cellText(header, 1))
                    || !"成绩".equals(cellText(header, 2))) {
                throw new BusinessException("表头不正确：首行需依次为 学号、姓名、成绩");
            }
            if (sheet.getLastRowNum() > 5000) {
                throw new BusinessException("成绩行数超过上限（单次最多导入 5000 行），请拆分文件");
            }
            ParseResult result = new ParseResult();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String no = cellText(row, 0);
                String name = cellText(row, 1);
                String scoreText = cellText(row, 2);
                if (no.isEmpty() && name.isEmpty() && scoreText.isEmpty()) {
                    continue;
                }
                int line = i + 1;
                if (no.isEmpty()) {
                    result.errors.add("第" + line + "行：学号为空");
                    continue;
                }
                BigDecimal score;
                try {
                    score = new BigDecimal(scoreText);
                } catch (Exception e) {
                    result.errors.add("第" + line + "行：成绩“" + scoreText + "”不是数字");
                    continue;
                }
                if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0) {
                    result.errors.add("第" + line + "行：成绩超出 0-100 范围");
                    continue;
                }
                GradeRow g = new GradeRow();
                g.line = line;
                g.studentNo = no;
                g.studentName = name;
                g.score = score;
                result.rows.add(g);
            }
            return result;
        } finally {
            wb.close();
        }
    }

    private static String cellText(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return numText(cell.getNumericCellValue());
        }
        if (cell.getCellType() == CellType.FORMULA) {
            try {
                return cell.getStringCellValue().trim();
            } catch (Exception ignored) {
            }
            try {
                return numText(cell.getNumericCellValue());
            } catch (Exception ignored) {
            }
            return "";
        }
        return "";
    }

    private static String numText(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d)) {
            return String.valueOf((long) d);
        }
        return BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
    }
}
