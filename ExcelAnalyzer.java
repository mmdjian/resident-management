import java.io.File;
import java.io.FileInputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;

public class ExcelAnalyzer {
    public static void main(String[] args) {
        String filePath = "C:/Users/Administrator/Downloads/6_4_润泽知园+111(3).xlsx";
        File file = new File(filePath);

        try {
            if (filePath.toLowerCase().endsWith(".xlsx")) {
                FileInputStream fis = new FileInputStream(file);
                XSSFWorkbook workbook = new XSSFWorkbook(fis);
                XSSFSheet sheet = workbook.getSheetAt(0);

                System.out.println("工作表名称: " + workbook.getSheetName(0));
                System.out.println("总行数: " + sheet.getPhysicalNumberOfRows());
                System.out.println("总列数: " + sheet.getRow(0).getLastCellNum());

                System.out.println("\n前10行内容:");
                for (int i = 0; i < Math.min(10, sheet.getPhysicalNumberOfRows()); i++) {
                    XSSFRow row = sheet.getRow(i);
                    if (row == null) {
                        System.out.println("第" + (i + 1) + "行: [空行]");
                        continue;
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("第").append(i + 1).append("行: [");
                    int cellCount = Math.min(15, row.getLastCellNum());
                    for (int j = 0; j < cellCount; j++) {
                        XSSFCell cell = row.getCell(j);
                        String cellValue = getCellValue(cell);
                        sb.append(j + 1).append("=").append(cellValue);
                        if (j < cellCount - 1) sb.append(", ");
                    }
                    sb.append("]");
                    System.out.println(sb.toString());
                }

                workbook.close();
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getCellValue(XSSFCell cell) {
        if (cell == null) return "[null]";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double num = cell.getNumericCellValue();
                if (num == (long) num) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "[blank]";
            default:
                return "[unknown]";
        }
    }
}
